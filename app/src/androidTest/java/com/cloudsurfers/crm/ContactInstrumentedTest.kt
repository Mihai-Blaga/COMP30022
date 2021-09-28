package com.cloudsurfers.crm

import android.content.Intent
import com.cloudsurfers.crm.functions.Contact
//import org.hamcrest.MatcherAssert.assertThat
import androidx.test.ext.truth.content.IntentSubject.assertThat
import org.junit.Assert
import org.junit.Test

class ContactInstrumentedTest {
    @Test
    fun newContactIntent_regularInput() {
        val name = "adam"
        val phone = "0411111111"
        val email = "adam@example.com"

        val intent: Intent = Contact.getCreateContact(name, phone, email)
        assertThat(intent).hasAction(Intent.ACTION_INSERT)

        val extras = intent.extras
        assert(extras?.get("name") == name)
        assert(extras?.get("phone") == phone)
        assert(extras?.get("email") == email)
    }
}