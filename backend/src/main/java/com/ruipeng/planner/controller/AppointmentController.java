package com.ruipeng.planner.controller;


import com.ruipeng.planner.dto.AppointmentCreateDto;
import com.ruipeng.planner.dto.AppointmentDetailsDto;
import com.ruipeng.planner.dto.MessageResponse;
import com.ruipeng.planner.entity.Appointment;
import com.ruipeng.planner.entity.AppointmentStatus;
import com.ruipeng.planner.config.security.UserDetailsImpl;
import com.ruipeng.planner.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {
    private final AppointmentService appointmentService;

    @Autowired
    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<AppointmentDetailsDto>> getUserAppointments() {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(appointmentService.getUserAppointments(userId));
    }

    @GetMapping("/advisor")
    @PreAuthorize("hasRole('ADVISOR')")
    public ResponseEntity<?> getAdvisorAppointments() {
        try {
            Long advisorId = getCurrentAdvisorId();
            return ResponseEntity.ok(appointmentService.getAdvisorAppointments(advisorId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADVISOR')")
    public ResponseEntity<?> getAppointmentById(@PathVariable Long id) {
        try {
            Appointment appointment = appointmentService.getAppointmentById(id);

            // Check if the current user is allowed to view this appointment
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            boolean isAdvisor = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADVISOR"));

            if (isAdvisor) {
                if (!appointment.getAdvisor().getUser().getId().equals(userDetails.getId())) {
                    return ResponseEntity.status(403).body(new MessageResponse("Not authorized to access this appointment"));
                }
            } else {
                if (!appointment.getUser().getId().equals(userDetails.getId())) {
                    return ResponseEntity.status(403).body(new MessageResponse("Not authorized to access this appointment"));
                }
            }

            return ResponseEntity.ok(AppointmentDetailsDto.fromEntity(appointment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createAppointment(@Valid @RequestBody AppointmentCreateDto dto) {
        Long userId = getCurrentUserId();
        try {
            AppointmentCreateDto appointment = appointmentService.createAppointment(dto, userId);
            return ResponseEntity.ok(appointment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('USER') or hasRole('ADVISOR')")
    public ResponseEntity<?> updateAppointmentStatus(
            @PathVariable Long id,
            @RequestParam AppointmentStatus status) {
        try {
            Appointment appointment = appointmentService.getAppointmentById(id);

            // Check if the current user is allowed to update this appointment
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            boolean isAdvisor = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADVISOR"));

            // Users can only cancel their own appointments
            // Advisors can confirm, cancel, or mark as completed
            if (isAdvisor) {
                if (appointment.getUser().getId().equals(userDetails.getId())) {
                    return ResponseEntity.status(403).body(new MessageResponse("Not authorized to update this appointment"));
                }
            } else {
                if (!appointment.getUser().getId().equals(userDetails.getId())) {
                    return ResponseEntity.status(403).body(new MessageResponse("Not authorized to update this appointment"));
                }

                // Users can only cancel
                if (status != AppointmentStatus.CANCELLED) {
                    return ResponseEntity.status(403).body(new MessageResponse("Users can only cancel appointments"));
                }
            }

            Appointment updatedAppointment = appointmentService.updateAppointmentStatus(id, status);
            return ResponseEntity.ok(updatedAppointment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PutMapping("/{id}/advisor-notes")
    @PreAuthorize("hasRole('ADVISOR')")
    public ResponseEntity<?> addAdvisorNotes(
            @PathVariable Long id,
            @RequestBody String notes) {
        try {
            Appointment appointment = appointmentService.getAppointmentById(id);

            // Check if the current advisor is allowed to update this appointment
            Long advisorId = getCurrentAdvisorId();
            if (!appointment.getAdvisor().getId().equals(advisorId)) {
                return ResponseEntity.status(403).body(new MessageResponse("Not authorized to update this appointment"));
            }

            Appointment updatedAppointment = appointmentService.addAdvisorNotesToAppointment(id, notes);
            return ResponseEntity.ok(updatedAppointment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getId();
    }

    private Long getCurrentAdvisorId() throws Exception {
        // This would need to be implemented to get the advisor ID from the user ID
        // For now, we'll throw an exception
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
