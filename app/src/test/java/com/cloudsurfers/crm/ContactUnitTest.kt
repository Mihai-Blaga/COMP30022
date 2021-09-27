package com.cloudsurfers.crm

import android.content.Intent
import com.cloudsurfers.crm.functions.Contact
//import org.hamcrest.MatcherAssert.assertThat
import androidx.test.ext.truth.content.IntentSubject.assertThat
import org.junit.Assert
import org.junit.Test

class ContactUnitTest {
    @Test
    fun newContactIntent_regularInput() {
        val name = "adam"
        val phone = "0411111111"
        val email = "adam@example.com"

        val intent: Intent = Contact.getCreateContact(name, phone, email)
        //startActivity(intent)
        //intended()

        assertThat(intent)
    }
}