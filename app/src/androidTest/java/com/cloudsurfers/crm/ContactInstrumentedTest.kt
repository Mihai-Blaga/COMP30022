package com.cloudsurfers.crm

import android.content.Intent
import android.provider.ContactsContract
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cloudsurfers.crm.functions.Contact
//import org.hamcrest.MatcherAssert.assertThat
import androidx.test.ext.truth.content.IntentSubject.assertThat
import com.cloudsurfers.crm.pages.main.MainActivity
import org.junit.Assert
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ContactInstrumentedTest {
    @Test
    fun newContactIntent_regularInput() {
        val name = "adam"
        val phone = "0411111111"
        val email = "adam@example.com"

        val intent: Intent = Contact.getCreateContact(name, phone, email)
        assertThat(intent).hasAction(Intent.ACTION_INSERT)

        val extras = intent.extras
        assert(extras!!.get(ContactsContract.Intents.Insert.NAME) == name)
        assert(extras.get(ContactsContract.Intents.Insert.PHONE) == phone)
        assert(extras.get(ContactsContract.Intents.Insert.EMAIL) == email)
    }
}