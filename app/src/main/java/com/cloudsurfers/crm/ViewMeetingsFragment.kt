package com.cloudsurfers.crm

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.ArrayList


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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val requestCode = 1
        requestPermissions(arrayOf(Manifest.permission.READ_CALENDAR), requestCode)
        val view: View = inflater.inflate(R.layout.fragment_view_meetings, container, false)

        val activity: Activity = activity as Activity
//        println("before")
//        println(Meeting.fetchAllMeetings(activity))
//        println("after")
//        println("before contacts")
//        println(Contact.readContacts(activity))
//        println("after contacts")
        val meetingsList: ArrayList<Meeting> = Meeting.fetchAllMeetings(activity) as ArrayList<Meeting>




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