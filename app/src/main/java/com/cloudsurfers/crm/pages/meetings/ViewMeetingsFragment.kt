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
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cloudsurfers.crm.R
import com.cloudsurfers.crm.functions.CalendarUtil
import com.cloudsurfers.crm.functions.Meeting
import kotlin.collections.ArrayList


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ViewMeetingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ViewMeetingsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

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



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_view_meetings, container, false)

        val activity: Activity = activity as Activity

        var meetingsList: ArrayList<Meeting> = ArrayList()

        if (requestPermission(activity, Manifest.permission.READ_CALENDAR) &&
            requestPermission(activity, Manifest.permission.READ_CONTACTS)) {
            meetingsList = Meeting.fetchAllMeetings(activity) as ArrayList<Meeting>
        }

        view.findViewById<RecyclerView>(R.id.view_meetings_list_recycler_view).apply {
            adapter = ViewMeetingsAdapter(meetingsList)
            layoutManager = LinearLayoutManager(activity)
        }

        val addMeetingButton = view.findViewById<Button>(R.id.addMeetingButton)
        addMeetingButton.setOnClickListener(){
            Navigation.findNavController(view).navigate(R.id.view_meeting_to_add_meeting)
        }

        val viewMeetingButton = view.findViewById<Button>(R.id.viewCalendarButton)
        viewMeetingButton.setOnClickListener(){
        val intent: Intent = CalendarUtil.getViewCalendarIntent()
            startActivity(intent)
        }

        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ViewMeetingsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ViewMeetingsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}