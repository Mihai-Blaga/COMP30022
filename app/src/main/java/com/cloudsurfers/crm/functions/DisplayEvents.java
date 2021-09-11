package com.cloudsurfers.crm.functions;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.cloudsurfers.crm.R;
import com.cloudsurfers.crm.functions.Meeting;

import java.util.ArrayList;

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

                ArrayList<Meeting> meetings = Meeting.fetchAllMeetings(this);

                // iterate through and output all the results in a table
                TableLayout eventTable = (TableLayout)findViewById(R.id.eventsTable);
                eventTable.setStretchAllColumns(true);
                eventTable.bringToFront();

                for (Meeting meeting:
                     meetings) {
                    System.out.println(meeting);
                    TableRow tr =  new TableRow(this);
                    tr.setLayoutParams(new TableLayout.LayoutParams(
                            TableLayout.LayoutParams.MATCH_PARENT,
                            TableLayout.LayoutParams.WRAP_CONTENT));
                    TextView c1 = new TextView(this);
                    c1.setText(meeting.getMeetingTime());
                    TextView c2 = new TextView(this);
                    c2.setText(meeting.getTitle());
                    TextView c3 = new TextView(this);
                    c3.setLayoutParams(new TableRow.LayoutParams(
                            TableLayout.LayoutParams.MATCH_PARENT,
                            TableLayout.LayoutParams.WRAP_CONTENT));
                    c3.setText(meeting.getDescription());
                    tr.addView(c1);
                    tr.addView(c2);
                    tr.addView(c3);
                    eventTable.addView(tr);
                }

                break;
        }
    }
}