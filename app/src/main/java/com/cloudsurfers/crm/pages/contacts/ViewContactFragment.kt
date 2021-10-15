package com.cloudsurfers.crm.pages.contacts

import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.cloudsurfers.crm.R
import com.cloudsurfers.crm.functions.CalendarUtil
import com.cloudsurfers.crm.databinding.FragmentViewContactBinding
import com.cloudsurfers.crm.functions.ComposeEmail
import androidx.annotation.RequiresApi
import androidx.core.view.children
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.chip.Chip





// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "name"
private const val ARG_PARAM2 = "email"
private const val ARG_PARAM3 = "mobile"
private const val ARG_PARAM4 = "notes"
private const val ARG_PARAM5 = "tags"

class ViewContactFragment : Fragment() {
    private var name: String? = null
    private var email: String? = null
    private var mobile: String? = null
    private var notes: String? = null
    private var tags: ArrayList<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            name = it.getString(ARG_PARAM1)
            email = it.getString(ARG_PARAM2)
            mobile = it.getString(ARG_PARAM3)
            notes = it.getString(ARG_PARAM4)
            tags = it.getStringArrayList(ARG_PARAM5)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        val binding = FragmentViewContactBinding.inflate(layoutInflater, container, false)
        requireActivity().findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar).title = name
        val sendEmailButton = binding.viewContactSendEmailButton
        sendEmailButton.setOnClickListener {
            val intent: Intent = ComposeEmail.getSendEmailIntent(email!!)
            startActivity(Intent.createChooser(intent, "Send mail using..."))
        }

        // Create meeting button intent
        val createMeetingButton = binding.viewContactCreateMeetingButton
        createMeetingButton.setOnClickListener {
            val intent: Intent = CalendarUtil.getInsertEventIntent(
                title = "",
                contactEmail = email!!,
                location = "",
                dateTime = Calendar.getInstance(),
                desc = ""
            )
            startActivity(intent)
        }

        // Add tags to the chip group
        binding.viewContactChipGroup.apply {
            for (tag: String in tags!!) {
                addNewChip(tag, this)
            }
        }

        // Set listener to edit text view
        binding.viewContactChipEditText.apply {
            addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                // Convert text to tag 1 second after finishing typing
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    handler.removeCallbacksAndMessages(null)
                    handler.postDelayed({
                        if (!text.isNullOrEmpty()) {
                            addNewChip(text.toString(), binding.viewContactChipGroup)
                            setText("")
                        }
                    },
                    1000)
                }

                override fun afterTextChanged(p0: Editable?) {}
            })
        }


        return binding.root
    }

    // Retrieve an ArrayList<String> of selected tags
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.view_contact_name_text).text = name
        view.findViewById<TextView>(R.id.view_contact_email_text).text = email
        view.findViewById<TextView>(R.id.view_contact_mobile_text).text = mobile
        view.findViewById<TextView>(R.id.view_contact_notes_text).text = notes
    }
}