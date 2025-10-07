package com.example.mixmate

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class HomePage : AppCompatActivity() {

    private lateinit var btnLogOut: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

    //menu inflation
    fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu) // replace with your menu file name
        return true
    }

    // Handle toolbar item clicks
    fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.btnLogOut -> {
                // Handle logout
                val intent = Intent(this, DiscoverPage::class.java)
                startActivity(intent)
                finish()
                true
            }
            R.id.action_search -> {
                // Handle search
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
