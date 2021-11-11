package com.cloudsurfers.crm.pages.meetings

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.cloudsurfers.crm.R
import com.cloudsurfers.crm.functions.Meeting
import java.time.format.DateTimeFormatter
import java.util.*

class ViewMeetingsAdapter(private val meetingsList: ArrayList<Meeting>, private val fromViewContact: Boolean) :
    RecyclerView.Adapter<ViewMeetingsAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    @RequiresApi(Build.VERSION_CODES.O)
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val contactNameTextView: TextView = view.findViewById(R.id.view_meetings_list_item_contact_name_text_view)
        val meetingNameTextView: TextView = view.findViewById(R.id.view_meetings_list_item_meeting_name_text_view)
        val meetingTimeTextView: TextView = view.findViewById(R.id.view_meetings_list_item_meeting_time_text_view)
        val meetingDateTextView: TextView = view.findViewById(R.id.view_meetings_list_item_meeting_date_text_view)
        val meetingLocationTextView: TextView = view.findViewById(R.id.view_meetings_list_item_meeting_location_text_view)

        lateinit var meeting: Meeting

    }

    // Create new views (invoked by the layout manager)
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.view_meetings_list_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.contactNameTextView.text = meetingsList[position].contact?.name
        viewHolder.meetingNameTextView.text = meetingsList[position].title
        viewHolder.meetingTimeTextView.text = meetingsList[position].meetingTime.split(" ")[1]
        viewHolder.meetingDateTextView.text = meetingsList[position].meetingDay
        viewHolder.meetingLocationTextView.text = meetingsList[position].location

        viewHolder.meeting = meetingsList[position]

        viewHolder.itemView.setOnClickListener {
            // Format the start date
            val meeting = viewHolder.meeting
            val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
            val date = meeting.beginDate?.format(dateFormatter)
            val time = meeting.beginDate?.format(timeFormatter)

            val bundle = bundleOf(
                "contactEmail" to meeting.contactEmail,
                "title" to meeting.title,
                "date" to date,
                "time" to time,
                "location" to meeting.location,
                "notes" to meeting.description?.substringBefore("\n-::~")?.substringBefore("-::~"),
                "eventID" to meeting.eventID
            )

            if (fromViewContact) {
                Navigation.findNavController(viewHolder.itemView).navigate(R.id.action_viewContactFragment_to_viewMeetingFragment, bundle)
            } else {
                Navigation.findNavController(viewHolder.itemView).navigate(R.id.action_viewMeetingsFragment_to_viewMeetingFragment, bundle)
            }
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = meetingsList.size

}