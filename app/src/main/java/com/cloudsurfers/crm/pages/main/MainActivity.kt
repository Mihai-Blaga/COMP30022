package com.cloudsurfers.crm.pages.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NavUtils
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.cloudsurfers.crm.R
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
        }
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id != R.id.viewMeetingsFragment)
                findViewById<Toolbar>(R.id.toolbar).setNavigationIcon(R.drawable.ic_back)
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
}