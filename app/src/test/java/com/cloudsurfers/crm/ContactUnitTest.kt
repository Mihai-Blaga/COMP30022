package com.cloudsurfers.crm

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import android.test.mock.MockContentResolver
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.cloudsurfers.crm.functions.Contact.Companion.readContacts
import com.cloudsurfers.crm.pages.main.MainActivity
import com.google.common.truth.Truth.assertThat
import org.junit.Assert
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.robolectric.Robolectric.setupActivity
import android.database.MatrixCursor
import android.test.mock.MockContentProvider
import com.cloudsurfers.crm.functions.Contact
import com.cloudsurfers.crm.functions.Contact.Companion.readContact
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.mockito.runners.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ContactUnitTest {
    @Mock
    private lateinit var a: Activity

    @Mock
    private lateinit var resolver: ContentResolver

    @Test
    fun readContact_creatingContact(){
        val lookupArray = arrayOf(
            ContactsContract.Data.RAW_CONTACT_ID,    // Contract class constant for the _ID column name
            ContactsContract.Data.MIMETYPE,
            ContactsContract.Data.DATA1,
            ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID
        )

        val exampleCursor = MatrixCursor(lookupArray)

        val types = arrayOf(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
            ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE,
            ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE)

        val values = arrayOf("0411111111",
            "name@email.com",
            "John Doe",
            "Note")

        for (i in 0 until 4){
            exampleCursor.newRow()
                .add(ContactsContract.Data.RAW_CONTACT_ID, "1")
                .add(ContactsContract.Data.MIMETYPE, types[i])
                .add(ContactsContract.Data.DATA1, values[i])
        }

        exampleCursor.addRow(arrayOf("1", ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE, "0411111111", 1))
        exampleCursor.addRow(arrayOf("1", ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE, "name@email.com", 1))
        exampleCursor.addRow(arrayOf("1", ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE, "John Doe", 1))
        exampleCursor.addRow(arrayOf("1", ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE, "Note", 1))

        assertEquals(4, exampleCursor.count)

        val c = Contact("1")

        `when`(a.contentResolver).thenReturn(resolver)

        `when`(resolver.query(ContactsContract.Data.CONTENT_URI, lookupArray, null, emptyArray<String>(), null))
            .thenReturn(exampleCursor)

        val out = readContact(c, a)

        assertNotEquals(out, null)
    }
}