package com.ruipeng.planner.dto;


import jakarta.validation.constraints.NotBlank;

public class FinancialPlanRequest {
    @NotBlank
    private String planName;

    private String additionalInstructions;

    // Getters and Setters
    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public String getAdditionalInstructions() {
        return additionalInstructions;
    }

    public void setAdditionalInstructions(String additionalInstructions) {
        this.additionalInstructions = additionalInstructions;
    }
}
