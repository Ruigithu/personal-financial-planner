package com.ruipeng.planner.entity;


import jakarta.persistence.*;
import lombok.Data;


import java.math.BigDecimal;


@Entity
@Table(name = "user_profiles")
@Data
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private Integer age;
    private String occupation;

    @Column(name = "education_level")
    @Enumerated(EnumType.STRING)
    private EducationLevel educationLevel;

    @Column(name = "monthly_income")
    private BigDecimal monthlyIncome;

    @Column(name = "monthly_expenses")
    private BigDecimal monthlyExpenses;

    @Column(name = "total_savings")
    private BigDecimal totalSavings;

    @Column(name = "total_debt")
    private BigDecimal totalDebt;

    @Column(name = "risk_tolerance")
    private Integer riskTolerance;

    @Column(name = "investment_horizon")
    private String investmentHorizon;

    // JSON列存储复杂数据
    @Column(name = "current_investments_json", columnDefinition = "TEXT")
    private String currentInvestmentsJson;

    @Column(name = "investment_interests_json", columnDefinition = "TEXT")
    private String investmentInterestsJson;

    @Column(name = "financial_goals_json", columnDefinition = "TEXT")
    private String financialGoalsJson;

    @Column(name = "retirement_age")
    private Integer retirementAge;

    @Column(name = "monthly_savings")
    private BigDecimal monthlySavings;

    public UserProfile() {
    }

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

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public EducationLevel getEducationLevel() {
        return educationLevel;
    }

    public void setEducationLevel(EducationLevel educationLevel) {
        this.educationLevel = educationLevel;
    }

    public BigDecimal getMonthlyIncome() {
        return monthlyIncome;
    }

    public void setMonthlyIncome(BigDecimal monthlyIncome) {
        this.monthlyIncome = monthlyIncome;
    }

    public BigDecimal getMonthlyExpenses() {
        return monthlyExpenses;
    }

    public void setMonthlyExpenses(BigDecimal monthlyExpenses) {
        this.monthlyExpenses = monthlyExpenses;
    }

    public BigDecimal getTotalSavings() {
        return totalSavings;
    }

    public void setTotalSavings(BigDecimal totalSavings) {
        this.totalSavings = totalSavings;
    }

    public BigDecimal getTotalDebt() {
        return totalDebt;
    }

    public void setTotalDebt(BigDecimal totalDebt) {
        this.totalDebt = totalDebt;
    }

    public Integer getRiskTolerance() {
        return riskTolerance;
    }

    public void setRiskTolerance(Integer riskTolerance) {
        this.riskTolerance = riskTolerance;
    }

    public String getInvestmentHorizon() {
        return investmentHorizon;
    }

    public void setInvestmentHorizon(String investmentHorizon) {
        this.investmentHorizon = investmentHorizon;
    }

    public String getCurrentInvestmentsJson() {
        return currentInvestmentsJson;
    }

    public void setCurrentInvestmentsJson(String currentInvestmentsJson) {
        this.currentInvestmentsJson = currentInvestmentsJson;
    }

    public String getInvestmentInterestsJson() {
        return investmentInterestsJson;
    }

    public void setInvestmentInterestsJson(String investmentInterestsJson) {
        this.investmentInterestsJson = investmentInterestsJson;
    }

    public String getFinancialGoalsJson() {
        return financialGoalsJson;
    }

    public void setFinancialGoalsJson(String financialGoalsJson) {
        this.financialGoalsJson = financialGoalsJson;
    }

    public Integer getRetirementAge() {
        return retirementAge;
    }

    public void setRetirementAge(Integer retirementAge) {
        this.retirementAge = retirementAge;
    }

    public BigDecimal getMonthlySavings() {
        return monthlySavings;
    }

    public void setMonthlySavings(BigDecimal monthlySavings) {
        this.monthlySavings = monthlySavings;
    }
}