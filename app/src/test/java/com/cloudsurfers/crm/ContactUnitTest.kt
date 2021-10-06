package com.cloudsurfers.crm

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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.robolectric.Robolectric.setupActivity

class ContactUnitTest {

    @Test
    fun verifyTestsWork(){
        assert(true)
    }

    @Test
    fun readContacts_correctOutputWithValidData(){

    }


    @Test
    fun readContacts_testQuery(){
        val resolver = MockContentResolver()
        val c = resolver.query(ContactsContract.Data.CONTENT_URI, null, null, null, null)
        assertFalse(c!!.moveToFirst())
        assertTrue(c.getColumnIndex(ContactsContract.Data.RAW_CONTACT_ID) >=0)
        c.close()
    }
}