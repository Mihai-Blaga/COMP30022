package com.cloudsurfers.crm

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.recyclerview.widget.RecyclerView

class ViewContactsAdapter(private val contacts: ArrayList<Contact>) :
    RecyclerView.Adapter<ViewContactsAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val contactNameTextView: TextView = view.findViewById(R.id.view_contacts_list_item_contact_name_text_view)
        init {
            // Define click listener for the ViewHolder's View.
            view.setOnClickListener() {
                val activity: AppCompatActivity = view.context as AppCompatActivity

                activity.supportFragmentManager.commit {
                    setReorderingAllowed(true)
                    // Replace whatever is in the fragment_container view with this fragment
                    replace<ViewContactFragment>(R.id.nav_host_fragment_container)
                }
            }
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.view_contacts_list_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.contactNameTextView.text = contacts[position].name
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = contacts.size

}