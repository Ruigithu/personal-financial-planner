package com.ruipeng.planner.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "financial_plans")
@Data
public class FinancialPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "creation_date")
    private LocalDateTime creationDate;

    @Column(name = "plan_name")
    private String planName;

    @Column(name = "health_score")
    private Integer healthScore;

    @Column(name = "health_assessment")
    private String healthAssessment;

    @Column(name = "share_code", unique = true)
    private String shareCode;

    // JSON列存储规划详情
    @Column(name = "asset_allocation", columnDefinition = "TEXT")
    private String assetAllocationJson;

    @Column(name = "goal_timeline", columnDefinition = "TEXT")
    private String goalTimelineJson;

    @Column(name = "investment_recommendations", columnDefinition = "TEXT")
    private String investmentRecommendationsJson;

    @Column(name = "action_plan", columnDefinition = "TEXT")
    private String actionPlanJson;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

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

    public String getShareCode() {
        return shareCode;
    }

    public void setShareCode(String shareCode) {
        this.shareCode = shareCode;
    }

    public String getAssetAllocationJson() {
        return assetAllocationJson;
    }

    public void setAssetAllocationJson(String assetAllocationJson) {
        this.assetAllocationJson = assetAllocationJson;
    }

    public String getGoalTimelineJson() {
        return goalTimelineJson;
    }

    public void setGoalTimelineJson(String goalTimelineJson) {
        this.goalTimelineJson = goalTimelineJson;
    }

    public String getInvestmentRecommendationsJson() {
        return investmentRecommendationsJson;
    }

    public void setInvestmentRecommendationsJson(String investmentRecommendationsJson) {
        this.investmentRecommendationsJson = investmentRecommendationsJson;
    }

    public String getActionPlanJson() {
        return actionPlanJson;
    }

    public void setActionPlanJson(String actionPlanJson) {
        this.actionPlanJson = actionPlanJson;
    }
}