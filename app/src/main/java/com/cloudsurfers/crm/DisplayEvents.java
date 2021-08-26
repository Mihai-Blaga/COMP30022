package com.cloudsurfers.crm;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;

public class DisplayEvents extends AppCompatActivity implements View.OnClickListener {
    Button fetchButton;
    Cursor calendarCursor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_events);

        // add listener to fetch button
        fetchButton = findViewById(R.id.fetchButton);
        fetchButton.setOnClickListener(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void createEvent(){
        System.out.println("Creating an event!");
        Calendar beginTime = Calendar.getInstance();
        beginTime.set(2012, 0, 19, 7, 30);
        Calendar endTime = Calendar.getInstance();
        endTime.set(2012, 0, 19, 8, 30);
        ContentResolver cr = getContentResolver();
        ContentValues values = new ContentValues();
        String summary = "Value";
        values.put(CalendarContract.Events.DTSTART, String.valueOf(beginTime));
        values.put(CalendarContract.Events.DTEND, String.valueOf(endTime));
        values.put(CalendarContract.Events.TITLE, summary);
        values.put(CalendarContract.Events.DESCRIPTION, summary);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, "America/Los_Angeles");
        values.put(CalendarContract.Events.EVENT_LOCATION, "");
        Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fetchButton:
                System.out.println("Pressed Button");
                // fetch button on click logic
                // first check if the permission to read Calendar has been granted
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED){
                    System.out.println("Returning because permission not granted");
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALENDAR}, 42);
                    return;
                }


                // Build and execute the query
                long now = System.currentTimeMillis();
                Uri.Builder builder = Uri.parse("content://com.android.calendar/instances/when").buildUpon();
                ContentUris.appendId(builder, now);
                ContentUris.appendId(builder, now + DateUtils.DAY_IN_MILLIS * 7);
                calendarCursor = getContentResolver().query(
                        builder.build(),
                        null,
                        null,
                        null,
                        CalendarContract.Events.DTSTART+" ASC"
                );

                System.out.println("Got events : "+String.valueOf(
                        calendarCursor.getCount()
                ));
                Calendar nowTime = Calendar.getInstance();
                nowTime.setTimeInMillis(now);
                System.out.println(String.format(
                        " Current Time : %d/%d/%d - %d:%d",
                        nowTime.get(Calendar.DAY_OF_MONTH),
                        nowTime.get(Calendar.MONTH)+1, // Jan is month 0
                        nowTime.get(Calendar.YEAR),
                        nowTime.get(Calendar.HOUR_OF_DAY),
                        nowTime.get(Calendar.MINUTE)
                ));

                // iterate through and output all the results in a table
                TableLayout eventTable = (TableLayout)findViewById(R.id.eventsTable);
                eventTable.setStretchAllColumns(true);
                eventTable.bringToFront();
                while(calendarCursor.moveToNext()){
                    if (calendarCursor!=null){
                        int id_start = calendarCursor.getColumnIndex(CalendarContract.Events.DTSTART);
                        int id_title = calendarCursor.getColumnIndex(CalendarContract.Events.TITLE);
                        int id_desc = calendarCursor.getColumnIndex(CalendarContract.Events.DESCRIPTION);
                        int id_location = calendarCursor.getColumnIndex(CalendarContract.Events.EVENT_LOCATION);

                        String beginTime = calendarCursor.getString(id_start);
                        Calendar beginCalendarObject = Calendar.getInstance();
                        beginCalendarObject.setTimeInMillis(Long.parseLong(beginTime));

                        String title = calendarCursor.getString(id_title);
                        String description = calendarCursor.getString(id_desc);
                        String location = calendarCursor.getString(id_location);

                        System.out.println(title+","+description+","+location);

                        TableRow tr =  new TableRow(this);
                        tr.setLayoutParams(new TableLayout.LayoutParams(
                                TableLayout.LayoutParams.MATCH_PARENT,
                                TableLayout.LayoutParams.WRAP_CONTENT));
                        TextView c1 = new TextView(this);
                        c1.setText(String.format(
                                "%d/%d/%d - %d:%d",
                                    beginCalendarObject.get(Calendar.DAY_OF_MONTH),
                                    beginCalendarObject.get(Calendar.MONTH)+1,
                                    beginCalendarObject.get(Calendar.YEAR),
                                    beginCalendarObject.get(Calendar.HOUR_OF_DAY),
                                    beginCalendarObject.get(Calendar.MINUTE)
                                ));

                        TextView c2 = new TextView(this);
                        c2.setText(title);
                        TextView c3 = new TextView(this);
                        c3.setLayoutParams(new TableRow.LayoutParams(
                                TableLayout.LayoutParams.MATCH_PARENT,
                                TableLayout.LayoutParams.WRAP_CONTENT));
                        c3.setText(description);
                        tr.addView(c1);
                        tr.addView(c2);
                        tr.addView(c3);
                        eventTable.addView(tr);
                    }
                    else{
                        Toast.makeText(this,"Events are over!",Toast.LENGTH_SHORT).show();
                    }
                }

                break;
        }
    }
}