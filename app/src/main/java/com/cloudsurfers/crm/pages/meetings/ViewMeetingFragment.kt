package com.cloudsurfers.crm.pages.meetings

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.cloudsurfers.crm.databinding.FragmentViewMeetingBinding
import com.cloudsurfers.crm.functions.CalendarUtil
import com.cloudsurfers.crm.functions.Util
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM0 = "eventID"
private const val ARG_PARAM1 = "contactEmail"
private const val ARG_PARAM2 = "title"
private const val ARG_PARAM3 = "date"
private const val ARG_PARAM4 = "time"
private const val ARG_PARAM5 = "location"
private const val ARG_PARAM6 = "notes"
private const val ARG_PARAM7 = "eventID"

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
    private var eventID: Long = -1



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            contactEmail = it.getString(ARG_PARAM1)
            title = it.getString(ARG_PARAM2)
            date = it.getString(ARG_PARAM3)
            time = it.getString(ARG_PARAM4)
            location = it.getString(ARG_PARAM5)
            notes = it.getString(ARG_PARAM6)
            eventID = it.getString(ARG_PARAM0)!!.toLong()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentViewMeetingBinding.inflate(layoutInflater, container, false)

        val prevContactEmail = contactEmail;
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
                    date!!.split(".")[1].toInt()-1,
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
            val minuteStr = minute.toString().padStart(2, '0')
            val hourStr = hourOfDay.toString().padStart(2, '0')
            binding.viewMeetingOutlinedTextFieldMeetingTime.editText?.setText("${hourStr}:${minuteStr}")
        }

        // On Click Listeners
//        binding.outlinedTextFieldMeetingDate.editText?.setOnClickListener(){
//            DatePickerDialog(requireActivity(), dateSetListener,
//                cal.get(Calendar.YEAR),
//                cal.get(Calendar.MONTH),
//                cal.get(Calendar.DAY_OF_MONTH)).show()
//        }


        binding.viewMeetingOutlinedTextFieldMeetingContact.editText?.onFocusChangeListener =
            View.OnFocusChangeListener { _, hasFocus -> if (!hasFocus) validateFields(binding)}

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
            if (!b) validateFields(binding)
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
            if (!b) validateFields(binding)
        }

        binding.viewMeetingEditMeetingButton.setOnClickListener {
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

            val eventID = CalendarUtil.updateEvent(
                requireActivity(),
                eventID,
                meetingName,
                meetingContact,
                meetingLocation,
                cal,
                meetingNotes,
                prevContactEmail
            )

            if (eventID >= 0){
                setFragmentResult("requestKey", bundleOf("refreshMeetings" to true))
                findNavController().popBackStack()
            }
        }

        binding.viewMeetingDeleteMeetingButton.setOnClickListener {
            val success = CalendarUtil.deleteEvent(requireActivity(), eventID!!)
            if (success){
                setFragmentResult("requestKey", bundleOf("refreshMeetings" to true))
                findNavController().popBackStack()
            }
            else{
                Toast.makeText(activity,"Failed to delete meeting, please try again.",Toast.LENGTH_SHORT).show()
            }

        }

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun validateFields(binding: FragmentViewMeetingBinding): Boolean {
        val emailField = binding.viewMeetingOutlinedTextFieldMeetingContact
        val dateField = binding.viewMeetingOutlinedTextFieldMeetingDate
        val timeField = binding.viewMeetingOutlinedTextFieldMeetingTime

        var valid = true

        if (!Util.isValidEmail(emailField.editText?.text.toString())) {
            emailField.isErrorEnabled = true
            emailField.error = "Invalid email"
            valid = false
        } else {
            emailField.error = null
            emailField.isErrorEnabled = false
        }

        if (!Util.isValidDate(dateField.editText?.text.toString())) {
            dateField.isErrorEnabled = true
            dateField.error = "Invalid date"
            valid = false
        } else {
            dateField.error = null
            dateField.isErrorEnabled = false
        }

        if (!Util.isValidTime(timeField.editText?.text.toString())) {
            timeField.isErrorEnabled = true
            timeField.error = "Invalid time"
            valid = false
        } else {
            timeField.error = null
            timeField.isErrorEnabled = false
        }


        return valid
    }
}