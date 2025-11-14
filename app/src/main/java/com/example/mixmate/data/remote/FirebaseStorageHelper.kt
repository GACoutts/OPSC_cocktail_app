package com.example.mixmate.data.remote

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Helper class for Firebase Storage operations
 * Handles uploading and managing recipe images in Firebase Cloud Storage
 */
object FirebaseStorageHelper {

    private const val TAG = "FirebaseStorageHelper"
    private const val RECIPES_PATH = "recipe_images"

    private val storage: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }

    private val storageRef: StorageReference by lazy {
        storage.reference
    }

    /**
     * Upload a recipe image to Firebase Storage
     *
     * @param imageUri Local URI of the image to upload
     * @param userId User ID to organize images by user
     * @param recipeId Optional recipe ID for organizing images
     * @return Download URL of the uploaded image, or null if upload fails
     */
    suspend fun uploadRecipeImage(
        imageUri: Uri,
        userId: String,
        recipeId: String? = null
    ): String? {
        return try {
            // Generate unique filename
            val imageId = recipeId ?: UUID.randomUUID().toString()
            val fileName = "${userId}_${imageId}_${System.currentTimeMillis()}.jpg"
            val imagePath = "$RECIPES_PATH/$userId/$fileName"

            Log.d(TAG, "Uploading image to path: $imagePath")

            // Create reference to the storage location
            val imageRef = storageRef.child(imagePath)

            // Upload the file
            val uploadTask = imageRef.putFile(imageUri).await()

            // Get the download URL
            val downloadUrl = imageRef.downloadUrl.await()

            Log.d(TAG, "Image uploaded successfully. URL: $downloadUrl")
            downloadUrl.toString()

        } catch (e: Exception) {
            Log.e(TAG, "Error uploading image: ${e.message}", e)
            null
        }
    }

    /**
     * Delete a recipe image from Firebase Storage
     *
     * @param imageUrl Full download URL of the image to delete
     * @return true if deletion was successful, false otherwise
     */
    suspend fun deleteRecipeImage(imageUrl: String): Boolean {
        return try {
            // Get reference from URL
            val imageRef = storage.getReferenceFromUrl(imageUrl)

            Log.d(TAG, "Deleting image: $imageUrl")
            imageRef.delete().await()

            Log.d(TAG, "Image deleted successfully")
            true

        } catch (e: Exception) {
            Log.e(TAG, "Error deleting image: ${e.message}", e)
            false
        }
    }

    /**
     * Delete all images for a specific user
     * Useful for account deletion or cleanup
     *
     * @param userId User ID whose images should be deleted
     * @return Number of images deleted
     */
    suspend fun deleteAllUserImages(userId: String): Int {
        return try {
            val userImagesRef = storageRef.child("$RECIPES_PATH/$userId")

            Log.d(TAG, "Deleting all images for user: $userId")

            // List all files in the user's directory
            val listResult = userImagesRef.listAll().await()

            var deletedCount = 0
            for (item in listResult.items) {
                try {
                    item.delete().await()
                    deletedCount++
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to delete image: ${item.path}", e)
                }
            }

            Log.d(TAG, "Deleted $deletedCount images for user $userId")
            deletedCount

        } catch (e: Exception) {
            Log.e(TAG, "Error deleting user images: ${e.message}", e)
            0
        }
    }

    /**
     * Check if an image URL is from Firebase Storage
     *
     * @param url URL to check
     * @return true if URL is from Firebase Storage
     */
    fun isFirebaseStorageUrl(url: String?): Boolean {
        return url?.contains("firebasestorage.googleapis.com") == true
    }

    /**
     * Get file size limit for uploads (in bytes)
     * Default: 10MB
     */
    const val MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024 // 10MB

    /**
     * Validate image file size before upload
     *
     * @param sizeInBytes Size of the file in bytes
     * @return true if size is within limits
     */
    fun isValidFileSize(sizeInBytes: Long): Boolean {
        return sizeInBytes <= MAX_FILE_SIZE_BYTES
    }
}
