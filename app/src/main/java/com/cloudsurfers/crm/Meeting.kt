package com.cloudsurfers.crm

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresApi
import android.os.Build
import com.cloudsurfers.crm.Meeting
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.database.Cursor
import android.provider.CalendarContract
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class Meeting private constructor(
    val eventID: String,
    val title: String,
    val description: String,
    val beginDate: Date,
    val endDate: Date,
    val contactName: String?,
    val contactEmail: String?
) {
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
            val now = System.currentTimeMillis()
            val builder = CalendarContract.Events.CONTENT_URI.buildUpon()
            calendarCursor = context.contentResolver.query(
                builder.build(), arrayOf(
                    CalendarContract.Events._ID,
                    CalendarContract.Events.TITLE,
                    CalendarContract.Events.DESCRIPTION,
                    CalendarContract.Events.DTSTART,
                    CalendarContract.Events.DTEND,
                    CalendarContract.Events.ALL_DAY
                ),
                null,
                null,
                null
            )
            val idEvent = 0
            val idTitle = 1
            val idDesc = 2
            val idStart = 3
            val idEnd = 4
            val idAllDay = 5
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
                    // create a meeting object and add it to arraylist
                    val newMeeting = Meeting(
                        eventId,
                        title,
                        desc,
                        startDate,
                        endDate,
                        attendeeName,
                        attendeeEmail
                    )
                    meetings.add(newMeeting)
                } while (calendarCursor!!.moveToNext())
            }
            calendarCursor!!.close()

            // sort and return meetings
            meetings.sortWith(Comparator { meeting, t1 -> meeting.beginDate.compareTo(t1.beginDate) })
            return meetings
        }
    }
}