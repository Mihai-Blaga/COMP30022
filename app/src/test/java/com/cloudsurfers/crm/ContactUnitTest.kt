package com.cloudsurfers.crm

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.cloudsurfers.crm.functions.Contact.Companion.readContacts
import com.cloudsurfers.crm.pages.main.MainActivity
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.robolectric.Robolectric.setupActivity

class ContactUnitTest {
    var activity = ActivityScenario.launch(MainActivity::class.java)

    @Test
    fun readContacts_correctOutputWithValidData(){

    }
}