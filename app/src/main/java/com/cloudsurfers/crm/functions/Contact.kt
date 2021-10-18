package com.cloudsurfers.crm.functions

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.provider.ContactsContract
import androidx.annotation.RequiresApi
import android.net.Uri
import android.provider.CalendarContract

import android.provider.ContactsContract.CommonDataKinds.StructuredName

import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.content.*

import android.provider.ContactsContract.RawContacts
import android.util.Log
import com.cloudsurfers.crm.R
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import android.content.ContentResolver
import android.database.Cursor


/** Helper class to store all the relevant contact information so that it can be passed between
 * components of the App without need of parsing the cursors multiple times.
 */
class Contact() {
    var id: String? = null
    var name: String? = null
    var phone: String? = null
    var email: String? = null
    var note: String? = null
    var uri: String? = null
    var groups: ArrayList<String>? = arrayListOf()

    constructor (i: String?, nm: String?, p: String?, e: String?, nt: String?) : this() {
        id = i
        name = nm
        phone = p
        email = e
        note = nt
    }

    constructor(i: String?) : this() {
        id = i
    }

    override fun toString(): String {
        return "Contact{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", note='" + note + '\'' +
                ", uri=" + uri + '\'' +
                ", groups=" + groups.toString() +
                '}'
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getGroupNames(activity: Activity): ArrayList<String> {
        val groupNames = arrayListOf<String>()
        for (id in groups!!) {
            val name = Group.getGroupNameFromId(id, activity)
            if (name != "") {
                groupNames.add(name)
            }
        }
        return groupNames
    }

    //Additional functionality provided by companion object
    companion object {

        private var allContacts: HashSet<Contact> = hashSetOf()
        private var emailContacts: HashMap<String, Contact> = hashMapOf()

        fun refresh(activity: Activity) {
            allContacts = hashSetOf()
            emailContacts = hashMapOf()
            readAllContacts(activity)
        }

        @SuppressLint("Recycle")
        private fun readAllContacts(activity: Activity) {
            //lookup keys are preferred to RAW_CONTACT_IDs as these can change.
            val mQuery = arrayOf(
                ContactsContract.Data.RAW_CONTACT_ID,    // Contract class constant for the _ID column name
                ContactsContract.Data.MIMETYPE,
                ContactsContract.Data.DATA1,
                ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID
            )

            val mCursor = activity.contentResolver.query(
                ContactsContract.Data.CONTENT_URI,
                mQuery,
                null,
                emptyArray<String>(),
                null
            )
            val tempMap = hashMapOf<String,Contact>()
            when (mCursor?.count) {
                null -> {
                    Log.e("Contact.readContact", "Error connecting to contacts")
                    return
                }
                0 -> {
                    Log.e("Contact.readContact", "Contact not found")
                    return
                    //TODO: notify user that search was unsuccessful
                }
                else -> {
                    mCursor.moveToFirst()
                    for (i in 0 until mCursor.count) {
                        mCursor.moveToPosition(i)
                        val id = mCursor.getString(0)
                        val c :Contact
                        if (tempMap.containsKey(id)){
                            c = tempMap[id]!!
                        }
                        else{
                            c = Contact()
                            c.id = id
                        }
                        // skip over ids which have been visited
                        when (mCursor.getString(1)) {
                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE ->
                                c.phone = mCursor.getString(2) ?: "Phone not found"
                            ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE ->
                                c.email = mCursor.getString(2) ?: "Email not found"
                            StructuredName.CONTENT_ITEM_TYPE ->
                                c.name = mCursor.getString(2) ?: "Name not found"
                            ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE ->
                                c.note = mCursor.getString(2) ?: "Note not found"
                            ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE -> {
                                val group = mCursor.getString(2)
                                if (c.groups != null) {
                                    if (!c.groups!!.contains(group)) {
                                        c.groups!!.add(group)
                                    }
                                }
                                else{
                                    c.groups = arrayListOf()
                                    c.groups!!.add(group)
                                }
                            }
                        }
                        tempMap[c.id!!] = c
                    }
                }
            }
            mCursor?.close()
            for(c in tempMap.values) {
                allContacts.add(c)
                c.email?.let { emailContacts.put(it, c) }
            }
        }

        //Provides an immutable (read-only) list of all contacts
        @RequiresApi(Build.VERSION_CODES.N)
        fun readContacts(activity: Activity): List<Contact> {
            if (allContacts.isEmpty()) {
                refresh(activity)
            }
            return allContacts.toList()
        }

        //Queries for contact information using the lookup uri and last known id of said contact.
        @RequiresApi(Build.VERSION_CODES.M)
        fun readContact(c: Contact, activity: Activity): Contact {
            //TODO: check for empty c.id and c.uri
            val lookupUri = ContactsContract.Contacts.getLookupUri(c.id!!.toLong(), c.uri)
            val contactDataUri = Uri.withAppendedPath(
                lookupUri,
                ContactsContract.Contacts.Data.CONTENT_DIRECTORY
            )

            // A "projection" defines the columns that will be returned for each row
            val mProjection: Array<String> = arrayOf(
                ContactsContract.Data.RAW_CONTACT_ID,    // Contract class constant for the _ID column name
                ContactsContract.Data.MIMETYPE,
                ContactsContract.Data.DATA1,
                ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID
            )

            val mCursor = activity.contentResolver.query(
                contactDataUri,
                mProjection,
                null,
                emptyArray<String>(),
                null
            )

            // Some providers return null if an error occurs, others throw an exception
            when (mCursor?.count) {
                null -> {
                    Log.e("Contact.readContact", "Error connecting to contacts")
                }
                0 -> {
                    Log.e("Contact.readContact", "Contact not found")
                    //TODO: notify user that search was unsuccessful
                }
                else -> {
                    mCursor.moveToFirst()

                    for (i in 0 until mCursor.count) {
                        mCursor.moveToPosition(i)
                        when (mCursor.getString(1)) {
                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE ->
                                c.phone = mCursor.getString(2) ?: "Phone not found"
                            ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE ->

                                c.email = mCursor.getString(2) ?: "Email not found"
                            StructuredName.CONTENT_ITEM_TYPE ->
                                c.name = mCursor.getString(2) ?: "Name not found"
                            ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE ->
                                c.note = mCursor.getString(2) ?: "Note not found"
                            ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE -> {
                                val group = mCursor.getString(2)
                                if (c.groups != null) {
                                    if (!c.groups!!.contains(group)) {
                                        c.groups!!.add(group)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            mCursor?.close()
            return c
        }

        fun readContactFromEmail(email: String = "alex@example.com", activity: Activity): Contact? {
            if (emailContacts.isEmpty()) {
                refresh(activity)
            }
            return if (emailContacts.containsKey(email)) {
                emailContacts[email]
            } else {
                null
            }
        }

        fun getCreateContact(name: String, phone: String, email: String): Intent {
            val contactIntent = Intent(ContactsContract.Intents.Insert.ACTION)
            contactIntent.type = RawContacts.CONTENT_TYPE
            contactIntent
                .putExtra(
                    ContactsContract.Intents.Insert.NAME,
                    name
                ) // These just have to be strings
                .putExtra(ContactsContract.Intents.Insert.PHONE, phone)
                .putExtra(ContactsContract.Intents.Insert.EMAIL, email)
//                .putExtra(ContactsContract.Intents.Insert.EXTRA_DATA_SET , "idfk what this is for")

            return contactIntent
        }

        @RequiresApi(Build.VERSION_CODES.N)
        fun createContact(
            activity: Activity,
            name: String,
            phone: String,
            email: String,
            notes: String,
            tags: ArrayList<String>
        ): Boolean {
            if (activity.checkSelfPermission(Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                val requestCode = 1;
                activity.requestPermissions(
                    arrayOf(Manifest.permission.WRITE_CONTACTS),
                    requestCode
                );
            }
            if (activity.checkSelfPermission(Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
                val requestCode = 1
                activity.requestPermissions(arrayOf(Manifest.permission.GET_ACCOUNTS), requestCode);
            }

            val ops = ArrayList<ContentProviderOperation>()
            val rawContactInsertIndex = 0

            val accManager = AccountManager.get(activity)
            val accounts = accManager.accounts

            val sharedPref = activity.getSharedPreferences(
                activity.getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
            )
            val currEmail = sharedPref.getString("email", "")
            val account = accounts.filter {
                it.name == currEmail
            }[0]

            ops.add(
                ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
                    .withValue(RawContacts.ACCOUNT_TYPE, account.type)
                    .withValue(RawContacts.ACCOUNT_NAME, account.name)
                    .build()
            )

            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(
                        ContactsContract.Data.RAW_CONTACT_ID,
                        rawContactInsertIndex
                    )
                    .withValue(ContactsContract.Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(StructuredName.DISPLAY_NAME, name)
                    .build()
            )

            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(
                        ContactsContract.Data.RAW_CONTACT_ID,
                        rawContactInsertIndex
                    )
                    .withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                    )
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
                    .build()
            )

            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(
                        ContactsContract.Data.RAW_CONTACT_ID,
                        rawContactInsertIndex
                    )
                    .withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
                    )
                    .withValue(ContactsContract.CommonDataKinds.Email.DATA, email)
                    .build()
            )

            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(
                        ContactsContract.Data.RAW_CONTACT_ID,
                        rawContactInsertIndex
                    )
                    .withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE
                    )
                    .withValue(ContactsContract.CommonDataKinds.Note.NOTE, notes)
                    .build()
            )
            val c = Contact()
            c.name = name
            c.phone = phone
            c.email = email
            c.note = notes
            c.groups = arrayListOf()
            for (tag in tags) {

                var tagId: String? = Group.getGroupByTitle(tag, activity)?.id
                if (tagId == null) {
                    tagId = Group.createNewGroup(activity, tag)!!.id
                }
                if (tagId != null) {
                    c.groups!!.add(tagId)
                }
                ops.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(
                            ContactsContract.Data.RAW_CONTACT_ID,
                            rawContactInsertIndex
                        )
                        .withValue(
                            ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE
                        )

                        .withValue(
                            ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID,
                            tagId!!
                        )
                        .build()
                )
            }
            allContacts.add(c)
            emailContacts[c.email!!] = c
            try {
                var results = activity.contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
                Group.refresh(activity)
                refresh(activity)
                return ops.size == results.size
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return false;
        }

        fun deleteContact(activity: Activity, contactID: String): Boolean{
            val uriQuery = arrayOf(
                ContactsContract.Data.RAW_CONTACT_ID,
                ContactsContract.Contacts.LOOKUP_KEY
            )

            val cur = activity.contentResolver.query(
                ContactsContract.Data.CONTENT_URI,
                uriQuery,
                null,
                null,
                null
            )
            val cr: ContentResolver = activity.contentResolver
            while (cur!!.moveToNext()) {
                try {
                    if (contactID.equals(cur.getString(0))){
                        val lookupKey: String = cur.getString(1)
                        val uri = Uri.withAppendedPath(
                            ContactsContract.Contacts.CONTENT_LOOKUP_URI,
                            lookupKey
                        )
                        cr.delete(uri, null, null)
                        return true
                    }

                } catch (e: java.lang.Exception) {
                    println(e.stackTrace)
                }
            }
            return false
        }

    }

}