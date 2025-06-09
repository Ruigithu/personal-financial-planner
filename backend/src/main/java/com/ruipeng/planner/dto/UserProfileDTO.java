package com.ruipeng.planner.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class UserProfileDTO {
    private String fullName;
    private Integer age;
    private String occupation;
    private String education;

    private BigDecimal monthlyIncome;
    private BigDecimal monthlyExpenses;
    private BigDecimal totalSavings;
    private BigDecimal totalDebt;

    private Integer riskTolerance;
    private String investmentHorizon;
    private List<String> currentInvestments;
    private List<String> investmentInterests;

    private List<String> financialGoals;
    private Integer retirementAge;
    private BigDecimal monthlySavings;

    public UserProfileDTO(String fullName, Integer age, String occupation, String education, BigDecimal monthlyIncome, BigDecimal monthlyExpenses, BigDecimal totalSavings, BigDecimal totalDebt, Integer riskTolerance, String investmentHorizon, List<String> currentInvestments, List<String> investmentInterests, List<String> financialGoals, Integer retirementAge, BigDecimal monthlySavings) {
        this.fullName = fullName;
        this.age = age;
        this.occupation = occupation;
        this.education = education;
        this.monthlyIncome = monthlyIncome;
        this.monthlyExpenses = monthlyExpenses;
        this.totalSavings = totalSavings;
        this.totalDebt = totalDebt;
        this.riskTolerance = riskTolerance;
        this.investmentHorizon = investmentHorizon;
        this.currentInvestments = currentInvestments;
        this.investmentInterests = investmentInterests;
        this.financialGoals = financialGoals;
        this.retirementAge = retirementAge;
        this.monthlySavings = monthlySavings;
    }


    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
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

    public List<String> getCurrentInvestments() {
        return currentInvestments;
    }

    public void setCurrentInvestments(List<String> currentInvestments) {
        this.currentInvestments = currentInvestments;
    }

    public List<String> getInvestmentInterests() {
        return investmentInterests;
    }

    public void setInvestmentInterests(List<String> investmentInterests) {
        this.investmentInterests = investmentInterests;
    }

    public List<String> getFinancialGoals() {
        return financialGoals;
    }

    public void setFinancialGoals(List<String> financialGoals) {
        this.financialGoals = financialGoals;
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

    public String getInvestmentHorizon() {
        return investmentHorizon;
    }

    public void setInvestmentHorizon(String investmentHorizon) {
        this.investmentHorizon = investmentHorizon;
    }
}
