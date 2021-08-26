package com.cloudsurfers.crm;

import java.util.ArrayList;
import java.util.Calendar;

public class Meeting {
    private Calendar beginCalendar;
    private Calendar endCalendar;
    private String title;
    private String description;
    private String contactName;
    private String contactEmail;
    private String eventID;


    public String getMeetingTime(){
        return new String();
    }

    public static ArrayList<Meeting> fetchAllMeeting(){
        ArrayList<Meeting> meetings = new ArrayList<>();
        return meetings;
    }

    public String getContactName() {
        return contactName;
    }

    public String getTitle() {
        return title;
    }
}
