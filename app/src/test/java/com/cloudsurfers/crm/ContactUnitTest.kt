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
    fun verifyTestsWork(){
        assert(true)
    }

    @Test
    fun readContacts_correctOutputWithValidData(){

    }


    @Test
    fun readContact_creatingContact(){
        val lookupArray = arrayOf(
            ContactsContract.Data.RAW_CONTACT_ID,    // Contract class constant for the _ID column name
            ContactsContract.Data.MIMETYPE,
            ContactsContract.Data.DATA1,
            ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID
        )

        val exampleCursor = MatrixCursor(lookupArray, 4)

        exampleCursor.addRow(arrayOf("1", ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE, "0411111111", 1))
        exampleCursor.addRow(arrayOf("1", ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE, "name@email.com", 1))
        exampleCursor.addRow(arrayOf("1", ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE, "John Doe", 1))
        exampleCursor.addRow(arrayOf("1", ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE, "Note", 1))

        val c = Contact("1")

        `when`(a.contentResolver).thenReturn(resolver)

        `when`(resolver.query(ContactsContract.Data.CONTENT_URI, lookupArray, null, emptyArray<String>(), null))
            .thenReturn(exampleCursor)

        val out = readContact(c, a)

        assertNotEquals(out, null)
    }

    /*
    @Test
    fun readPhone(){
        //Step 1: Create data you want to return and put it into a matrix cursor
        //In this case I am mocking getting phone numbers from Contacts Provider
        val exampleData = arrayOf("(979) 267-8509")
        val examleProjection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
        val matrixCursor = MatrixCursor(examleProjection)
        matrixCursor.addRow(exampleData)

        //Step 2: Create a stub content provider and add the matrix cursor as the expected result of the query
        val mockProvider = HashMapMockContentProvider()
        mockProvider.addQueryResult(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            matrixCursor
        )

        //Step 3: Create a mock resolver and add the content provider.
        val mockResolver = MockContentResolver()
        mockResolver.addProvider(
            ContactsContract.AUTHORITY /*Needs to be the same as the authority of the provider you are mocking */,
            mockProvider
        )

        //Step 4: Add the mock resolver to the mock context
        val mockContext = ContextWithMockContentResolver(super.getContext())
        mockContext.setContentResolver(mockResolver)

        //Example Test
        val underTest = ExampleClassUnderTest()
        val result: String = underTest.getPhoneNumbers(mockContext)
        assertEquals("(979) 267-8509", result)
    }*/
}