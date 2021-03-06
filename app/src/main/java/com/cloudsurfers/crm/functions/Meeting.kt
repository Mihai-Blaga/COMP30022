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
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class Meeting() {
    var eventID: String? = null
    var title: String? = null
    var description: String? = null
    var beginDate: LocalDateTime? = null
    var endDate: LocalDateTime? = null
    var contactName: String? = null
    var contactEmail: String? = null
    var contact: Contact? = null
    var location: String? = null

    private constructor(
        eventID: String?,
        title: String?,
        description: String?,
        beginDate: LocalDateTime?,
        endDate: LocalDateTime?,
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
        @RequiresApi(Build.VERSION_CODES.O)
        get() {
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a")
            val begin = beginDate?.format(formatter)?.split(" ")?.toTypedArray()
            val end = endDate?.format(formatter)?.split(" ")?.toTypedArray()
            val meetingTime: String
            return if((begin!=null) && (end!=null)) {
                meetingTime = if ((begin[2] === end[2])) {
                    begin[0] + " " + begin[1] + "-" + end[1] + end[2]
                } else {
                    begin[0] + " " + begin[1] + begin[2] + "-" + end[1] + end[2]
                }
                meetingTime
            } else{
                ""
            }
        }

    val meetingDay: String
        @RequiresApi(Build.VERSION_CODES.O)
        get(){
            val formatter = DateTimeFormatter.ofPattern("E, d MMM")
            return if(beginDate!=null){
                beginDate!!.format(formatter)}
            else{
                ""
            }
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
                ", location='" + location + '\'' +
                '}'
    }

    companion object {
        private var calendarCursor: Cursor? = null
        private var emailLookup: HashMap<String, ArrayList<Meeting>> = hashMapOf()
        private var allMeetings: HashSet<Meeting> = hashSetOf()

        @RequiresApi(Build.VERSION_CODES.O)
        fun refresh(context: Context){
            emailLookup = hashMapOf()
            allMeetings = hashSetOf()
            searchAllMeetings(context)
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun searchAllMeetings(context: Context): ArrayList<Meeting>? {
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
            // reset the static variable
            emailLookup = hashMapOf()
            allMeetings = hashSetOf()
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

                    // fetch the dates in milliseconds and convert into LocalDateTime Objects
                    val startDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(calendarCursor!!.getLong(idStart)), ZoneId.systemDefault())
                    val endDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(calendarCursor!!.getLong(idEnd)), ZoneId.systemDefault())
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
                    var contact: Contact? = null
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
                    allMeetings.add(newMeeting)
                    // add email lookup as well
                    if(!emailLookup.containsKey(attendeeEmail)){
                        if (attendeeEmail != null) {
                            emailLookup[attendeeEmail] = arrayListOf()
                        }
                    }
                    emailLookup[attendeeEmail]?.add(newMeeting)
                } while (calendarCursor!!.moveToNext())
            }
            calendarCursor!!.close()

            // sort and return meetings
            meetings.sortWith { meeting, t1 -> meeting.beginDate!!.compareTo(t1.beginDate) }
            return meetings
        }

        @RequiresApi(Build.VERSION_CODES.O)
        @JvmStatic
        fun fetchAllMeetings(context: Context): ArrayList<Meeting>? {
            if (allMeetings.isEmpty()){
                return searchAllMeetings(context)
            }
            val meetings = arrayListOf<Meeting>()
            meetings.addAll(allMeetings)
            meetings.sortWith { meeting, t1 -> meeting.beginDate!!.compareTo(t1.beginDate) }
            return meetings
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun fetchMeetingsByEmail(email: String,activity: Activity): ArrayList<Meeting>? {
            if (emailLookup.isEmpty()){
                fetchAllMeetings(activity)
            }
            if (emailLookup.containsKey(email)) {
                val meetings = emailLookup[email]
                meetings?.filter {
                    LocalDateTime.now().isBefore(if (it.endDate != null) it.endDate else LocalDateTime.now())
                } as ArrayList<Meeting>
                meetings.sortWith { meeting, t1 -> meeting.beginDate!!.compareTo(t1.beginDate) }
                return meetings
            }
            return arrayListOf()
        }
    }
}