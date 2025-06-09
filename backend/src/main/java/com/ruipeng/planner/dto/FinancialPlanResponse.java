package com.ruipeng.planner.dto;


import lombok.Data;

@Data
public class FinancialPlanResponse {
    private Integer healthScore;

    public FinancialPlanResponse(Integer healthScore) {
        this.healthScore = healthScore;
    }

    public FinancialPlanResponse() {
    }

    public Integer getHealthScore() {
        return healthScore;
    }

    public void setHealthScore(Integer healthScore) {
        this.healthScore = healthScore;
    }
}
