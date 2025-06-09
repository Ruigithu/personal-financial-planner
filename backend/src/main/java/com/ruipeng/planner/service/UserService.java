package com.ruipeng.planner.service;

import com.ruipeng.planner.dto.UserProfileUpdateDto;
import com.ruipeng.planner.entity.User;
import com.ruipeng.planner.entity.UserProfile;
import com.ruipeng.planner.repository.UserProfileRepository;
import com.ruipeng.planner.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, UserProfileRepository userProfileRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    public UserProfile getUserProfileByUserId(Long userId) {
        return userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("User profile not found for user id: " + userId));
    }

    @Transactional
    public UserProfile updateUserProfile(Long userId, UserProfileUpdateDto profileUpdate) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("User profile not found for user id: " + userId));

        if (profileUpdate.getAge() != null) {
            profile.setAge(profileUpdate.getAge());
        }
        if (profileUpdate.getOccupation() != null) {
            profile.setOccupation(profileUpdate.getOccupation());
        }
        if (profileUpdate.getEducationLevel() != null) {
            profile.setEducationLevel(profileUpdate.getEducationLevel());
        }
        if (profileUpdate.getMonthlyIncome() != null) {
            profile.setMonthlyIncome(profileUpdate.getMonthlyIncome());
        }
        if (profileUpdate.getMonthlyExpenses() != null) {
            profile.setMonthlyExpenses(profileUpdate.getMonthlyExpenses());
        }
        if (profileUpdate.getTotalSavings() != null) {
            profile.setTotalSavings(profileUpdate.getTotalSavings());
        }
        if (profileUpdate.getTotalDebt() != null) {
            profile.setTotalDebt(profileUpdate.getTotalDebt());
        }
        if (profileUpdate.getRiskTolerance() != null) {
            profile.setRiskTolerance(profileUpdate.getRiskTolerance());
        }
        if (profileUpdate.getInvestmentHorizon() != null) {
            profile.setInvestmentHorizon(profileUpdate.getInvestmentHorizon());
        }
        if (profileUpdate.getCurrentInvestmentsJson() != null) {
            profile.setCurrentInvestmentsJson(profileUpdate.getCurrentInvestmentsJson());
        }
        if (profileUpdate.getInvestmentInterestsJson() != null) {
            profile.setInvestmentInterestsJson(profileUpdate.getInvestmentInterestsJson());
        }
        if (profileUpdate.getFinancialGoalsJson() != null) {
            profile.setFinancialGoalsJson(profileUpdate.getFinancialGoalsJson());
        }
        if (profileUpdate.getRetirementAge() != null) {
            profile.setRetirementAge(profileUpdate.getRetirementAge());
        }
        if (profileUpdate.getMonthlySavings() != null) {
            profile.setMonthlySavings(profileUpdate.getMonthlySavings());
        }

        return userProfileRepository.save(profile);
    }

    @Transactional
    public void updateUserPassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        // Verify the current password
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Update with the new password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}