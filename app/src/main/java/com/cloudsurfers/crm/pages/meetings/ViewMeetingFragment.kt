package com.cloudsurfers.crm.pages.meetings

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.cloudsurfers.crm.databinding.FragmentViewMeetingBinding
import com.cloudsurfers.crm.functions.CalendarUtil
import com.cloudsurfers.crm.functions.Util
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "contactEmail"
private const val ARG_PARAM2 = "title"
private const val ARG_PARAM3 = "date"
private const val ARG_PARAM4 = "time"
private const val ARG_PARAM5 = "location"
private const val ARG_PARAM6 = "notes"

/**
 * A simple [Fragment] subclass.
 * Use the [AddMeetingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ViewMeetingFragment : Fragment() {
    private var contactEmail: String? = null
    private var title: String? = null
    private var date: String? = null
    private var time: String? = null
    private var location: String? = null
    private var notes: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            contactEmail = it.getString(ARG_PARAM1)
            title = it.getString(ARG_PARAM2)
            date = it.getString(ARG_PARAM3)
            time = it.getString(ARG_PARAM4)
            location = it.getString(ARG_PARAM5)
            notes = it.getString(ARG_PARAM6)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentViewMeetingBinding.inflate(layoutInflater, container, false)

        // Set value of text fields
        println("${contactEmail}, ${title}, ${date}, ${time}, ${location}, ${notes}")
        binding.viewMeetingOutlinedTextFieldMeetingContact.editText?.setText(contactEmail)
        binding.viewMeetingOutlinedTextFieldMeetingName.editText?.setText(title)
        binding.viewMeetingOutlinedTextFieldMeetingDate.editText?.setText(date)
        binding.viewMeetingOutlinedTextFieldMeetingTime.editText?.setText(time)
        binding.viewMeetingOutlinedTextFieldMeetingLocation.editText?.setText(location)
        binding.viewMeetingOutlinedTextFieldMeetingNotes.editText?.setText(notes)


        // Stores the date and time that can be changed by the user
        val cal = Calendar.getInstance().apply {
            if (date != null && date != "")
                set(date!!.split(".")[2].toInt(),
                    date!!.split(".")[1].toInt(),
                    date!!.split(".")[0].toInt())
            if (this@ViewMeetingFragment.time != null && this@ViewMeetingFragment.time != "") {
                val hours = this@ViewMeetingFragment.time!!.split(":")[0].toInt()
                val minutes = this@ViewMeetingFragment.time!!.split(":")[1].toInt()
                set(Calendar.HOUR_OF_DAY, hours)
                set(Calendar.MINUTE, minutes)
            }
        }

        // Date and Time Dialog Selectors
        val dateSetListener =
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val myFormat = "dd.MM.yyyy" // mention the format you need
                val sdf = SimpleDateFormat(myFormat, Locale.US)
                binding.viewMeetingOutlinedTextFieldMeetingDate.editText?.setText(sdf.format(cal.time))
            }

        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
            cal.set(Calendar.MINUTE, minute)
            binding.viewMeetingOutlinedTextFieldMeetingTime.editText?.setText("${hourOfDay}:${minute}")
        }

        // On Click Listeners
//        binding.outlinedTextFieldMeetingDate.editText?.setOnClickListener(){
//            DatePickerDialog(requireActivity(), dateSetListener,
//                cal.get(Calendar.YEAR),
//                cal.get(Calendar.MONTH),
//                cal.get(Calendar.DAY_OF_MONTH)).show()
//        }
        binding.viewMeetingOutlinedTextFieldMeetingDate.editText?.setOnFocusChangeListener { v, b ->
            // This line prevents keyboard from showing
            Util.hideKeyboard(v, requireContext())
            if (b) {
                DatePickerDialog(
                    requireActivity(), dateSetListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            }

        }

        binding.viewMeetingOutlinedTextFieldMeetingTime.editText?.setOnFocusChangeListener { v, b ->
            // This line prevents keyboard from showing
            Util.hideKeyboard(v, requireContext())
            if (b) {
                TimePickerDialog(
                    requireActivity(), timeSetListener,
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    false
                ).show()
            }
        }

        binding.viewMeetingSaveMeetingButton.setOnClickListener {
            val meetingName =
                binding.viewMeetingOutlinedTextFieldMeetingName.editText?.text.toString()
            val meetingContact =
                binding.viewMeetingOutlinedTextFieldMeetingContact.editText?.text.toString()
            val meetingLocation =
                binding.viewMeetingOutlinedTextFieldMeetingLocation.editText?.text.toString()
            val meetingNotes =
                binding.viewMeetingOutlinedTextFieldMeetingNotes.editText?.text.toString()

//            val intent = CalendarUtil.getInsertEventIntent(meetingName, meetingContact, meetingLocation, cal, meetingNotes)
//            startActivity(intent)

            val eventID = CalendarUtil.addEvent(
                requireActivity(),
                meetingName,
                meetingContact,
                meetingLocation,
                cal,
                meetingNotes
            )

            if (eventID >= 0) {
                requireActivity().onBackPressed()
            }
        }


        return binding.root
    }
}