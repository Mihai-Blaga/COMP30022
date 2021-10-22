package com.cloudsurfers.crm.pages.contacts

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.cloudsurfers.crm.databinding.FragmentAddNewContactBinding
import com.cloudsurfers.crm.functions.Contact
import com.cloudsurfers.crm.functions.Util
import com.cloudsurfers.crm.pages.meetings.AddMeetingFragment
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.chip.Chip
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AddMeetingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddContactFragment : Fragment() {
    private var tags: ArrayList<String> = ArrayList<String>()

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
    ): View {

        val binding = FragmentAddNewContactBinding.inflate(layoutInflater, container, false)

        // Add tags to the chip group
        binding.addContactChipGroup.apply {
            for (tag: String in tags) {
                addNewChip(tag, this)
            }
        }

        // Set listener to edit text view
        binding.addContactChipEditText.apply {
            addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                // Convert text to tag 1 second after finishing typing
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    handler.removeCallbacksAndMessages(null)
                    handler.postDelayed({
                        if (!text.isNullOrEmpty()) {
                            addNewChip(text.toString(), binding.addContactChipGroup)
                            setText("")
                        }
                    },
                        1000)
                }

                override fun afterTextChanged(p0: Editable?) {}
            })
        }

        // Data validation when losing focus
        binding.outlinedTextFieldEmail.editText?.onFocusChangeListener =
            View.OnFocusChangeListener { _, hasFocus -> if (!hasFocus) validateFields(binding)}
        binding.outlinedTextFieldMobile.editText?.onFocusChangeListener =
            View.OnFocusChangeListener { _, hasFocus -> if (!hasFocus) validateFields(binding)}

        binding.addContactButton.setOnClickListener {
            val name = binding.outlinedTextFieldName.editText?.text.toString()
            val email = binding.outlinedTextFieldEmail.editText?.text.toString()
            val phone = binding.outlinedTextFieldMobile.editText?.text.toString()
            val notes = binding.outlinedTextFieldNotes.editText?.text.toString()
            val success = Contact.createContact(requireActivity(), name, phone, email, notes, getTags(binding.addContactChipGroup))
            if (validateFields(binding))
                if (success && validateFields(binding)){
                    setFragmentResult("requestKey", bundleOf("refreshContacts" to true))
                    findNavController().popBackStack()
                }
                else{
                    val text = "Failed to add Contact. Please retry!"
                    val duration = Toast.LENGTH_SHORT

                    val toast = Toast.makeText(requireActivity(), text, duration)
                    toast.show()
                }
        }

        return binding.root
    }



    private fun validateFields(binding: FragmentAddNewContactBinding): Boolean {
        val emailField = binding.outlinedTextFieldEmail
        val phoneField = binding.outlinedTextFieldMobile
        var valid = true

        if (!Util.isValidEmail(emailField.editText?.text.toString())) {
            emailField.isErrorEnabled = true
            emailField.error = "Invalid email"
            valid = false
        } else {
            emailField.error = null
            emailField.isErrorEnabled = false
        }
        if (!Util.isValidPhoneNumber(phoneField.editText?.text.toString())) {
            phoneField.isErrorEnabled = true
            phoneField.error = "Invalid phone number"
            valid = false
        } else {
            phoneField.error = null
            phoneField.isErrorEnabled = false
        }

        return valid
    }

    private fun getTags(chipGroup: FlexboxLayout): ArrayList<String> {
        val tags = ArrayList<String>()
        for (child: View in chipGroup.children) {
            if (child is Chip)
                tags.add(child.text as String)
        }
        return tags
    }

    // Adds a chip with the desired string to the chipGroup
    private fun addNewChip(tag: String, chipGroup: FlexboxLayout) {
        val chip = Chip(context)
        chip.text = tag
        chip.isCloseIconVisible = true
        chip.isClickable = true
        chip.gravity = Gravity.CENTER_VERTICAL
        chip.isCheckable = false

        chipGroup.addView(chip as View, chipGroup.childCount - 1)
        chip.setOnCloseIconClickListener { chipGroup.removeView(chip as View) }
    }
}