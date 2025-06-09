package com.ruipeng.planner.dto;


import com.ruipeng.planner.entity.SessionType;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class AppointmentCreateDto {
    private Long id;
    @NotNull
    private Long advisorId;

    @NotNull
    private LocalDateTime appointmentDate;

    @NotNull
    private Integer durationMinutes;

    @NotNull
    private SessionType sessionType;

    private Long sharedPlanId;

    private String userNotes;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAdvisorId() {
        return advisorId;
    }

    public void setAdvisorId(Long advisorId) {
        this.advisorId = advisorId;
    }

    public LocalDateTime getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDateTime appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public SessionType getSessionType() {
        return sessionType;
    }

    public void setSessionType(SessionType sessionType) {
        this.sessionType = sessionType;
    }

    public Long getSharedPlanId() {
        return sharedPlanId;
    }

    public void setSharedPlanId(Long sharedPlanId) {
        this.sharedPlanId = sharedPlanId;
    }

    public String getUserNotes() {
        return userNotes;
    }

    public void setUserNotes(String userNotes) {
        this.userNotes = userNotes;
    }
}
