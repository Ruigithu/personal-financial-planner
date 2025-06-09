package com.ruipeng.planner.service;

import com.ruipeng.planner.dto.AppointmentCreateDto;
import com.ruipeng.planner.dto.AppointmentDetailsDto;
import com.ruipeng.planner.dto.OAuthSuccessEvent;
import com.ruipeng.planner.entity.*;
import com.ruipeng.planner.repository.AdvisorRepository;
import com.ruipeng.planner.repository.AppointmentRepository;
import com.ruipeng.planner.repository.FinancialPlanRepository;
import com.ruipeng.planner.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final AdvisorRepository advisorRepository;
    private final FinancialPlanRepository financialPlanRepository;
    private final EmailService emailService;

    @Autowired
    private GoogleCalendarService googleCalendarService;

    @Autowired
    private EmailInvitationService emailInvitationService;

    @Autowired
    private GoogleOAuthService googleOAuthService;

    @Autowired
    public AppointmentService(AppointmentRepository appointmentRepository, UserRepository userRepository, AdvisorRepository advisorRepository, FinancialPlanRepository financialPlanRepository, EmailService emailService) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.advisorRepository = advisorRepository;
        this.financialPlanRepository = financialPlanRepository;
        this.emailService = emailService;
    }
    Logger log = LoggerFactory.getLogger(AppointmentService.class);

    public List<AppointmentDetailsDto> getUserAppointments(Long userId) {
        List<Appointment> appointmentList = appointmentRepository.findByUserId(userId);
        List<AppointmentDetailsDto> result =  new ArrayList<>();
        for (Appointment appointment : appointmentList) {
            result.add(AppointmentDetailsDto.fromEntity(appointment));
        }
        return  result;
    }

    public List<Appointment> getAdvisorAppointments(Long advisorId) {
        return appointmentRepository.findByAdvisorId(advisorId);
    }

    public Appointment getAppointmentById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found with id: " + id));
    }

    @Transactional
    public AppointmentCreateDto createAppointment(AppointmentCreateDto dto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        Advisor advisor = advisorRepository.findById(dto.getAdvisorId())
                .orElseThrow(() -> new EntityNotFoundException("Advisor not found with id: " + dto.getAdvisorId()));

        // Check if the appointment time is available
        List<Appointment> existingAppointments = appointmentRepository.findByAdvisorIdAndAppointmentDateBetween(
                advisor.getId(),
                dto.getAppointmentDate().minusMinutes(30),
                dto.getAppointmentDate().plusMinutes(dto.getDurationMinutes())
        );

        if (!existingAppointments.isEmpty()) {
            throw new IllegalStateException("Selected time slot is no longer available");
        }

        // Create the appointment
        Appointment appointment = new Appointment();
        appointment.setUser(user);
        appointment.setAdvisor(advisor);
        appointment.setAppointmentDate(dto.getAppointmentDate());
        appointment.setDurationMinutes(dto.getDurationMinutes());
        appointment.setSessionType(dto.getSessionType());
        appointment.setStatus(AppointmentStatus.CONFIRMED); // Initial status
        appointment.setBookingDate(LocalDateTime.now());

        if (dto.getSharedPlanId() != null) {
            FinancialPlan plan = financialPlanRepository.findById(dto.getSharedPlanId())
                    .orElseThrow(() -> new EntityNotFoundException("Financial plan not found with id: " + dto.getSharedPlanId()));
            appointment.setSharedPlan(plan);
        }

        appointment.setUserNotes(dto.getUserNotes());

        // Save appointment
        Appointment savedAppointment = appointmentRepository.save(appointment);

        // 所有预约都是免费的，直接生成会议链接
        String meetingLink = generateMeetingLink(savedAppointment);
        savedAppointment.setMeetingLink(meetingLink);
        savedAppointment = appointmentRepository.save(savedAppointment);
        try {
            // 检查用户OAuth状态
            if (googleOAuthService.isUserAuthorized(userId)) {
                // 用户已授权，直接生成会议链接
                generateMeetingLink(savedAppointment.getId(), userId);
            } else {
                // 用户未授权，需要前端处理OAuth流程
                log.info("User {} needs OAuth authorization for appointment {}",
                        userId, savedAppointment.getId());
                // 前端会检测到这种情况并触发OAuth流程
            }
        } catch (Exception e) {
            log.error("Failed to generate meeting link during appointment creation: {}", e.getMessage());
            // 不影响预约创建，会议链接可以稍后生成
        }

        AppointmentCreateDto result = new AppointmentCreateDto();
        result.setId(savedAppointment.getId());

        return result;
    }

    @Transactional
    public Appointment updateAppointmentStatus(Long appointmentId, AppointmentStatus newStatus) {
        Appointment appointment = getAppointmentById(appointmentId);
        appointment.setStatus(newStatus);

        if (newStatus == AppointmentStatus.CONFIRMED && appointment.getMeetingLink() == null) {
            // Generate meeting link when confirming appointment
            String meetingLink = generateMeetingLink(appointment);
            appointment.setMeetingLink(meetingLink);

            // Send confirmation emails
            emailService.sendAppointmentConfirmation(appointment);
        } else if (newStatus == AppointmentStatus.CANCELLED) {
            // Send cancellation emails
            emailService.sendAppointmentCancellation(appointment);
        }

        return appointmentRepository.save(appointment);
    }

    @Transactional
    public Appointment addAdvisorNotesToAppointment(Long appointmentId, String notes) {
        Appointment appointment = getAppointmentById(appointmentId);
        appointment.setAdvisorNotes(notes);
        return appointmentRepository.save(appointment);
    }

    @Transactional
    public void handlePaymentSuccess(String paymentIntentId) {
        // Find appointment by payment intent ID and confirm it
        // This is a simplified implementation
        // In a real application, you would have a field to store the payment intent ID
        // and use it to look up the appointment

        // For demonstration purposes:
        // Appointment appointment = appointmentRepository.findByPaymentIntentId(paymentIntentId);
        // if (appointment != null) {
        //     updateAppointmentStatus(appointment.getId(), AppointmentStatus.CONFIRMED);
        // }
    }

    private String generateMeetingLink(Appointment appointment) {
        // In a real application, this would integrate with Zoom, Google Meet, or another video conferencing API
        // For demonstration purposes, we'll return a placeholder
        return "https://meeting.example.com/join/" + appointment.getId() + "?key=" + System.currentTimeMillis();
    }

    private User getUser(Long userId){
        return userRepository.findById(userId).orElse(null);
    }

    @Transactional
    public void generateMeetingLink(Long appointmentId, Long userId) {
        try {
            Appointment appointment = getAppointmentById(appointmentId);

            // 验证用户权限
            if (!appointment.getUser().getId().equals(userId)) {
                throw new SecurityException("User not authorized to access this appointment");
            }

            // 检查是否已经有会议链接
            if (appointment.getMeetingLink() != null && !appointment.getMeetingLink().isEmpty()) {
                log.info("Meeting link already exists for appointment {}", appointmentId);
                return;
            }

            // 🎯 统一调用：自动选择最佳认证方式
            String meetingLink = googleCalendarService.createAppointmentEvent(appointment, userId);

            // 更新预约信息
            appointment.setMeetingLink(meetingLink);
            appointmentRepository.save(appointment);

            // 发送包含会议链接的邮件通知
            if (meetingLink != null) {
                emailInvitationService.sendMeetInvitation(appointment, meetingLink);
            }

            log.info("Meeting link generated successfully for appointment {}: {}", appointmentId, meetingLink);

        } catch (Exception e) {
            log.error("Failed to generate meeting link for appointment {}: {}",
                    appointmentId, e.getMessage(), e);
            throw new RuntimeException("Failed to generate meeting link", e);
        }
    }

    /**
     * OAuth授权后的回调处理
     */
    @EventListener
    public void handleOAuthSuccessEvent(OAuthSuccessEvent event) {
        try {
            generateMeetingLink(event.getAppointmentId(), event.getUserId());
            log.info("Meeting link generated for appointment {} after OAuth success",
                    event.getAppointmentId());
        } catch (Exception e) {
            log.error("Failed to generate meeting link after OAuth success: {}", e.getMessage());
        }
    }
}
