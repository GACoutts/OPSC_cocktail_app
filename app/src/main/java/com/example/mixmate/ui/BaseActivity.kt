package com.example.mixmate.ui

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.mixmate.*

enum class FooterTab { HOME, DISCOVER, LIST, FAVOURITES, PROFILE }

/** Extend this instead of AppCompatActivity in screens that include the footer. */
abstract class BaseActivity : AppCompatActivity() {

    /** Child screens can override to highlight the active tab. */
    open fun activeTab(): FooterTab? = null

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID); wireFooter()
    }
    override fun setContentView(view: View?) {
        super.setContentView(view); wireFooter()
    }

    private fun wireFooter() {
        val home = findViewById<ImageView?>(R.id.nav_home)
        val discover = findViewById<ImageView?>(R.id.nav_discover)
        val list = findViewById<ImageView?>(R.id.nav_list)
        val fav = findViewById<ImageView?>(R.id.nav_favourites)
        val profile = findViewById<ImageView?>(R.id.nav_profile)

        if (home == null && discover == null && list == null && fav == null && profile == null) return

        // highlight active tab
        listOf(home, discover, list, fav, profile).forEach { it?.isSelected = false }
        when (activeTab()) {
            FooterTab.HOME -> home?.isSelected = true
            FooterTab.DISCOVER -> discover?.isSelected = true
            FooterTab.LIST -> list?.isSelected = true
            FooterTab.FAVOURITES -> fav?.isSelected = true
            FooterTab.PROFILE -> profile?.isSelected = true
            null -> {}
        }

        fun goIfNotCurrent(target: Class<out Activity>) {
            if (javaClass != target) startActivity(Intent(this, target))
        }

        home?.setOnClickListener { goIfNotCurrent(HomePage::class.java) }
        discover?.setOnClickListener { goIfNotCurrent(DiscoverPage::class.java) }
        list?.setOnClickListener { goIfNotCurrent(MyBar::class.java) }
        fav?.setOnClickListener { goIfNotCurrent(FavouritesActivity::class.java) }
        profile?.setOnClickListener { goIfNotCurrent(ProfileActivity::class.java) }

    }
}
