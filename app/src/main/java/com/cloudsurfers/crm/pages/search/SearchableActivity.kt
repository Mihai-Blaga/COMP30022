package com.cloudsurfers.crm.pages.search

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.SearchView
import android.widget.TextView
import androidx.core.view.children
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cloudsurfers.crm.R
import com.cloudsurfers.crm.functions.Contact
import com.cloudsurfers.crm.pages.main.MainActivity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class SearchableActivity : MainActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_searchable)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager

        // Initialise recycler view with empty array list
        findViewById<RecyclerView>(R.id.search_recycler_view).apply {
            adapter = SearchAdapter(ArrayList())
            layoutManager = LinearLayoutManager(this@SearchableActivity)
        }


        // Set the searchable configuration of the SearchView
        findViewById<SearchView>(R.id.searchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            isIconified = false

            setOnQueryTextListener(object: SearchView.OnQueryTextListener {
                // Do nothing extra on submission
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                // Change the adapter with updated query data every time the string is changed
                override fun onQueryTextChange(query: String?): Boolean {
                    this@SearchableActivity.findViewById<RecyclerView>(R.id.search_recycler_view).apply {
                        adapter = SearchAdapter(basicContactSearch(query!!))
                    }
                    return false
                }
            })

        }

        // Populate chip group with chips
        val tags: ArrayList<String> = ArrayList<String>().apply {
            add("Friend")
            add("University")
            add("COMP30022")
            add("Kotlin")
            add("Squid")
            add("Game")
        }

        val chipGroup: ChipGroup = findViewById(R.id.search_chip_group)

        for (tag: String in tags) {
            val chip: Chip = layoutInflater.inflate(R.layout.search_chip, chipGroup, false) as Chip
            chip.text = tag
            chipGroup.addView(chip)
        }

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    // Filter contacts by first letter in name and query
    private fun basicContactSearch(query: String): ArrayList<Contact> {
        if (query == "") return ArrayList()
        return Contact.readContacts(this)?.filter { c ->
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

    override fun startActivity(intent: Intent?) {
        // Override to add tags to search query

        if (Intent.ACTION_SEARCH == intent?.action) {
            intent.putExtras(
                Bundle().apply {
                    putStringArrayList("tags", getSelectedTags())
                 }
            )
        }

        super.startActivity(intent)
    }

    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH == intent.action) {
            intent.getStringExtra(SearchManager.QUERY)?.also { query ->
                // TODO: Perform The Search
//                val tags: ArrayList<String>? = intent.getStringArrayListExtra("tags")
                val queryContacts: ArrayList<Contact> = basicContactSearch(query)

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


}