package com.cloudsurfers.crm.functions
import android.Manifest
import android.app.Activity
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.net.Uri
import android.os.Build
import android.provider.CalendarContract
import android.util.Log
import androidx.annotation.RequiresApi
import com.cloudsurfers.crm.R
import android.provider.CalendarContract.Attendees




class CalendarUtil{
    companion object{
        private val emailRegex = Regex("[a-z0-9!#\$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#\$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?")
        private val EVENT_PROJECTION: Array<String> = arrayOf(
            CalendarContract.Calendars._ID,                     // 0
            CalendarContract.Calendars.ACCOUNT_NAME,            // 1
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,   // 2
            CalendarContract.Calendars.OWNER_ACCOUNT            // 3
        )

        // The indices for the projection array above.
        private const val PROJECTION_ID_INDEX: Int = 0
        private const val PROJECTION_ACCOUNT_NAME_INDEX: Int = 1
        private const val PROJECTION_DISPLAY_NAME_INDEX: Int = 2
        private const val PROJECTION_OWNER_ACCOUNT_INDEX: Int = 3
        private const val ONE_HOUR_IN_MILLI : Long = 60 * 60 * 1000;

        @RequiresApi(Build.VERSION_CODES.N)
        fun getInsertEventIntent(title: String, contactEmail: String, location: String, dateTime: Calendar,  desc: String): Intent{
            val startMillis: Long = dateTime.timeInMillis
            val endMillis: Long = startMillis + ONE_HOUR_IN_MILLI;
            val intent: Intent = Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
                .putExtra(CalendarContract.Events.TITLE, title)
                .putExtra(CalendarContract.Events.DESCRIPTION, desc)
                .putExtra(CalendarContract.Events.EVENT_LOCATION, location)
                .putExtra(
                    CalendarContract.Events.AVAILABILITY,
                    CalendarContract.Events.AVAILABILITY_BUSY
                )
                .putExtra(Intent.EXTRA_EMAIL, contactEmail)
            return intent
        }

        @RequiresApi(Build.VERSION_CODES.N)
        fun getViewEventIntent(eventID : Long): Intent{
            val uri: Uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventID)
            return Intent(Intent.ACTION_VIEW).setData(uri)
        }

        @RequiresApi(Build.VERSION_CODES.N)
        fun getViewCalendarIntent(): Intent{
            val builder = CalendarContract.CONTENT_URI.buildUpon()
            builder.appendPath("time")
            ContentUris.appendId(builder, Calendar.getInstance().timeInMillis)
            return Intent(Intent.ACTION_VIEW).setData(builder.build())
        }

        private fun getCalendarId(activity: Activity) : Long{
            val uri: Uri = CalendarContract.Calendars.CONTENT_URI
            val sharedPref = activity.getSharedPreferences(activity.getString(R.string.preference_file_key), Context.MODE_PRIVATE)
            val currEmail = sharedPref.getString("email", "")
            val cursor: Cursor? = activity.contentResolver.query(uri, EVENT_PROJECTION, null, null, null)
            if(cursor!!.moveToFirst()){
                while (cursor.moveToNext()) {
                    // Get the field values
                    val calID: Long = cursor.getLong(PROJECTION_ID_INDEX)
                    val accountName: String = cursor.getString(PROJECTION_OWNER_ACCOUNT_INDEX)
                    if (accountName == currEmail) return calID
                }
            }

            return -1
        }

        @RequiresApi(Build.VERSION_CODES.N)
        fun addEvent(activity: Activity, title: String, contactEmail: String, location: String, dateTime: Calendar,  desc: String) : Long {
            if (activity.checkSelfPermission(Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED){
                val requestCode = 1;
                activity.requestPermissions(arrayOf(Manifest.permission.WRITE_CALENDAR), requestCode);
            }

            val calID = getCalendarId(activity)
            if (calID < 0) return calID

            val startMillis: Long = dateTime.timeInMillis
            val endMillis: Long = startMillis + ONE_HOUR_IN_MILLI;

            val eventValues = ContentValues().apply {
                put(CalendarContract.Events.DTSTART, startMillis)
                put(CalendarContract.Events.DTEND, endMillis)
                put(CalendarContract.Events.TITLE, title)
                put(CalendarContract.Events.DESCRIPTION, desc)
                put(CalendarContract.Events.CALENDAR_ID, calID)
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
                put(CalendarContract.Events.EVENT_LOCATION, location)
            }
            val uri: Uri? = activity.contentResolver.insert(CalendarContract.Events.CONTENT_URI, eventValues)

            Log.i("ddddd", uri.toString())

            var eventId: Long = -1
            if (uri != null){
                eventId = uri.lastPathSegment?.toLong() ?: -1
            }

            if (eventId < 0) return eventId;

            if (emailRegex.matches(contactEmail)){
                val attendeeValues = ContentValues().apply {
                    put(CalendarContract.Attendees.ATTENDEE_EMAIL, contactEmail);
                    put(CalendarContract.Attendees.EVENT_ID, eventId);
                }
                activity.contentResolver.insert(CalendarContract.Attendees.CONTENT_URI, attendeeValues)

            }

            return eventId
        }


        fun deleteEvent(activity: Activity, eventID: Long): Boolean{
            val deleteUri: Uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventID)
            val rows: Int = activity.contentResolver.delete(deleteUri, null, null)
            return rows > 0
        }
        @RequiresApi(Build.VERSION_CODES.N)
        fun updateEvent(activity: Activity, eventID: Long, title: String, contactEmail: String, location: String, dateTime: Calendar,  desc: String, prevContactEmail: String?) : Long {
            if (activity.checkSelfPermission(Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED){
                val requestCode = 1;
                activity.requestPermissions(arrayOf(Manifest.permission.WRITE_CALENDAR), requestCode);
            }

            val startMillis: Long = dateTime.timeInMillis
            val endMillis: Long = startMillis + ONE_HOUR_IN_MILLI;

            val eventValues = ContentValues().apply {
                put(CalendarContract.Events.DTSTART, startMillis)
                put(CalendarContract.Events.DTEND, endMillis)
                put(CalendarContract.Events.TITLE, title)
                put(CalendarContract.Events.DESCRIPTION, desc)
                put(CalendarContract.Events.EVENT_TIMEZONE, dateTime.timeZone.displayName)
                put(CalendarContract.Events.EVENT_LOCATION, location)
            }

            var updateUri: Uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventID)
            activity.contentResolver.update(updateUri, eventValues, null, null)

            if (!contactEmail.isNullOrBlank() && contactEmail != prevContactEmail){
                if (!prevContactEmail.isNullOrBlank()){
                    val selection =
                        "(" + Attendees.EVENT_ID + " = ?) AND (" + Attendees.ATTENDEE_EMAIL + " = ?)"
                    val selectionArgs = arrayOf(eventID.toString() + "", prevContactEmail)
                    activity.contentResolver.delete(Attendees.CONTENT_URI, selection, selectionArgs)
                }


                val attendeeValues = ContentValues().apply {
                    put(CalendarContract.Attendees.ATTENDEE_EMAIL, contactEmail);
                    put(CalendarContract.Attendees.EVENT_ID, eventID);
                }

                if (emailRegex.matches(contactEmail)) {
                    activity.contentResolver.insert(
                        CalendarContract.Attendees.CONTENT_URI,
                        attendeeValues
                    )
                }

            }


            return eventID
        }
    }
}
