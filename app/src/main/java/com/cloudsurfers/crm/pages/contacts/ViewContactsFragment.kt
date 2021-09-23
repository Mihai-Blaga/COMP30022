package com.cloudsurfers.crm.pages.contacts

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cloudsurfers.crm.R
import com.cloudsurfers.crm.functions.Contact
import com.cloudsurfers.crm.pages.search.SearchableActivity
import com.google.android.material.bottomnavigation.BottomNavigationView


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ViewContactsList.newInstance] factory method to
 * create an instance of this fragment.
 */
class ViewContactsList : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    //Launches popup requesting access to reading contacts
    private val requestContactPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                android.util.Log.i("ViewContactsFragment", "Read permission granted")

                //Refresh page to enable viewing contact functionality
                this.findNavController().navigate(R.id.viewContactsList)
            } else {
                android.util.Log.w("ViewContactsFragment", "Read permission denied")
            }
        }

    //Used to check if reading contact permission is granted. Otherwise displays permission request
    //popup.
    //Returns boolean on whether permission was granted
    private fun requestPermission(activity: Activity): Boolean{
        val per = Manifest.permission.READ_CONTACTS
        when {
            ContextCompat.checkSelfPermission(
                activity,
                per
            ) == PackageManager.PERMISSION_GRANTED -> {
                return true
            }
            shouldShowRequestPermissionRationale(per) -> {
                //TODO: Explain to user why permission is needed
                requestContactPermissionLauncher.launch(per)
            }
            else -> {
                requestContactPermissionLauncher.launch(per)
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_view_contacts_list, container, false)
        val activity: Activity = activity as Activity

        var contactList: ArrayList<Contact> = ArrayList()

        if (requestPermission(activity)) {
            contactList = Contact.readContacts(activity) as ArrayList<Contact>
        }

        // Configure list view
        view.findViewById<RecyclerView>(R.id.view_contacts_list_recycler_view).apply {
            adapter = ViewContactsAdapter(contactList)
            layoutManager = LinearLayoutManager(activity)
        }

        // Configure search button
        view.findViewById<Button>(R.id.view_contacts_list_search_button).setOnClickListener {
            val searchIntent =  Intent(activity, SearchableActivity::class.java)
            startActivity(searchIntent)
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
         * @return A new instance of fragment ViewContactsList.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ViewContactsList().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}