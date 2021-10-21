package com.cloudsurfers.crm.pages.search

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.SearchView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.view.children
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cloudsurfers.crm.R
import com.cloudsurfers.crm.functions.Contact
import com.cloudsurfers.crm.functions.Group
import com.cloudsurfers.crm.functions.Meeting
import com.cloudsurfers.crm.pages.main.MainActivity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class SearchableActivity : MainActivity() {

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_searchable)

        // Initialise recycler view with empty array list
        findViewById<RecyclerView>(R.id.search_recycler_view).apply {
            adapter = SearchAdapter(ArrayList())
            layoutManager = LinearLayoutManager(this@SearchableActivity)
        }


        // Set the searchable configuration of the SearchView
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager

        val searchView = findViewById<SearchView>(R.id.searchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            isIconified = false

            setOnQueryTextListener(object: SearchView.OnQueryTextListener {
                // Do nothing extra on submission
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                // Change the adapter with updated query data after user finishes typing
                override fun onQueryTextChange(query: String?): Boolean {
                    handler?.removeCallbacksAndMessages(null)
                    handler?.postDelayed({
                        this@SearchableActivity.findViewById<RecyclerView>(R.id.search_recycler_view).apply {
                            adapter = SearchAdapter(basicContactSearchWithTags(query!!, getSelectedTags()))
                        }
                    }, 250)
                    return false
                }
            })

        }

        val tags: ArrayList<String> = Group.getAllGroupNames(this)

        // Update recycler view each time a new chip is pressed
        val chipGroup: ChipGroup = findViewById(R.id.search_chip_group)
        for (tag: String in tags) {
            val chip: Chip = layoutInflater.inflate(R.layout.search_chip, chipGroup, false).apply {
                setOnClickListener {
                    this@SearchableActivity.findViewById<RecyclerView>(R.id.search_recycler_view)
                        .apply {
                            adapter = SearchAdapter(
                                basicContactSearchWithTags(
                                    searchView.query.toString(),
                                    getSelectedTags()
                                )
                            )
                        }
                }
            } as Chip
            chip.text = tag
            chipGroup.addView(chip)
        }

        // Set back button action
        findViewById<ImageButton>(R.id.search_back_button).apply {
            setOnClickListener {
                this@SearchableActivity.onBackPressed()
            }
        }

        handleIntent(intent)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    // Filter contacts by first letter in name and query
    @RequiresApi(Build.VERSION_CODES.N)
    private fun basicContactSearch(query: String): ArrayList<Contact> {
        if (query == "") return ArrayList()
        return Contact.readContacts(this).filter { c -> !(c.name == null || c.name.equals("")) }
            .filter { c -> c.name!!.lowercase().split(" ").any { it.startsWith(query.lowercase()) }
            } as ArrayList<Contact>
    }

    // Basic contact search with tags
    @RequiresApi(Build.VERSION_CODES.N)
    private fun basicContactSearchWithTags(query: String, tags: ArrayList<String>): ArrayList<Contact> {
        if (tags.size == 0) return basicContactSearch(query)
        return Group.getContactsByGroupName(this, tags).filter { c ->
            c.name?.lowercase()?.startsWith(query.lowercase()) == true
        } as ArrayList<Contact>
    }

    // Extract the selected tags as an array list
    private fun getSelectedTags(): ArrayList<String> {
        return ArrayList(findViewById<ChipGroup>(R.id.search_chip_group).children.filter {
            (it as Chip).isChecked
        }.map {
            (it as Chip).text as String
        }.toList())
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH == intent.action) {
            intent.getStringExtra(SearchManager.QUERY)?.also { query ->
                val tags: ArrayList<String> = getSelectedTags()
                val queryContacts: ArrayList<Contact> = basicContactSearchWithTags(query, tags)

                val recyclerView = findViewById<RecyclerView>(R.id.search_recycler_view)
                val emptyTextView = findViewById<TextView>(R.id.search_no_data_text_view)

                // Add the contents into an adapter class and put into RecyclerView
                recyclerView.apply {
                    adapter = SearchAdapter(queryContacts)
                    layoutManager = LinearLayoutManager(this@SearchableActivity)
                }

                // In case of no data hide the recycler view and provide a message to the user
                if (queryContacts.isEmpty()) {
                    recyclerView.visibility = View.GONE
                    emptyTextView.visibility = View.VISIBLE
                } else {
                    recyclerView.visibility = View.VISIBLE
                    emptyTextView.visibility = View.GONE
                }
            }
        }
    }

//    override fun onResume() {
//        super.onResume()
//        finish();
//        startActivity(intent);
//    }
}