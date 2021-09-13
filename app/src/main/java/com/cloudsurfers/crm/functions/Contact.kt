package com.cloudsurfers.crm.functions

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.ContactsContract
import androidx.annotation.RequiresApi
import android.net.Uri
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


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

        //Provides an immutable (read-only) list of all contacts
        @RequiresApi(Build.VERSION_CODES.M)
        fun readContacts(activity: Activity): List<Contact>? {
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
        fun readContact(c: Contact, activity: Activity): Contact {
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

        fun readContactFromEmail(email: String = "alex@example.com", activity: Activity): Contact {
            var c = Contact()

            val selectionClause = "${ContactsContract.Data.DATA1} = ?"
            val selectionArgs = arrayOf(email)

            val mProjection: Array<String> = arrayOf(
                ContactsContract.Data.RAW_CONTACT_ID,    // Contract class constant for the _ID column name
                ContactsContract.Data.MIMETYPE,
                ContactsContract.Data.DATA1,
                ContactsContract.Contacts.LOOKUP_KEY
            )

            val mCursor = activity.contentResolver.query(
                ContactsContract.Data.CONTENT_URI,
                mProjection,
                selectionClause,
                selectionArgs,
                null
            )

            when (mCursor?.count){
                null -> {}
                //TODO: let user know that no contact with this email was found
                0 -> {}
                else -> {
                    mCursor.moveToPosition(0)

                    c.id = mCursor.getString(0)
                    c.email = mCursor.getString(2)
                    c.uri = mCursor.getString(3)
                }
            }

            c = readContact(c, activity)

            return c
        }

        fun getCreateContact(name: String, phone: String, email: String): Intent {
            val contactIntent = Intent(ContactsContract.Intents.Insert.ACTION)
            contactIntent.type = ContactsContract.RawContacts.CONTENT_TYPE
            contactIntent
                .putExtra(ContactsContract.Intents.Insert.NAME , name) // These just have to be strings
                .putExtra(ContactsContract.Intents.Insert.PHONE , phone)
                .putExtra(ContactsContract.Intents.Insert.EMAIL , email)
//                .putExtra(ContactsContract.Intents.Insert.EXTRA_DATA_SET , "idfk what this is for")

            return contactIntent
        }

    }

}