package com.cloudsurfers.crm

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import com.cloudsurfers.crm.databinding.AddNewMeetingBinding
import java.util.*

class AddMeetingActivity : AppCompatActivity() {
    private lateinit var binding : AddNewMeetingBinding
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AddNewMeetingBinding.inflate(layoutInflater);
        setContentView(binding.root);

        val cal = Calendar.getInstance()

        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val myFormat = "dd.MM.yyyy" // mention the format you need
            val sdf = SimpleDateFormat(myFormat, Locale.US)
            binding.outlinedTextFieldMeetingDateTime.editText?.setText(sdf.format(cal.time))
        }

        binding.outlinedTextFieldMeetingDateTime.editText?.setOnClickListener(){
            DatePickerDialog(this, dateSetListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.addMeetingButton.setOnClickListener(){
            val meetingName = binding.outlinedTextFieldMeetingName.editText?.text.toString()
            val meetingContact  = binding.outlinedTextFieldMeetingContact.editText?.text.toString()
            val meetingLocation = binding.outlinedTextFieldMeetingLocation.editText?.text.toString()
            val meetingTime = binding.outlinedTextFieldMeetingDateTime.editText?.text.toString()
            val meetingNotes = binding.outlinedTextFieldMeetingNotes.editText?.text.toString()

            val intent = CalendarUtil.getInsertEventIntent(meetingName, meetingContact, meetingLocation, Calendar.getInstance().time, meetingNotes)
            startActivity(intent)
        }
    }

}