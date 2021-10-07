package com.cloudsurfers.crm.pages.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.cloudsurfers.crm.R
import com.cloudsurfers.crm.pages.search.SearchableActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

open class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
        val navController = navHostFragment.navController

        // Set-up the bottom navigation
        findViewById<BottomNavigationView>(R.id.bottom_navigation)
            .setupWithNavController(navController)

        // Set-up the top toolbar
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        val toolbar = findViewById<Toolbar>(R.id.toolbar).apply {
            setupWithNavController(navController, appBarConfiguration)
            inflateMenu(R.menu.top_app_bar_contact_list)
        }
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // Only show back icon when not in view meetings (home page)
            if (destination.id != R.id.viewMeetingsFragment)
                findViewById<Toolbar>(R.id.toolbar).setNavigationIcon(R.drawable.ic_back)
            // Make not visible
            toolbar.findViewById<View>(R.id.view_contacts_list_search_icon_button).isVisible =
                destination.id == R.id.viewContactsList
        }

        // Set search button to only show when in view contacts list page
        toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.view_contacts_list_search_icon_button)
                startActivity(Intent(this, SearchableActivity::class.java))
            true
        }

        // Override the back-button of the toolbar to be the same as the up android up button
        toolbar.setNavigationOnClickListener {
            this.onBackPressed()
        }

        // Navigate to the ViewContactFragment if coming from search
        if (Intent.ACTION_SEARCH == intent.action) {
            val bundle: Bundle? = intent.extras
            navController.popBackStack()  // Pop back-stack so that navigation takes back to the search page instead
            navController.navigate(R.id.viewContactFragment, bundle)
        }
    }


    override fun onResume() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
        val navController = navHostFragment.navController
        if (navController.currentDestination?.id == R.id.viewMeetingsFragment) {
            findViewById<BottomNavigationView>(R.id.bottom_navigation)?.selectedItemId= R.id.viewMeetingsFragment
        }
        super.onResume()
    }
}