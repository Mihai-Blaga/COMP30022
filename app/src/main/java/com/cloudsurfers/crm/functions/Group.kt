package com.cloudsurfers.crm.functions

import android.app.Activity
import android.content.ContentValues
import android.os.Build
import android.provider.ContactsContract
import androidx.annotation.RequiresApi
import java.util.stream.Collectors

class Group(var id: String?, var title: String?) {

    fun addContact(activity: Activity, contact: Contact){
        id?.let { addContactToGroup(activity, contact, it) }
    }

    companion object {
        private var groups: ArrayList<Group> = arrayListOf()
        private var accountName: String? = null
        private var accountType: String? = null
        private var groupIdLookup: HashMap<String, String> = hashMapOf()
        private var groupContactLookup: HashMap<String, ArrayList<Contact>> = hashMapOf()

        @RequiresApi(Build.VERSION_CODES.N)
        fun createNewGroup(activity: Activity, title: String): Group? {
            if (accountName == null) {
                refresh(activity)
            }
            val group = getGroupByTitle(title, activity)
            if (group != null) {
                return group
            }

            val groupValues = ContentValues()
            groupValues.put(ContactsContract.Groups.TITLE, title)
            groupValues.put(ContactsContract.Groups.GROUP_VISIBLE, 1)
            groupValues.put(ContactsContract.Groups.ACCOUNT_TYPE, accountType)
            groupValues.put(ContactsContract.Groups.ACCOUNT_NAME, accountName)
            groupValues.put(ContactsContract.Groups.SHOULD_SYNC, true)

            // insert into the table
            activity.contentResolver.insert(ContactsContract.Groups.CONTENT_URI, groupValues)
            refresh(activity)
            return getGroupByTitle(title, activity)
        }

        @RequiresApi(Build.VERSION_CODES.N)
        fun getGroupByTitle(title: String, activity: Activity): Group? {
            val allGroups = groups
            if (allGroups.isEmpty()) {
                refresh(activity)
            }
            for (i in allGroups) {
                if (i.title == title) {
                    return i
                }
            }
            return null
        }

        fun addContactToGroup(activity: Activity, contact: Contact, id: String){
            val values = ContentValues()
            values.put(ContactsContract.Data.RAW_CONTACT_ID, contact.id)
            values.put(ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID, id)
            values.put(
                ContactsContract.Data.MIMETYPE,
                ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE
            )

            activity.contentResolver.insert(ContactsContract.Data.CONTENT_URI, values)
        }


        @RequiresApi(Build.VERSION_CODES.N)
        fun getAllGroupNames(activity: Activity): ArrayList<String>{
            if (groupIdLookup.isEmpty()) {
                refresh(activity)
            }
            // remove tags which do not have any associated contacts
            val nonEmptyTags = arrayListOf<String>()
            for(tag in groupIdLookup.keys){
                val id = groupIdLookup[tag]
                if (groupContactLookup.containsKey(id) && !groupContactLookup[id]?.isEmpty()!!){
                    nonEmptyTags.add(tag)
                }
            }

            return nonEmptyTags

        }

        @RequiresApi(Build.VERSION_CODES.N)
        fun getContactsByGroupName(activity: Activity, title: String): ArrayList<Contact> {
            // if there are no contact lookups available, fetch them
            if (groupContactLookup.isEmpty()) {
                refresh(activity)
            }
            val groupId = groupIdLookup[title]
            if (groupContactLookup.containsKey(groupId)) {
                return groupContactLookup[groupId]!!
            }
            return arrayListOf()
        }

        @RequiresApi(Build.VERSION_CODES.N)
        fun getContactsByGroupName(
            activity: Activity,
            titles: ArrayList<String>
        ): ArrayList<Contact> {
            val intersectionContactList = arrayListOf<Contact>()

            // create a list of all the contacts that are present in any of the lists
            val unionContactList = arrayListOf<Contact>()
            for (title in titles) {
                for (contact in getContactsByGroupName(activity, title)) {
                    if (!unionContactList.stream().anyMatch { it.id == contact.id }) {
                        unionContactList.add(contact)
                    }
                }
            }

            // create an intersection set by only adding the contacts in all of tags
            for (contact in unionContactList) {
                var presentInAll = true
                for (title in titles) {
                    val contactsInTitle = getContactsByGroupName(activity, title)
                    if (!contactsInTitle.stream().anyMatch({ it.id == contact.id })) {
                        presentInAll = false
                        break
                    }
                }
                if (presentInAll) {
                    intersectionContactList.add(contact)
                }
            }

            return intersectionContactList
        }

        @RequiresApi(Build.VERSION_CODES.N)
        fun refresh(activity: Activity) {
            fetchAllGroupNames(activity)
            fetchContactsByGroups(activity)
        }

        @RequiresApi(Build.VERSION_CODES.N)
        private fun fetchContactsByGroups(activity: Activity) {
            // reset the hashmap
            groupContactLookup = hashMapOf()
            // read all the contacts
            val contactsList = Contact.readContacts(activity)

            // go through each contact and add their ids
            for (contact in contactsList!!) {
                for (groupId in contact.groups!!) {
                    if (!groupContactLookup.containsKey(groupId)) {
                        groupContactLookup[groupId] = arrayListOf()
                    }
                    var alreadyPresent = false
                    if (groupContactLookup[groupId]?.stream()?.anyMatch { it.id == contact.id }!!) {
                        alreadyPresent = true
                    }
                    if (!alreadyPresent) {
                        groupContactLookup[groupId]?.add(contact)
                    }
                }
            }
        }

        @RequiresApi(Build.VERSION_CODES.N)
        private fun fetchAllGroupNames(activity: Activity) {
            // reset the lookup and groups list
            groupIdLookup = hashMapOf()
            groups = arrayListOf()

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
                }
                0 -> {
                    android.util.Log.e("ContactUtils", "Contact Groups not found")
                }
                else -> {
                    uriCursor.moveToFirst()
                    groups = arrayListOf()
                    for (i in 0 until uriCursor.count) {
                        uriCursor.moveToPosition(i)
                        val group = Group((uriCursor.getInt(0)).toString(), uriCursor.getString(1))
                        if (group.id != null && group.title != null) {
                            if (groupIdLookup.containsKey(group.id)) {
                                groupIdLookup.replace(group.title!!, group.id!!)
                            } else {
                                groupIdLookup[group.title!!] = group.id!!
                            }
                        }
                        groups.add(group)
                        group.title?.let {
                            if (!groupNames.contains(it)) {
                                groupNames.add(it)
                            }
                        }
                        accountName = uriCursor.getString(2)
                        accountType = uriCursor.getString(3)
                    }
                }
            }
            uriCursor?.close()
        }

    }
}