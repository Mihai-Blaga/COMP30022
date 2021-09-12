package com.cloudsurfers.crm.functions

import android.Manifest
import android.app.Activity
import android.content.Context
import androidx.annotation.RequiresApi
import android.os.Build
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.database.Cursor
import android.provider.CalendarContract
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class Meeting() {
    var eventID: String? = null
    var title: String? = null
    var description: String? = null
    var beginDate: Date? = null
    var endDate: Date? = null
    var contactName: String? = null
    var contactEmail: String? = null
    var contact: Contact? = null
    var location: String? = null

    private constructor(
        eventID: String,
        title: String,
        description: String,
        beginDate: Date,
        endDate: Date,
        contactName: String?,
        contactEmail: String?,
        contact: Contact?,
        location: String?
    ) : this() {
        this.eventID = eventID
        this.title = title
        this.description = description
        this.beginDate = beginDate
        this.endDate = endDate
        this.contactEmail = contactEmail
        this.contactName = contactName
        this.contact = contact
        this.location = location
    }

    val meetingTime: String
        get() {
            val dateFormat: DateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm aa")
            val begin = dateFormat.format(beginDate).split(" ").toTypedArray()
            val end = dateFormat.format(endDate).split(" ").toTypedArray()
            val meetingTime: String
            meetingTime = if (begin[2] === end[2]) {
                begin[0] + " " + begin[1] + "-" + end[1] + end[2]
            } else {
                begin[0] + " " + begin[1] + begin[2] + "-" + end[1] + end[2]
            }
            return meetingTime
        }

    override fun toString(): String {
        return "Meeting{" +
                "beginDate=" + beginDate +
                ", endDate=" + endDate +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", contactName='" + contactName + '\'' +
                ", contactEmail='" + contactEmail + '\'' +
                ", eventID='" + eventID + '\'' +
                ", contact=" + contact +
                '}'
    }

    companion object {
        private var calendarCursor: Cursor? = null

        @JvmStatic
        @RequiresApi(api = Build.VERSION_CODES.N)
        fun fetchAllMeetings(context: Context): ArrayList<Meeting>? {
            // first check if the permission to read Calendar has been granted
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_CALENDAR
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                println("Returning because permission not granted")
                // Activity should implement the following code
                // ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALENDAR}, 42);
                return null
            }

            // Build and execute the query
            val builder = CalendarContract.Events.CONTENT_URI.buildUpon()
            calendarCursor = context.contentResolver.query(
                builder.build(), arrayOf(
                    CalendarContract.Events._ID,
                    CalendarContract.Events.TITLE,
                    CalendarContract.Events.DESCRIPTION,
                    CalendarContract.Events.DTSTART,
                    CalendarContract.Events.DTEND,
                    CalendarContract.Events.ALL_DAY,
                    CalendarContract.Events.EVENT_LOCATION
                ),
                null,
                null,
                null
            )
            // declare the ids as they appear in the query result
            val idEvent = 0
            val idTitle = 1
            val idDesc = 2
            val idStart = 3
            val idEnd = 4
            val idAllDay = 5
            val idLocation = 6
            val meetings = ArrayList<Meeting>()
            if (calendarCursor!!.moveToFirst()) {
                do {
                    // extract fields for the event, skip over all day events
                    if (calendarCursor!!.getInt(idAllDay) != 0) {
                        continue
                    }
                    val eventId = calendarCursor!!.getString(idEvent)
                    val title = calendarCursor!!.getString(idTitle)
                    val desc = calendarCursor!!.getString(idDesc)
                    val startDate = Date(calendarCursor!!.getLong(idStart))
                    val endDate = Date(calendarCursor!!.getLong(idEnd))
                    val location = calendarCursor!!.getString(idLocation)
                    // create a cursor query for attendees and fetch attendee name and email
                    val eventAttendeesCursor = context.contentResolver.query(
                        CalendarContract.Attendees.CONTENT_URI, arrayOf(
                            CalendarContract.Attendees.ATTENDEE_NAME,
                            CalendarContract.Attendees.ATTENDEE_EMAIL,
                            CalendarContract.Attendees.EVENT_ID
                        ),
                        CalendarContract.Attendees.EVENT_ID + " = " + eventId,
                        null,
                        null
                    )
                    var attendeeName: String? = null
                    var attendeeEmail: String? = null
                    if (eventAttendeesCursor!!.moveToFirst()) {
                        attendeeName = eventAttendeesCursor.getString(0)
                        attendeeEmail = eventAttendeesCursor.getString(1)
                    }
                    eventAttendeesCursor.close()
                    // try to find the associated contact otherwise keep it null
                    var contact: Contact? = null;
                    if (attendeeEmail != null) {
                        contact = Contact.readContactFromEmail(attendeeEmail, context as Activity)
                    }
                    // create a meeting object and add it to arraylist
                    val newMeeting = Meeting(
                        eventId,
                        title,
                        desc,
                        startDate,
                        endDate,
                        attendeeName,
                        attendeeEmail,
                        contact,
                        location
                    )
                    meetings.add(newMeeting)
                } while (calendarCursor!!.moveToNext())
            }
            calendarCursor!!.close()

            // sort and return meetings
            meetings.sortWith(Comparator { meeting, t1 -> meeting.beginDate!!.compareTo(t1.beginDate) })
            return meetings
        }
    }
}