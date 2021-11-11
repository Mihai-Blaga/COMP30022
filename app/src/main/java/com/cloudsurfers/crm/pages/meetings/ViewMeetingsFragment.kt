package com.cloudsurfers.crm.pages.meetings

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cloudsurfers.crm.R
import com.cloudsurfers.crm.functions.CalendarUtil
import com.cloudsurfers.crm.functions.Meeting
import java.time.LocalDateTime
import kotlin.collections.ArrayList


class ViewMeetingsFragment : Fragment() {

    private var meetingsList: ArrayList<Meeting> = ArrayList()

    //Launches popup requesting access to reading contacts
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                android.util.Log.i("ViewMeetingsFragment", "Read permission granted")

                //Refresh page to enable viewing contact functionality
                this.findNavController().navigate(R.id.viewMeetingsFragment)
            } else {
                android.util.Log.w("ViewMeetingsFragment", "Read permission denied")
            }
        }

    //Used to check if reading contact permission is granted. Otherwise displays permission request
    //popup.
    //Returns boolean on whether permission was granted
    private fun requestPermission(activity: Activity, per: String): Boolean{
        when {
            ContextCompat.checkSelfPermission(
                activity,
                per
            ) == PackageManager.PERMISSION_GRANTED -> {
                return true
            }
            shouldShowRequestPermissionRationale(per) -> {
                //TODO: Explain to user why permission is needed
                requestPermissionLauncher.launch(per)
            }
            else -> {
                requestPermissionLauncher.launch(per)
            }
        }

        return (ContextCompat.checkSelfPermission(activity, per)
                == PackageManager.PERMISSION_GRANTED)
    }




    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_view_meetings, container, false)

        val activity: Activity = activity as Activity

        // Retrieve meetings
        getMeetings(activity)

        // Configure recycler view
        view.findViewById<RecyclerView>(R.id.view_meetings_list_recycler_view).apply {
            adapter = ViewMeetingsAdapter(meetingsList, false)
            layoutManager = LinearLayoutManager(activity)
        }

        val addMeetingButton = view.findViewById<Button>(R.id.addMeetingButton)
        addMeetingButton.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.view_meeting_to_add_meeting)
        }

        val viewMeetingButton = view.findViewById<Button>(R.id.viewCalendarButton)
        viewMeetingButton.setOnClickListener {
        val intent: Intent = CalendarUtil.getViewCalendarIntent()
            startActivity(intent)
        }

        setFragmentResultListener("requestKey") { _, bundle ->
            val refreshMeetings = bundle.getBoolean("refreshMeetings", false)
            if (refreshMeetings) getMeetings(activity)
        }

        return view
    }

    // Retrieve the meetings
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getMeetings(activity: Activity){
        if (requestPermission(activity, Manifest.permission.READ_CALENDAR) &&
            requestPermission(activity, Manifest.permission.READ_CONTACTS)) {
            meetingsList = Meeting.fetchAllMeetings(activity) as ArrayList<Meeting>
            meetingsList = meetingsList.filter {
                LocalDateTime.now().isBefore(if (it.endDate != null) it.endDate else LocalDateTime.now())
            } as ArrayList<Meeting>
        }
    }

}