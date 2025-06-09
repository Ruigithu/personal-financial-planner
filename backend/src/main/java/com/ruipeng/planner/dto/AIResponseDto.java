package com.ruipeng.planner.dto;


import java.util.List;
import java.util.Map;

public class AIResponseDto {
    private Integer healthScore;
    private String healthAssessment;
    private Map<String, Integer> assetAllocation;
    private List<Map<String, Object>> goalTimeline;
    private List<Map<String, Object>> investmentRecommendations;
    private List<Map<String, Object>> actionPlan;

    // Getters and Setters
    public Integer getHealthScore() {
        return healthScore;
    }

    public void setHealthScore(Integer healthScore) {
        this.healthScore = healthScore;
    }

    public String getHealthAssessment() {
        return healthAssessment;
    }

    public void setHealthAssessment(String healthAssessment) {
        this.healthAssessment = healthAssessment;
    }

    public Map<String, Integer> getAssetAllocation() {
        return assetAllocation;
    }

    public void setAssetAllocation(Map<String, Integer> assetAllocation) {
        this.assetAllocation = assetAllocation;
    }

    public List<Map<String, Object>> getGoalTimeline() {
        return goalTimeline;
    }

    public void setGoalTimeline(List<Map<String, Object>> goalTimeline) {
        this.goalTimeline = goalTimeline;
    }

    public List<Map<String, Object>> getInvestmentRecommendations() {
        return investmentRecommendations;
    }

    public void setInvestmentRecommendations(List<Map<String, Object>> investmentRecommendations) {
        this.investmentRecommendations = investmentRecommendations;
    }

    public List<Map<String, Object>> getActionPlan() {
        return actionPlan;
    }

    public void setActionPlan(List<Map<String, Object>> actionPlan) {
        this.actionPlan = actionPlan;
    }
}
