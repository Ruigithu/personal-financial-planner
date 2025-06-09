package com.ruipeng.planner.dto;

import java.util.List;

public class MeetingRequest {
    private String userId;
    private String title;
    private String description;
    private String startTime; // ISO 8601 格式
    private String endTime;   // ISO 8601 格式
    private String timeZone = "Asia/Shanghai";
    private List<String> attendees;

    // 构造函数、getter 和 setter
    public MeetingRequest() {}

    public MeetingRequest(String userId, String title, String description,
                          String startTime, String endTime, List<String> attendees) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.attendees = attendees;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getTimeZone() { return timeZone; }
    public void setTimeZone(String timeZone) { this.timeZone = timeZone; }

    public List<String> getAttendees() { return attendees; }
    public void setAttendees(List<String> attendees) { this.attendees = attendees; }
}
