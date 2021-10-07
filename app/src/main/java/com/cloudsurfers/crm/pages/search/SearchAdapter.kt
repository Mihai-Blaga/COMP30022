package com.cloudsurfers.crm.pages.search

import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.RecyclerView
import com.cloudsurfers.crm.R
import com.cloudsurfers.crm.functions.Contact
import com.cloudsurfers.crm.pages.main.MainActivity

class SearchAdapter(private val contacts: ArrayList<Contact>):
    RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    @RequiresApi(Build.VERSION_CODES.N)
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val searchItemTextView: TextView = view.findViewById(R.id.search_list_item_text_view)
        lateinit var contact: Contact

        init {
            // Define click listener for the ViewHolder's View.
            view.setOnClickListener {
                val activity: AppCompatActivity = view.context as AppCompatActivity
                val c: Contact = Contact.readContact(contact, activity)

                val bundle = bundleOf("name" to c.name, "email" to c.email, "mobile" to c.phone, "notes" to c.note, "tags" to c.getGroupNames(activity))

                val viewContactIntent: Intent = Intent(activity, MainActivity::class.java).apply {
                    action = Intent.ACTION_SEARCH
                    putExtras(bundle)
                }
                activity.startActivity(viewContactIntent)
            }
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.search_list_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.searchItemTextView.text = contacts[position].name
        viewHolder.contact = contacts[position]
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = contacts.size

}