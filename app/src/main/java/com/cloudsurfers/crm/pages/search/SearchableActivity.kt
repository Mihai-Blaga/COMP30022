package com.cloudsurfers.crm.pages.search

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SearchView
import androidx.appcompat.widget.SearchView.SearchAutoComplete
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cloudsurfers.crm.R
import com.cloudsurfers.crm.functions.Contact
import com.cloudsurfers.crm.pages.main.MainActivity

class SearchableActivity : MainActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_searchable)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager

        // Set the searchable configuration of the SearchView
        findViewById<SearchView>(R.id.searchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
        }

        // Change colour of hint and icon in search view
//        val searchAutoComplete: SearchAutoComplete = findViewById<SearchAutoComplete>(R.id.search_src_text);
//        searchAutoComplete.setHintTextColor(ContextCompat.getColor(this, R.color.light_white));
//        searchAutoComplete.setTextColor(ContextCompat.getColor(this, R.color.light_white));

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    // Filter contacts by first letter in name and query
    private fun basicContactSearch(query: String): ArrayList<Contact> {
        return Contact.readContacts(this)?.filter { c ->
            c.name?.lowercase()?.startsWith(query.lowercase())  == true
        } as ArrayList<Contact>
    }

    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH == intent.action) {
            intent.getStringExtra(SearchManager.QUERY)?.also { query ->
                // TODO: Perform The Search
                val contacts: ArrayList<Contact> = basicContactSearch(query)

                // Add the contents into an adapter class and put into RecyclerView
                findViewById<RecyclerView>(R.id.search_recycler_view).apply {
                    adapter = SearchAdapter(contacts)
                    layoutManager = LinearLayoutManager(this@SearchableActivity)
                }
            }
        }
    }
}