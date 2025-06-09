package com.ruipeng.planner.dto;


import com.ruipeng.planner.entity.EducationLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class UserProfileUpdateDto {
    private Integer age;
    private String occupation;
    private EducationLevel educationLevel;
    private BigDecimal monthlyIncome;
    private BigDecimal monthlyExpenses;
    private BigDecimal totalSavings;
    private BigDecimal totalDebt;
    private Integer riskTolerance;
    private String investmentHorizon;
    private String currentInvestmentsJson;
    private String investmentInterestsJson;
    private String financialGoalsJson;
    private Integer retirementAge;
    private BigDecimal monthlySavings;

    // Getters and Setters
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
