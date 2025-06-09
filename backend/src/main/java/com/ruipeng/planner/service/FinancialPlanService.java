package com.ruipeng.planner.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.ruipeng.planner.dto.FinancialPlanRequest;
import com.ruipeng.planner.entity.FinancialPlan;
import com.ruipeng.planner.entity.User;
import com.ruipeng.planner.entity.UserProfile;
import com.ruipeng.planner.repository.FinancialPlanRepository;
import com.ruipeng.planner.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class FinancialPlanService {
    private final FinancialPlanRepository financialPlanRepository;
    private final UserRepository userRepository;
    private final GoogleAIService googleAiService;
    private final ObjectMapper objectMapper;

    @Autowired
    public FinancialPlanService(FinancialPlanRepository financialPlanRepository, UserRepository userRepository, GoogleAIService googleAiService, ObjectMapper objectMapper) {
        this.financialPlanRepository = financialPlanRepository;
        this.userRepository = userRepository;
        this.googleAiService = googleAiService;
        this.objectMapper = objectMapper;
    }

    public List<FinancialPlan> getUserFinancialPlans(Long userId) {

        List<FinancialPlan> plans = financialPlanRepository.findByUserId(userId);
        for(FinancialPlan plan : plans) {
            plan.setUser(null);
        }
        return plans;
    }

    public FinancialPlan getFinancialPlanById(Long planId) {
        return financialPlanRepository.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException("Financial plan not found with id: " + planId));
    }

    public FinancialPlan getFinancialPlanByShareCode(String shareCode) {
        return financialPlanRepository.findByShareCode(shareCode)
                .orElseThrow(() -> new EntityNotFoundException("Financial plan not found with share code: " + shareCode));
    }

    @Transactional
    public FinancialPlan generateFinancialPlan(Long userId, FinancialPlanRequest request) {
        System.out.println("userId 是"+userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        UserProfile profile = user.getProfile();
        System.out.println(profile==null);
        if (profile == null) {
            throw new IllegalStateException("User profile is incomplete");
        }

        // Generate financial plan using Google AI
        String aiGeneratedPlan = googleAiService.generateFinancialPlan(profile, request);

        // Parse the AI response and create a FinancialPlan entity
        FinancialPlan plan = new FinancialPlan();
        plan.setUser(user);
        plan.setCreationDate(LocalDateTime.now());
        plan.setPlanName(request.getPlanName());

        // Extract pieces from AI response
        try {
            // This is simplified. In practice, you would parse the structured response from the AI
            String healthAssessment = "Based on your financial information, here is an assessment..."; // This would be from AI
            Integer healthScore = 75; // This would be calculated based on AI response

            // JSON fields would be structured data from AI response
            plan.setAssetAllocationJson(objectMapper.writeValueAsString(
                    googleAiService.extractAssetAllocation(aiGeneratedPlan)));
            plan.setGoalTimelineJson(objectMapper.writeValueAsString(
                    googleAiService.extractGoalTimeline(aiGeneratedPlan)));
            plan.setInvestmentRecommendationsJson(objectMapper.writeValueAsString(
                    googleAiService.extractInvestmentRecommendations(aiGeneratedPlan)));
            plan.setActionPlanJson(objectMapper.writeValueAsString(
                    googleAiService.extractActionPlan(aiGeneratedPlan)));

            plan.setHealthScore(healthScore);
            plan.setHealthAssessment(healthAssessment);

            // Generate unique share code
            plan.setShareCode(generateShareCode());

        } catch (Exception e) {
            throw new RuntimeException("Error parsing AI response", e);
        }

        return financialPlanRepository.save(plan);
    }

    private String generateShareCode() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    public ByteArrayResource generatePdfReport(Long planId) throws Exception {
        FinancialPlan plan = getFinancialPlanById(planId);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Add content to PDF
        document.add(new Paragraph("Financial Plan: " + plan.getPlanName()).simulateBold().setFontSize(20));
        document.add(new Paragraph("Created on: " + plan.getCreationDate()));
        document.add(new Paragraph("Health Score: " + plan.getHealthScore() + "/100"));
        document.add(new Paragraph("\n"));

        document.add(new Paragraph("Health Assessment").simulateBold().setFontSize(16));
        document.add(new Paragraph(plan.getHealthAssessment()));
        document.add(new Paragraph("\n"));

        // Parse and add JSON data
        try {
            document.add(new Paragraph("Asset Allocation").simulateBold().setFontSize(16));
            document.add(new Paragraph(formatJson(plan.getAssetAllocationJson())));
            document.add(new Paragraph("\n"));

            document.add(new Paragraph("Goal Timeline").simulateBold().setFontSize(16));
            document.add(new Paragraph(formatJson(plan.getGoalTimelineJson())));
            document.add(new Paragraph("\n"));

            document.add(new Paragraph("Investment Recommendations").simulateBold().setFontSize(16));
            document.add(new Paragraph(formatJson(plan.getInvestmentRecommendationsJson())));
            document.add(new Paragraph("\n"));

            document.add(new Paragraph("Action Plan").simulateBold().setFontSize(16));
            document.add(new Paragraph(formatJson(plan.getActionPlanJson())));
        } catch (Exception e) {
            document.add(new Paragraph("Error formatting JSON data: " + e.getMessage()));
        }

        document.close();

        return new ByteArrayResource(baos.toByteArray());
    }

    private String formatJson(String json) {
        try {
            Object jsonObj = objectMapper.readValue(json, Object.class);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObj);
        } catch (Exception e) {
            return json;
        }
    }

    private User getUser(Long userId){
        return userRepository.findById(userId).orElse(null);
    }
}