package com.ruipeng.planner.controller;


import com.ruipeng.planner.dto.AdvisorProfileDto;
import com.ruipeng.planner.dto.AvailabilitySlotDto;
import com.ruipeng.planner.dto.MessageResponse;
import com.ruipeng.planner.entity.Advisor;
import com.ruipeng.planner.entity.AvailabilitySlot;
import com.ruipeng.planner.config.security.UserDetailsImpl;
import com.ruipeng.planner.service.AdvisorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/advisors")
public class AdvisorController {
    private final AdvisorService advisorService;

    @Autowired
    public AdvisorController(AdvisorService advisorService) {
        this.advisorService = advisorService;
    }

    @GetMapping
    public ResponseEntity<List<AdvisorProfileDto>> getAllAdvisors() {
        return ResponseEntity.ok(advisorService.getAllAdvisors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAdvisorById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(advisorService.getAdvisorProfileDtoById(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/specialty/{specialty}")
    public ResponseEntity<List<Advisor>> getAdvisorsBySpecialty(@PathVariable String specialty) {
        return ResponseEntity.ok(advisorService.findAdvisorsBySpecialty(specialty));
    }

    @GetMapping("/language/{language}")
    public ResponseEntity<List<Advisor>> getAdvisorsByLanguage(@PathVariable String language) {
        return ResponseEntity.ok(advisorService.findAdvisorsByLanguage(language));
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('ADVISOR')")
    public ResponseEntity<?> updateAdvisorProfile(@Valid @RequestBody AdvisorProfileDto profileDto) {
        try {
            Advisor advisor = advisorService.updateAdvisorProfile(getCurrentAdvisorId(), profileDto);
            return ResponseEntity.ok(advisor);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/availability")
    @PreAuthorize("hasRole('ADVISOR')")
    public ResponseEntity<?> getMyAvailability() {
        try {
            List<AvailabilitySlot> slots = advisorService.getAdvisorAvailability(getCurrentAdvisorId());
            return ResponseEntity.ok(slots);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/availability")
    @PreAuthorize("hasRole('ADVISOR')")
    public ResponseEntity<?> addAvailabilitySlot(@Valid @RequestBody AvailabilitySlotDto slotDto) {
        try {
            AvailabilitySlot slot = advisorService.addAvailabilitySlot(getCurrentAdvisorId(), slotDto);
            return ResponseEntity.ok(slot);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/availability/{slotId}")
    @PreAuthorize("hasRole('ADVISOR')")
    public ResponseEntity<?> removeAvailabilitySlot(@PathVariable Long slotId) {
        try {
            advisorService.removeAvailabilitySlot(slotId);
            return ResponseEntity.ok(new MessageResponse("Availability slot removed successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/{advisorId}/available-slots")
    public ResponseEntity<?> getAvailableTimeSlots(
            @PathVariable Long advisorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<Map<String, Object>> availableSlots =
                    advisorService.getAvailableTimeSlots(advisorId, startDate, endDate);
            return ResponseEntity.ok(availableSlots);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    private Long getCurrentAdvisorId() throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Get the advisor ID from the user ID
        // This would need to be implemented in the AdvisorService
        // For now, we'll throw an exception if the user is not an advisor
        return advisorService.getAdvisorByUserId(userDetails.getId()).getId();
    }
}
