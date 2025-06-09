package com.ruipeng.planner.service;
import com.ruipeng.planner.dto.FinancialPlanResponse;
import com.ruipeng.planner.dto.UserProfileDTO;
import com.ruipeng.planner.entity.EducationLevel;
import com.ruipeng.planner.entity.UserProfile;
import com.ruipeng.planner.repository.UserProfileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class UserProfileService {
    private final UserProfileRepository repository;
    private final ObjectMapper objectMapper;

    @Autowired
    public UserProfileService(UserProfileRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public UserProfile saveProfile(UserProfileDTO dto) {
        UserProfile profile = new UserProfile();
        profile.setAge(dto.getAge());
        profile.setOccupation(dto.getOccupation());
        profile.setEducationLevel(EducationLevel.valueOf(dto.getEducation()));
        profile.setMonthlyIncome(dto.getMonthlyIncome());
        profile.setMonthlyExpenses(dto.getMonthlyExpenses());
        profile.setTotalSavings(dto.getTotalSavings());
        profile.setTotalDebt(dto.getTotalDebt());
        profile.setRiskTolerance(dto.getRiskTolerance());
        profile.setInvestmentHorizon(dto.getInvestmentHorizon());
        try {
            profile.setCurrentInvestmentsJson(objectMapper.writeValueAsString(dto.getCurrentInvestments()));
            profile.setInvestmentInterestsJson(objectMapper.writeValueAsString(dto.getInvestmentInterests()));
            profile.setFinancialGoalsJson(objectMapper.writeValueAsString(dto.getFinancialGoals()));
        } catch (Exception e) {
            throw new RuntimeException("Error serializing JSON", e);
        }
        profile.setRetirementAge(dto.getRetirementAge());
        profile.setMonthlySavings(dto.getMonthlySavings());
        return repository.save(profile);
    }

    public FinancialPlanResponse generatePlan(UserProfileDTO dto) {
        // Save profile
        saveProfile(dto);

        // Simple health score calculation
        FinancialPlanResponse response = new FinancialPlanResponse();
        int score = 75; // Base score

        BigDecimal income = dto.getMonthlyIncome() != null ? dto.getMonthlyIncome() : BigDecimal.ZERO;
        BigDecimal expenses = dto.getMonthlyExpenses() != null ? dto.getMonthlyExpenses() : BigDecimal.ZERO;
        BigDecimal savings = dto.getTotalSavings() != null ? dto.getTotalSavings() : BigDecimal.ZERO;
        BigDecimal debt = dto.getTotalDebt() != null ? dto.getTotalDebt() : BigDecimal.ZERO;

        if (income.compareTo(BigDecimal.ZERO) > 0 && expenses.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal ratio = income.divide(expenses, 2, RoundingMode.HALF_UP);
            if (ratio.compareTo(new BigDecimal("2")) > 0) score += 5;
            else if (ratio.compareTo(new BigDecimal("1.2")) < 0) score -= 10;
        }

        if (income.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal debtRatio = debt.divide(income.multiply(new BigDecimal("12")), 2,  RoundingMode.HALF_UP);
            if (debtRatio.compareTo(new BigDecimal("0.5")) > 0) score -= 15;
            else if (debtRatio.compareTo(new BigDecimal("0.2")) < 0) score += 10;
        }

        if (expenses.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal emergencyFund = expenses.multiply(new BigDecimal("6"));
            if (savings.compareTo(emergencyFund) > 0) score += 10;
            else if (savings.compareTo(expenses) < 0) score -= 5;
        }

        score = Math.min(Math.max(score, 0), 100);
        response.setHealthScore(score);
        return response;
    }
}
