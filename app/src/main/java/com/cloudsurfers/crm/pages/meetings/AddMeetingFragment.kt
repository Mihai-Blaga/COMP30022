package com.cloudsurfers.crm.pages.meetings

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.cloudsurfers.crm.functions.CalendarUtil
import com.cloudsurfers.crm.databinding.FragmentAddNewMeetingBinding
import com.cloudsurfers.crm.functions.Contact
import com.cloudsurfers.crm.functions.Util
import java.util.*
import android.widget.ArrayAdapter
import com.cloudsurfers.crm.R


class AddMeetingFragment : Fragment() {
    private var contactEmails: ArrayList<String> = ArrayList<String>()


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentAddNewMeetingBinding.inflate(layoutInflater, container, false)
        contactEmails = Contact.readContacts(requireActivity()).map { it.email } as ArrayList<String>
        println(contactEmails)
//        binding.outlinedTextFieldMeetingName.editText?.setText("Someone's Meeting")
        // Stores the date and time that can be changed by the user
        val cal = Calendar.getInstance()

        // Date and Time Dialog Selectors
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val myFormat = "dd.MM.yyyy" // mention the format you need
            val sdf = SimpleDateFormat(myFormat, Locale.US)
            binding.outlinedTextFieldMeetingDate.editText?.setText(sdf.format(cal.time))
        }

        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
            cal.set(Calendar.MINUTE, minute)
            val minuteStr = minute.toString().padStart(2, '0')
            val hourStr = hourOfDay.toString().padStart(2, '0')
            val timeStr = "${hourStr}:${minuteStr}"
            binding.outlinedTextFieldMeetingTime.editText?.setText(timeStr)
        }

        // On Click Listeners
//        binding.outlinedTextFieldMeetingDate.editText?.setOnClickListener(){
//            DatePickerDialog(requireActivity(), dateSetListener,
//                cal.get(Calendar.YEAR),
//                cal.get(Calendar.MONTH),
//                cal.get(Calendar.DAY_OF_MONTH)).show()
//        }

        // Configure autocomplete text edit view for contact emails
        binding.addMeetingAutocompleteEmailTextField.setAdapter(
            ArrayAdapter(
                requireActivity(),
                R.layout.autocomplete_list_item, contactEmails
            )
        )
        binding.addMeetingAutocompleteEmailTextField.onFocusChangeListener =
            View.OnFocusChangeListener { _, hasFocus -> if (!hasFocus) validateFields(binding)}

        binding.outlinedTextFieldMeetingDate.editText?.setOnFocusChangeListener { v, b ->
            // This line prevents keyboard from showing
            Util.hideKeyboard(v, requireContext())
            if (b){
                DatePickerDialog(requireActivity(), dateSetListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)).show()
            }
            if (!b) validateFields(binding)
        }

        // Data validation when losing focus
        binding.outlinedTextFieldMeetingTime.editText?.setOnFocusChangeListener { v, b ->
            // This line prevents keyboard from showing
            Util.hideKeyboard(v, requireContext())
            if (b){
                TimePickerDialog(requireActivity(), timeSetListener,
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    false).show()
            }
            if (!b) validateFields(binding)
        }

        binding.addMeetingButton.setOnClickListener {
            val meetingName = binding.outlinedTextFieldMeetingName.editText?.text.toString()
            val meetingContact  = binding.addMeetingAutocompleteEmailTextField.text.toString()
            val meetingLocation = binding.outlinedTextFieldMeetingLocation.editText?.text.toString()
            val meetingNotes = binding.outlinedTextFieldMeetingNotes.editText?.text.toString()

//            val intent = CalendarUtil.getInsertEventIntent(meetingName, meetingContact, meetingLocation, cal, meetingNotes)
//            startActivity(intent)
            if (validateFields(binding)) {
                val eventID = CalendarUtil.addEvent(requireActivity(), meetingName, meetingContact, meetingLocation, cal, meetingNotes)

                if (eventID >= 0) {
                    setFragmentResult("requestKey", bundleOf("refreshMeetings" to true))
                    findNavController().popBackStack()
                }
            }
        }


        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun validateFields(binding: FragmentAddNewMeetingBinding): Boolean {
        val emailField = binding.outlinedTextFieldMeetingContact
        val dateField = binding.outlinedTextFieldMeetingDate
        val timeField = binding.outlinedTextFieldMeetingTime
        val email = binding.addMeetingAutocompleteEmailTextField.text.toString()

        var valid = true
        if (email !in contactEmails) {
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
            if (dateField.editText?.text.toString().isNullOrEmpty())
                valid = false
            dateField.error = null
            dateField.isErrorEnabled = false
        }

        if (!Util.isValidTime(timeField.editText?.text.toString())) {
            timeField.isErrorEnabled = true
            timeField.error = "Invalid time"
            valid = false
        } else {
            if (timeField.editText?.text.toString().isNullOrEmpty())
                valid = false
            timeField.error = null
            timeField.isErrorEnabled = false
        }


        return valid
    }
}