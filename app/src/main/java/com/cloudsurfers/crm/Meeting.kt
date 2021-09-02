package com.cloudsurfers.crm;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;
import android.text.format.DateUtils;


import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

public class Meeting{
    private Date beginDate;
    private Date endDate;
    private String title;
    private String description;
    private String contactName;
    private String contactEmail;
    private String eventID;
    private static Cursor calendarCursor;

    public Date getBeginDate() {
        return beginDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public String getDescription() {
        return description;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public String getEventID() {
        return eventID;
    }

    private Meeting(
            String eventID,
            String title,
            String description,
            Date beginDate,
            Date endDate,
            String contactName,
            String contactEmail
    ){
        this.eventID = eventID;
        this.title = title;
        this.description = description;
        this.beginDate = beginDate;
        this.endDate = endDate;
        this.contactEmail = contactEmail;
        this.contactName = contactName;
    }

    public String getMeetingTime(){
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm aa");
        String[] begin = dateFormat.format(beginDate).split(" ");
        String[] end = dateFormat.format(endDate).split(" ");
        String meetingTime;
        if (begin[2]==end[2]){
            meetingTime = begin[0]+" "+begin[1]+"-"+end[1]+end[2];
        }
        else{
            meetingTime = begin[0]+" "+begin[1]+begin[2]+"-"+end[1]+end[2];
        }
        return meetingTime;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static ArrayList<Meeting> fetchAllMeetings(Context context) {
        // first check if the permission to read Calendar has been granted
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            System.out.println("Returning because permission not granted");
            // Activity should implement the following code
            // ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALENDAR}, 42);
            return null;
        }

        // Build and execute the query
        long now = System.currentTimeMillis();
        Uri.Builder builder = CalendarContract.Events.CONTENT_URI.buildUpon();
        calendarCursor = context.getContentResolver().query(
                builder.build(),
                new String[]{
                        CalendarContract.Events._ID,
                        CalendarContract.Events.TITLE,
                        CalendarContract.Events.DESCRIPTION,
                        CalendarContract.Events.DTSTART,
                        CalendarContract.Events.DTEND,
                        CalendarContract.Events.ALL_DAY
                },
                null,
                null,
                null
        );
        int idEvent = 0;
        int idTitle = 1;
        int idDesc = 2;
        int idStart = 3;
        int idEnd = 4;
        int idAllDay =5;
        ArrayList<Meeting> meetings = new ArrayList<>();

        if(calendarCursor.moveToFirst()){
            do {
                // extract fields for the event, skip over all day events
                if(calendarCursor.getInt(idAllDay)!=0){
                    continue;
                }
                String eventId = calendarCursor.getString(idEvent);
                String title = calendarCursor.getString(idTitle);
                String desc = calendarCursor.getString(idDesc);
                Date startDate = new Date(calendarCursor.getLong(idStart));
                Date endDate = new Date(calendarCursor.getLong(idEnd));
                // create a cursor query for attendees and fetch attendee name and email
                Cursor eventAttendeesCursor =
                        context.getContentResolver().query(
                                CalendarContract.Attendees.CONTENT_URI,
                                new String []{
                                        CalendarContract.Attendees.ATTENDEE_NAME,
                                        CalendarContract.Attendees.ATTENDEE_EMAIL,
                                        CalendarContract.Attendees.EVENT_ID
                                },
                                CalendarContract.Attendees.EVENT_ID +" = " + eventId,
                                null,
                                null);
                String attendeeName = null;
                String attendeeEmail = null;
                if(eventAttendeesCursor.moveToFirst()){
                    attendeeName = eventAttendeesCursor.getString(0);
                    attendeeEmail = eventAttendeesCursor.getString(1);
                }
                eventAttendeesCursor.close();
                // create a meeting object and add it to arraylist
                Meeting newMeeting = new Meeting(eventId, title, desc, startDate, endDate,
                        attendeeName, attendeeEmail);
                meetings.add(newMeeting);
            }
            while (calendarCursor.moveToNext());
        }
        calendarCursor.close();

        // sort and return meetings
        meetings.sort(new Comparator<Meeting>() {
            @Override
            public int compare(Meeting meeting, Meeting t1) {
                return meeting.getBeginDate().compareTo(t1.getBeginDate());
            }
        });
        return meetings;

    }

    public String getContactName() {
        return contactName;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return "Meeting{" +
                "beginDate=" + beginDate +
                ", endDate=" + endDate +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", contactName='" + contactName + '\'' +
                ", contactEmail='" + contactEmail + '\'' +
                ", eventID='" + eventID + '\'' +
                '}';
    }
}
