package com.ruipeng.planner.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruipeng.planner.entity.Appointment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class AppointmentDetailsDto {
    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime appointmentDate;

    private Integer durationMinutes;
    private String sessionType;
    private String status;
    private String meetingLink;
    private String userNotes;
    private String advisorNotes;

    // 平铺的顾问信息 - 不再嵌套！
    private Long advisorId;
    private String advisorFirstName;
    private String advisorLastName;
    private String advisorProfessionalTitle;
    private Set<String> advisorSpecialties;

    // 共享计划信息（可选）
    private Long sharedPlanId;
    private String sharedPlanName;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime sharedPlanCreationDate;
    private String sharedPlanHealthAssessment;

    // 构造函数
    public AppointmentDetailsDto() {}

    // 基本信息的 getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDateTime appointmentDate) { this.appointmentDate = appointmentDate; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getSessionType() { return sessionType; }
    public void setSessionType(String sessionType) { this.sessionType = sessionType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMeetingLink() { return meetingLink; }
    public void setMeetingLink(String meetingLink) { this.meetingLink = meetingLink; }

    public String getUserNotes() { return userNotes; }
    public void setUserNotes(String userNotes) { this.userNotes = userNotes; }

    public String getAdvisorNotes() { return advisorNotes; }
    public void setAdvisorNotes(String advisorNotes) { this.advisorNotes = advisorNotes; }

    // 顾问信息的 getters/setters
    public Long getAdvisorId() { return advisorId; }
    public void setAdvisorId(Long advisorId) { this.advisorId = advisorId; }

    public String getAdvisorFirstName() { return advisorFirstName; }
    public void setAdvisorFirstName(String advisorFirstName) { this.advisorFirstName = advisorFirstName; }

    public String getAdvisorLastName() { return advisorLastName; }
    public void setAdvisorLastName(String advisorLastName) { this.advisorLastName = advisorLastName; }

    public String getAdvisorProfessionalTitle() { return advisorProfessionalTitle; }
    public void setAdvisorProfessionalTitle(String advisorProfessionalTitle) { this.advisorProfessionalTitle = advisorProfessionalTitle; }


    public Set<String> getAdvisorSpecialties() {
        return advisorSpecialties;
    }

    public void setAdvisorSpecialties(Set<String> advisorSpecialties) {
        this.advisorSpecialties = advisorSpecialties;
    }

    // 共享计划信息的 getters/setters
    public Long getSharedPlanId() { return sharedPlanId; }
    public void setSharedPlanId(Long sharedPlanId) { this.sharedPlanId = sharedPlanId; }

    public String getSharedPlanName() { return sharedPlanName; }
    public void setSharedPlanName(String sharedPlanName) { this.sharedPlanName = sharedPlanName; }

    public LocalDateTime getSharedPlanCreationDate() { return sharedPlanCreationDate; }
    public void setSharedPlanCreationDate(LocalDateTime sharedPlanCreationDate) { this.sharedPlanCreationDate = sharedPlanCreationDate; }

    public String getSharedPlanHealthAssessment() { return sharedPlanHealthAssessment; }
    public void setSharedPlanHealthAssessment(String sharedPlanHealthAssessment) { this.sharedPlanHealthAssessment = sharedPlanHealthAssessment; }

    // 从实体转换的静态方法
    public static AppointmentDetailsDto fromEntity(Appointment appointment) {
        AppointmentDetailsDto dto = new AppointmentDetailsDto();

        // 复制基本信息
        dto.setId(appointment.getId());
        dto.setAppointmentDate(appointment.getAppointmentDate());
        dto.setDurationMinutes(appointment.getDurationMinutes());
        dto.setSessionType(appointment.getSessionType().toString());
        dto.setStatus(appointment.getStatus().toString());
        dto.setMeetingLink(appointment.getMeetingLink());
        dto.setUserNotes(appointment.getUserNotes());
        dto.setAdvisorNotes(appointment.getAdvisorNotes());

        // 平铺顾问信息
        if (appointment.getAdvisor() != null) {
            dto.setAdvisorId(appointment.getAdvisor().getId());
            dto.setAdvisorProfessionalTitle(appointment.getAdvisor().getProfessionalTitle());
            dto.setAdvisorSpecialties(appointment.getAdvisor().getSpecialties());

            if (appointment.getAdvisor().getUser() != null) {
                dto.setAdvisorFirstName(appointment.getAdvisor().getUser().getFirstName());
                dto.setAdvisorLastName(appointment.getAdvisor().getUser().getLastName());
            }
        }

        // 平铺共享计划信息
        if (appointment.getSharedPlan() != null) {
            dto.setSharedPlanId(appointment.getSharedPlan().getId());
            dto.setSharedPlanName(appointment.getSharedPlan().getPlanName());
            dto.setSharedPlanCreationDate(appointment.getSharedPlan().getCreationDate());
            dto.setSharedPlanHealthAssessment(appointment.getSharedPlan().getHealthAssessment());
        }

        return dto;
    }
}