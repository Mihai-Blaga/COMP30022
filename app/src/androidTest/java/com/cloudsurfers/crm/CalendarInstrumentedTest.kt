package com.cloudsurfers.crm

import android.content.Intent
import com.cloudsurfers.crm.functions.Contact
//import org.hamcrest.MatcherAssert.assertThat
import androidx.test.ext.truth.content.IntentSubject.assertThat
import com.cloudsurfers.crm.functions.CalendarUtil
import android.icu.util.Calendar
import android.provider.CalendarContract
import org.junit.Assert
import org.junit.Test
import java.util.*

class CalendarInstrumentedTest {
    @Test
    fun newCalendarIntent_regularInput() {
        val meetingName = "Test"
        val meetingContact = "Adam"
        val meetingLocation = "UniMelb"
        val meetingNotes = "N/A"
        val cal = Calendar.getInstance()

        val intent = CalendarUtil.getInsertEventIntent(meetingName, meetingContact, meetingLocation, cal, meetingNotes)
        assertThat(intent).hasAction(Intent.ACTION_INSERT)

        val extras = intent.extras
        assert(extras!!.get(CalendarContract.Events.TITLE) == meetingName)
        assert(extras.get(Intent.EXTRA_EMAIL) == meetingContact)
        assert(extras.get(CalendarContract.Events.EVENT_LOCATION) == meetingLocation)
        assert(extras.get(CalendarContract.Events.DESCRIPTION) == meetingNotes)

    }
}