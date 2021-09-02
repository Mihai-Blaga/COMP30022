package com.cloudsurfers.crm

import android.Manifest
import android.app.Activity
import android.content.ContentUris
import android.content.pm.PackageManager
import android.os.Build
import android.provider.ContactsContract
import androidx.annotation.RequiresApi
import kotlinx.serialization.Serializable
import android.net.Uri

/** Helper class to store all the relevant contact information so that it can be passed between
 * components of the App without need of parsing the cursors multiple times.
 */
class Contact(){
    var id: String? = null
    var name: String? = null
    var phone: String? = null
    var email: String? = null
    var note: String? = null
    var uri: String? = null

    constructor (i: String?, nm: String?, p: String?, e: String?, nt: String?) : this() {
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
                val requestCode = 1
                activity.requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), requestCode)
            }
            return
        }

        //Provides an immutable (read-only) list of all contacts
        @RequiresApi(Build.VERSION_CODES.M)
        fun readContacts(activity: Activity): List<Contact>? {
            checkPermissions(activity)

            val tempMap = hashMapOf<String, Contact>()

            //lookup keys are preferred to RAW_CONTACT_IDs as these can change.
            val uriQuery = arrayOf(
                ContactsContract.Data.RAW_CONTACT_ID,
                ContactsContract.Contacts.LOOKUP_KEY
            )

            val uriCursor = activity.contentResolver.query(
                ContactsContract.Data.CONTENT_URI,
                uriQuery,
                null,
                emptyArray<String>(),
                null
            )

            when (uriCursor?.count) {
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
                    //processing can be optimised but this is the simplest and most readable
                    uriCursor.moveToFirst()

                    for (i in 0 until uriCursor.count) {
                        uriCursor.moveToPosition(i)

                        val c = Contact()
                        c.id = uriCursor.getString(0)
                        c.uri = uriCursor.getString(1)

                        tempMap[uriCursor.getString(0)] = readContact(c, activity)
                    }
                }
            }

            uriCursor.close()

            return tempMap.values.toList()
        }

        //Queries for contact information using the lookup uri and last known id of said contact.
        @RequiresApi(Build.VERSION_CODES.M)
        fun readContact(c: Contact, activity: Activity): Contact{
            checkPermissions(activity)

            //TODO: check for empty c.id and c.uri
            val lookupUri = ContactsContract.Contacts.getLookupUri(c.id!!.toLong(), c.uri)
            val contactDataUri = Uri.withAppendedPath(lookupUri,
                ContactsContract.Contacts.Data.CONTENT_DIRECTORY)

            // A "projection" defines the columns that will be returned for each row
            val mProjection: Array<String> = arrayOf(
                ContactsContract.Data.RAW_CONTACT_ID,    // Contract class constant for the _ID column name
                ContactsContract.Data.MIMETYPE,
                ContactsContract.Data.DATA1,
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
                    android.util.Log.e("Contact.readContact", "Error connecting to contacts")
                }
                0 -> {
                    android.util.Log.e("Contact.readContact", "Contact not found")
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
                            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE ->
                                c.name = mCursor.getString(2) ?: "Name not found"
                            ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE ->
                                c.note = mCursor.getString(2) ?: "Note not found"
                        }
                    }
                }
            }

            mCursor?.close()
            return c
        }

        fun readContactFromEmail(email: String, activity: Activity): Contact{
            var c = Contact()

            return c
        }

    }

}