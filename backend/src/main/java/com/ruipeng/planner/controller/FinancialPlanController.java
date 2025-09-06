package com.ruipeng.planner.controller;

import com.ruipeng.planner.dto.FinancialPlanRequest;
import com.ruipeng.planner.dto.FinancialPlanResponse;
import com.ruipeng.planner.dto.MessageResponse;
import com.ruipeng.planner.entity.FinancialPlan;
import com.ruipeng.planner.config.security.UserDetailsImpl;
import com.ruipeng.planner.service.FinancialPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/plans")

public class FinancialPlanController {
    private final FinancialPlanService financialPlanService;

    @Autowired
    public FinancialPlanController(FinancialPlanService financialPlanService) {
        this.financialPlanService = financialPlanService;
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<FinancialPlan>> getUserPlans() {
        Long userId = getCurrentUserId();
        List<FinancialPlan> plans = financialPlanService.getUserFinancialPlans(userId);
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getPlanById(@PathVariable Long id) {
        try {
            FinancialPlan plan = financialPlanService.getFinancialPlanById(id);

            // Check if the plan belongs to the current user
            if (!plan.getUser().getId().equals(getCurrentUserId())) {
                return ResponseEntity.status(403).body(new MessageResponse("Not authorized to access this plan"));
            }
            plan.setUser(null);
            return ResponseEntity.ok(plan);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/shared/{shareCode}")
    public ResponseEntity<?> getPlanByShareCode(@PathVariable String shareCode) {
        try {
            FinancialPlan plan = financialPlanService.getFinancialPlanByShareCode(shareCode);
            return ResponseEntity.ok(plan);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> generatePlan(@Valid @RequestBody FinancialPlanRequest request) {
        Long userId = getCurrentUserId();
        try {
            FinancialPlan plan = financialPlanService.generateFinancialPlan(userId, request);
            System.out.println("id:"+plan.getId());
            FinancialPlan newPlan = new FinancialPlan();
            newPlan.setId(plan.getId());
            return ResponseEntity.ok(newPlan);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> downloadPlanPdf(@PathVariable Long id) {
        try {
            FinancialPlan plan = financialPlanService.getFinancialPlanById(id);

            // Check if the plan belongs to the current user
            if (!plan.getUser().getId().equals(getCurrentUserId())) {
                return ResponseEntity.status(403).body(new MessageResponse("Not authorized to access this plan"));
            }

            ByteArrayResource resource = financialPlanService.generatePdfReport(id);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"financial-plan-" + plan.getId() + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(resource.contentLength())
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getId();
    }
}
