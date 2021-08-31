package com.cloudsurfers.crm

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.provider.ContactsContract
import androidx.annotation.RequiresApi
import kotlinx.serialization.Serializable

/** Helper class to store all the relevant contact information so that it can be passed between
 * components of the App without need of parsing the cursors multiple times.
 */
@Serializable
class Contact(){
    var id: String? = null
    var name: String? = null
    var phone: String? = null
    var email: String? = null
    var note: String? = null

    public constructor (i: String?, nm: String?, p: String?, e: String?, nt: String?) : this() {
        id = i
        name = nm
        phone = p
        email = e
        note = nt
    }

    constructor(i:String?) : this() {
        id = i
    }

    //Additional functionality provided by companion object
    companion object {

        //Checks that contacts are accessible in the relevant activity
        @RequiresApi(Build.VERSION_CODES.M)
        fun checkPermissions(activity: Activity){
            if (activity.checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED){
                val requestCode = 1;
                activity.requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), requestCode)
            }
            return
        }

        //Provides an immutable (read-only) list of all contacts
        @RequiresApi(Build.VERSION_CODES.M)
        fun readContacts(activity: Activity): List<Contact>? {
            checkPermissions(activity)

            val tempMap = hashMapOf<String, Contact>()

            // A "projection" defines the columns that will be returned for each row
            val mProjection: Array<String> = arrayOf(
                ContactsContract.Data.RAW_CONTACT_ID,    // Contract class constant for the _ID column name
                ContactsContract.Data.MIMETYPE,
                ContactsContract.Data.DATA1
            )

            val mCursor = activity.contentResolver.query(
                ContactsContract.Data.CONTENT_URI,
                mProjection,
                null,
                emptyArray<String>(),
                null
            )

            // Some providers return null if an error occurs, others throw an exception
            when (mCursor?.count) {
                null -> {
                    android.util.Log.e("ContactUtils", "Error connecting to contacts")
                    return null
                }
                0 -> {
                    android.util.Log.e("ContactUtils", "Contact not found")
                    return null
                    //TODO: notify user that search was unsuccessful
                }
                else -> {
                    mCursor.moveToFirst()

                    for (i in 0 until mCursor.count) {
                        mCursor.moveToPosition(i)

                        var c: Contact?
                        if (tempMap.containsKey(mCursor.getString(0))){
                            c = tempMap[mCursor.getString(0)]
                        } else {
                            c = Contact()
                            tempMap[mCursor.getString(0)] = c
                        }


                        when (mCursor.getString(1)) {
                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE ->
                                c?.phone = mCursor.getString(2) ?: "Phone not found"
                            ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE ->
                                c?.email = mCursor.getString(2) ?: "Email not found"
                            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE ->
                                c?.name = mCursor.getString(2) ?: "Name not found"
                            ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE ->
                                c?.note = mCursor.getString(2) ?: "Note not found"
                        }
                    }
                }
            }

            return tempMap.values.toList()
        }

        //Queries for contact information using the name of the contact
        @RequiresApi(Build.VERSION_CODES.M)
        fun readContact(name: String?, activity: Activity): Contact{
            checkPermissions(activity)

            var c: Contact = Contact()

            // A "projection" defines the columns that will be returned for each row
            val mProjection: Array<String> = arrayOf(
                ContactsContract.Data.RAW_CONTACT_ID,    // Contract class constant for the _ID column name
                ContactsContract.Data.MIMETYPE,
                ContactsContract.Data.DATA1
            )

            val mCursor = activity.contentResolver.query(
                ContactsContract.Data.CONTENT_URI,
                mProjection,
                null,
                emptyArray<String>(),
                null
            )

            // Some providers return null if an error occurs, others throw an exception
            when (mCursor?.count) {
                null -> {
                    android.util.Log.e("ContactUtils", "Error connecting to contacts")
                }
                0 -> {
                    android.util.Log.e("ContactUtils", "Contact not found")
                    //TODO: notify user that search was unsuccessful
                }
                else -> {
                    mCursor.moveToFirst()

                    for (i in 0..mCursor.count - 1) {
                        mCursor.moveToPosition(i)
                        when (mCursor.getString(1)) {
                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE ->
                                c.phone = mCursor.getString(2) ?: "Phone not found"
                            ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE ->
                                c.email = mCursor.getString(2) ?: "Email not found"
                            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE ->
                                c.name = mCursor.getString(2) ?: "Name not found"
                            ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE ->
                                c.note = mCursor.getString(2) ?: "Note not found"
                        }
                    }
                }
            }

            return c
        }
    }

}

//class Contact(name: String?, phone: String?, email: String?, notes: String?)