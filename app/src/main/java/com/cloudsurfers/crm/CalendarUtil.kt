package com.cloudsurfers.crm
import android.Manifest
import android.app.Activity
import android.content.ContentUris
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.net.Uri
import android.os.Build
import android.provider.CalendarContract
import androidx.annotation.RequiresApi
import java.util.*


class CalendarUtil{
    companion object{
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

        @RequiresApi(Build.VERSION_CODES.N)
        fun addEvent(activity: Activity) : Long {
            if (activity.checkSelfPermission(Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED){
                val requestCode = 1;
                activity.requestPermissions(arrayOf(Manifest.permission.WRITE_CALENDAR), requestCode);
            }

            val calID = 3;
            val startMillis: Long = Calendar.getInstance().run {
                set(2021, 7, 21, 7, 30)
                timeInMillis
            }
            val endMillis: Long = Calendar.getInstance().run {
                set(2021, 7, 21, 8, 45)
                timeInMillis
            }

            val values = ContentValues().apply {
                put(CalendarContract.Events.DTSTART, startMillis)
                put(CalendarContract.Events.DTEND, endMillis)
                put(CalendarContract.Events.TITLE, "Jazzercise")
                put(CalendarContract.Events.DESCRIPTION, "Group workout")
                put(CalendarContract.Events.CALENDAR_ID, calID)
                put(CalendarContract.Events.EVENT_TIMEZONE, "Australia/Sydney")
            }
            val uri: Uri? = activity.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            var eventID: Long = -1
            if (uri != null){
                eventID = uri.lastPathSegment?.toLong() ?: -1
            }

            return eventID
        }
    }
}
