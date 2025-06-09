package com.ruipeng.planner.dto;

public class MeetingResponse {
    private String eventId;
    private String calendarLink;
    private String meetLink;
    private String title;

    public MeetingResponse(String eventId, String calendarLink, String meetLink, String title) {
        this.eventId = eventId;
        this.calendarLink = calendarLink;
        this.meetLink = meetLink;
        this.title = title;
    }

    // Getters and Setters
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getCalendarLink() {
        return calendarLink;
    }

    public void setCalendarLink(String calendarLink) {
        this.calendarLink = calendarLink;
    }

    public String getMeetLink() {
        return meetLink;
    }

    public void setMeetLink(String meetLink) {
        this.meetLink = meetLink;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
