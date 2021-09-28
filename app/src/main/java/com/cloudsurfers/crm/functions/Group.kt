package com.cloudsurfers.crm.functions

import android.app.Activity
import android.content.ContentValues
import android.os.Build
import android.provider.ContactsContract
import androidx.annotation.RequiresApi

class Group(var id: String?, var title: String?) {

    fun addContact(activity: Activity, contact: Contact){
        val values = ContentValues()
        values.put(ContactsContract.Data.RAW_CONTACT_ID, contact.id)
        values.put(ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID, id)
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE)

        activity.contentResolver.insert(ContactsContract.Data.CONTENT_URI, values)
    }

    companion object {
        var groups: ArrayList<Group> = arrayListOf()
        var accountName: String? = null
        var accountType: String? = null
        var groupLookup: HashMap<String,String> = hashMapOf()

        @RequiresApi(Build.VERSION_CODES.N)
        fun createNewGroup(activity: Activity, title: String): Group? {
            if(accountName == null){
                getAllGroupNames(activity)
            }
            for (i in groups){
                if (i.title == title){
                    return i
                }
            }

            val groupValues = ContentValues()
            groupValues.put(ContactsContract.Groups.TITLE, title)
            groupValues.put(ContactsContract.Groups.GROUP_VISIBLE, 1)
            groupValues.put(ContactsContract.Groups.ACCOUNT_TYPE, accountType)
            groupValues.put(ContactsContract.Groups.ACCOUNT_NAME, accountName)
            groupValues.put(ContactsContract.Groups.SHOULD_SYNC, true)

            // insert into the table
            activity.contentResolver.insert(ContactsContract.Groups.CONTENT_URI, groupValues)
            getAllGroupNames(activity)
            val allGroups = groups
            for (i in allGroups){
                if (i.title == title){
                    return i
                }
            }
            return null
        }

        @RequiresApi(Build.VERSION_CODES.N)
        fun deleteGroup(activity: Activity, group: Group){
            group.id?.let { deleteGroup(activity, it) }
        }

        @RequiresApi(Build.VERSION_CODES.N)
        fun deleteGroup(activity: Activity, id: String){
            activity.contentResolver.delete(
                ContactsContract.Groups.CONTENT_URI,
                ContactsContract.Groups._ID +"="+id, null)
            getAllGroupNames(activity)
        }

        @RequiresApi(Build.VERSION_CODES.N)
        fun getAllGroupNames(activity: Activity): ArrayList<String>? {
            // reset the lookup
            groupLookup = hashMapOf()

            val projection = arrayOf(
                ContactsContract.Groups._ID,
                ContactsContract.Groups.TITLE,
                ContactsContract.Groups.ACCOUNT_NAME,
                ContactsContract.Groups.ACCOUNT_TYPE
            )
            val groupNames = arrayListOf<String>()
            val uriCursor = activity.contentResolver.query(
                ContactsContract.Groups.CONTENT_URI,
                projection,
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
                    android.util.Log.e("ContactUtils", "Contact Groups not found")
                    return null
                }
                else -> {
                    //processing can be optimised but this is the simplest and most readable
                    uriCursor.moveToFirst()
                    groups = arrayListOf()
                    for (i in 0 until uriCursor.count) {
                        uriCursor.moveToPosition(i)
                        val group = Group((uriCursor.getInt(0)).toString(), uriCursor.getString(1))
                        if (group.id != null && group.title!=null){
                            if(groupLookup.containsKey(group.id)){
                                groupLookup.replace(group.title!!, group.id!!)
                            }
                            else{
                                groupLookup[group.title!!] = group.id!!
                            }
                        }
                        groups.add(group)
                        group.title?.let { if(!groupNames.contains(it)){groupNames.add(it)} }
                        accountName = uriCursor.getString(2)
                        accountType = uriCursor.getString(3)
                    }
                }
            }
            uriCursor.close()
            return groupNames
        }

        @RequiresApi(Build.VERSION_CODES.N)
        fun getContactsFromGroupName(activity: Activity, title: String, redoGroupLookup:Boolean): ArrayList<Contact>? {
            if (!groupLookup.containsKey(title)){
                return null
            }
            return groupLookup[title]?.let { Contact.readContactsFromGroup(it, activity, redoGroupLookup) }
        }

        @RequiresApi(Build.VERSION_CODES.N)
        fun getContactsFromGroupName(activity: Activity, title: String): ArrayList<Contact>? {
           return getContactsFromGroupName(activity,title,false)
        }
    }
}