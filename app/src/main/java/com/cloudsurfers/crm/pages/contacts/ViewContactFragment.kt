package com.cloudsurfers.crm.pages.contacts

import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
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


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "name"
private const val ARG_PARAM2 = "email"
private const val ARG_PARAM3 = "mobile"
private const val ARG_PARAM4 = "notes"
/**
 * A simple [Fragment] subclass.
 * Use the [ViewContactFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ViewContactFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var name: String? = null
    private var email: String? = null
    private var mobile: String? = null
    private var notes: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            name = it.getString(ARG_PARAM1)
            email = it.getString(ARG_PARAM2)
            mobile = it.getString(ARG_PARAM3)
            notes = it.getString(ARG_PARAM4)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val binding = FragmentViewContactBinding.inflate(layoutInflater, container, false);
        requireActivity().findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar).title = name
        val sendEmailButton = binding.viewContactSendEmailButton
        sendEmailButton.setOnClickListener(){
            val intent: Intent = ComposeEmail.getSendEmailIntent(email!!)
            startActivity(Intent.createChooser(intent, "Send mail using..."))
        }


        // Create meeting button intent
        val createMeetingButton = binding.viewContactCreateMeetingButton
        createMeetingButton.setOnClickListener(){
            val intent: Intent = CalendarUtil.getInsertEventIntent(
                title = "",
                contactEmail = email!!,
                location = "",
                dateTime = Calendar.getInstance(),
                desc = ""
            )
            startActivity(intent)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.view_contact_name_text).text = name
        view.findViewById<TextView>(R.id.view_contact_email_text).text = email
        view.findViewById<TextView>(R.id.view_contact_mobile_text).text = mobile
        view.findViewById<TextView>(R.id.view_contact_notes_text).text = notes


    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ViewContactFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(name: String, email: String, mobile: String, notes: String) =
            ViewContactFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, name)
                    putString(ARG_PARAM2, email)
                    putString(ARG_PARAM3, mobile)
                    putString(ARG_PARAM4, notes)
                }
            }
    }
}