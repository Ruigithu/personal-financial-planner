package com.ruipeng.planner.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruipeng.planner.dto.FinancialPlanResponse;
import com.ruipeng.planner.dto.UserProfileDTO;
import com.ruipeng.planner.entity.EducationLevel;
import com.ruipeng.planner.entity.UserProfile;
import com.ruipeng.planner.repository.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserProfileRepository mockRepository;

    @Mock
    private ObjectMapper mockObjectMapper;

    private UserProfileService userProfileService;
    private UserProfileDTO testUserProfileDTO;
    private UserProfile testUserProfile;

    @BeforeEach
    void setUp() {
        userProfileService = new UserProfileService(mockRepository, mockObjectMapper);

        // Setup test data
        testUserProfileDTO = createTestUserProfileDTO();
        testUserProfile = createTestUserProfile();
    }

    @Test
    void should_save_profile_successfully() throws JsonProcessingException {
        // arrange
        when(mockObjectMapper.writeValueAsString(any())).thenReturn("[]");
        when(mockRepository.save(any(UserProfile.class))).thenReturn(testUserProfile);

        // act
        UserProfile result = userProfileService.saveProfile(testUserProfileDTO);

        // assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(testUserProfile);

        // Verify repository save was called
        ArgumentCaptor<UserProfile> profileCaptor = ArgumentCaptor.forClass(UserProfile.class);
        verify(mockRepository).save(profileCaptor.capture());

        UserProfile capturedProfile = profileCaptor.getValue();
        assertThat(capturedProfile.getAge()).isEqualTo(testUserProfileDTO.getAge());
        assertThat(capturedProfile.getOccupation()).isEqualTo(testUserProfileDTO.getOccupation());
        assertThat(capturedProfile.getEducationLevel()).isEqualTo(EducationLevel.valueOf(testUserProfileDTO.getEducation()));
        assertThat(capturedProfile.getMonthlyIncome()).isEqualTo(testUserProfileDTO.getMonthlyIncome());
        assertThat(capturedProfile.getMonthlyExpenses()).isEqualTo(testUserProfileDTO.getMonthlyExpenses());
        assertThat(capturedProfile.getTotalSavings()).isEqualTo(testUserProfileDTO.getTotalSavings());
        assertThat(capturedProfile.getTotalDebt()).isEqualTo(testUserProfileDTO.getTotalDebt());
        assertThat(capturedProfile.getRiskTolerance()).isEqualTo(testUserProfileDTO.getRiskTolerance());
        assertThat(capturedProfile.getInvestmentHorizon()).isEqualTo(testUserProfileDTO.getInvestmentHorizon());
        assertThat(capturedProfile.getRetirementAge()).isEqualTo(testUserProfileDTO.getRetirementAge());
        assertThat(capturedProfile.getMonthlySavings()).isEqualTo(testUserProfileDTO.getMonthlySavings());

        // Verify JSON serialization was called
        verify(mockObjectMapper, times(3)).writeValueAsString(any());
    }

    @Test
    void should_handle_json_serialization_exception() throws JsonProcessingException {
        // arrange
        // 使用一个有效的教育等级，这样异常会在JSON序列化时发生，而不是在枚举转换时
        testUserProfileDTO.setEducation("HIGH_SCHOOL");
        when(mockObjectMapper.writeValueAsString(any()))
                .thenThrow(new JsonProcessingException("Serialization failed") {});

        // act & assert
        assertThatThrownBy(() -> userProfileService.saveProfile(testUserProfileDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Error serializing JSON")
                .hasCauseInstanceOf(JsonProcessingException.class);

        // Verify repository save was not called
        verify(mockRepository, never()).save(any());
    }

    @Test
    void should_serialize_json_fields_correctly() throws JsonProcessingException {
        // arrange
        String currentInvestmentsJson = "[\"stocks\", \"bonds\"]";
        String investmentInterestsJson = "[\"real estate\", \"crypto\"]";
        String financialGoalsJson = "[\"retirement\", \"house\"]";

        when(mockObjectMapper.writeValueAsString(testUserProfileDTO.getCurrentInvestments()))
                .thenReturn(currentInvestmentsJson);
        when(mockObjectMapper.writeValueAsString(testUserProfileDTO.getInvestmentInterests()))
                .thenReturn(investmentInterestsJson);
        when(mockObjectMapper.writeValueAsString(testUserProfileDTO.getFinancialGoals()))
                .thenReturn(financialGoalsJson);
        when(mockRepository.save(any(UserProfile.class))).thenReturn(testUserProfile);

        // act
        userProfileService.saveProfile(testUserProfileDTO);

        // assert
        ArgumentCaptor<UserProfile> profileCaptor = ArgumentCaptor.forClass(UserProfile.class);
        verify(mockRepository).save(profileCaptor.capture());

        UserProfile capturedProfile = profileCaptor.getValue();
        assertThat(capturedProfile.getCurrentInvestmentsJson()).isEqualTo(currentInvestmentsJson);
        assertThat(capturedProfile.getInvestmentInterestsJson()).isEqualTo(investmentInterestsJson);
        assertThat(capturedProfile.getFinancialGoalsJson()).isEqualTo(financialGoalsJson);

        // Verify the exact objects were serialized
        verify(mockObjectMapper).writeValueAsString(testUserProfileDTO.getCurrentInvestments());
        verify(mockObjectMapper).writeValueAsString(testUserProfileDTO.getInvestmentInterests());
        verify(mockObjectMapper).writeValueAsString(testUserProfileDTO.getFinancialGoals());
    }

    @Test
    void should_generate_plan_with_high_score() throws JsonProcessingException {
        // arrange - High income, low expenses, low debt, good savings
        UserProfileDTO highScoreDTO = createHighScoreUserProfileDTO();
        when(mockObjectMapper.writeValueAsString(any())).thenReturn("[]");
        when(mockRepository.save(any(UserProfile.class))).thenReturn(testUserProfile);

        // act
        FinancialPlanResponse response = userProfileService.generatePlan(highScoreDTO);

        // assert
        assertThat(response).isNotNull();
        assertThat(response.getHealthScore()).isGreaterThan(75); // Should be higher than base score
        verify(mockRepository).save(any(UserProfile.class)); // Verify profile was saved
    }

    @Test
    void should_generate_plan_with_low_score() throws JsonProcessingException {
        // arrange - Low income, high expenses, high debt, low savings
        UserProfileDTO lowScoreDTO = createLowScoreUserProfileDTO();
        when(mockObjectMapper.writeValueAsString(any())).thenReturn("[]");
        when(mockRepository.save(any(UserProfile.class))).thenReturn(testUserProfile);

        // act
        FinancialPlanResponse response = userProfileService.generatePlan(lowScoreDTO);

        // assert
        assertThat(response).isNotNull();
        assertThat(response.getHealthScore()).isLessThan(75); // Should be lower than base score
        verify(mockRepository).save(any(UserProfile.class));
    }

    @Test
    void should_calculate_score_with_excellent_income_expense_ratio() throws JsonProcessingException {
        // arrange - Income > 2x expenses
        UserProfileDTO dto = createTestUserProfileDTO();
        dto.setMonthlyIncome(new BigDecimal("10000"));
        dto.setMonthlyExpenses(new BigDecimal("4000")); // Ratio = 2.5 > 2
        dto.setTotalDebt(BigDecimal.ZERO);

        when(mockObjectMapper.writeValueAsString(any())).thenReturn("[]");
        when(mockRepository.save(any(UserProfile.class))).thenReturn(testUserProfile);

        // act
        FinancialPlanResponse response = userProfileService.generatePlan(dto);

        // assert
        assertThat(response.getHealthScore()).isEqualTo(100); // 75 + 5 (good ratio) + 10 (low debt) + 10 (good emergency fund) = 100
    }

    @Test
    void should_calculate_score_with_poor_income_expense_ratio() throws JsonProcessingException {
        // arrange - Income < 1.2x expenses
        UserProfileDTO dto = createTestUserProfileDTO();
        dto.setMonthlyIncome(new BigDecimal("5000"));
        dto.setMonthlyExpenses(new BigDecimal("4500")); // Ratio = 1.11 < 1.2
        dto.setTotalDebt(BigDecimal.ZERO);

        when(mockObjectMapper.writeValueAsString(any())).thenReturn("[]");
        when(mockRepository.save(any(UserProfile.class))).thenReturn(testUserProfile);

        // act
        FinancialPlanResponse response = userProfileService.generatePlan(dto);

        // assert
        assertThat(response.getHealthScore()).isEqualTo(75); // 75 - 10 (poor ratio) + 10 (low debt) + 0 (emergency fund insufficient but > expenses)
    }

    @Test
    void should_calculate_score_with_high_debt_ratio() throws JsonProcessingException {
        // arrange - Debt > 50% of annual income
        UserProfileDTO dto = createTestUserProfileDTO();
        dto.setMonthlyIncome(new BigDecimal("5000")); // Annual: 60,000
        dto.setTotalDebt(new BigDecimal("35000")); // 58% of annual income

        when(mockObjectMapper.writeValueAsString(any())).thenReturn("[]");
        when(mockRepository.save(any(UserProfile.class))).thenReturn(testUserProfile);

        // act
        FinancialPlanResponse response = userProfileService.generatePlan(dto);

        // assert
        assertThat(response.getHealthScore()).isEqualTo(50); // 75 - 10 (poor ratio 1.0 < 1.2) - 15 (high debt) + 0 (emergency fund insufficient)
    }

    @Test
    void should_calculate_score_with_low_debt_ratio() throws JsonProcessingException {
        // arrange - Debt < 20% of annual income
        UserProfileDTO dto = createTestUserProfileDTO();
        dto.setMonthlyIncome(new BigDecimal("5000")); // Annual: 60,000
        dto.setTotalDebt(new BigDecimal("10000")); // 16.7% of annual income

        when(mockObjectMapper.writeValueAsString(any())).thenReturn("[]");
        when(mockRepository.save(any(UserProfile.class))).thenReturn(testUserProfile);

        // act
        FinancialPlanResponse response = userProfileService.generatePlan(dto);

        // assert
        assertThat(response.getHealthScore()).isEqualTo(75); // 75 - 10 (poor ratio 1.0 < 1.2) + 10 (low debt) + 0 (emergency fund insufficient)
    }

    @Test
    void should_calculate_score_with_good_emergency_fund() throws JsonProcessingException {
        // arrange - Savings > 6x monthly expenses
        UserProfileDTO dto = createTestUserProfileDTO();
        dto.setMonthlyExpenses(new BigDecimal("3000"));
        dto.setTotalSavings(new BigDecimal("20000")); // > 6x expenses (18,000)
        dto.setTotalDebt(BigDecimal.ZERO);

        when(mockObjectMapper.writeValueAsString(any())).thenReturn("[]");
        when(mockRepository.save(any(UserProfile.class))).thenReturn(testUserProfile);

        // act
        FinancialPlanResponse response = userProfileService.generatePlan(dto);

        // assert
        assertThat(response.getHealthScore()).isEqualTo(100); // 75 + 5 (good ratio 8000/3000=2.67 > 2) + 10 (low debt) + 10 (good emergency fund) = 100
    }

    @Test
    void should_calculate_score_with_poor_emergency_fund() throws JsonProcessingException {
        // arrange - Savings < monthly expenses
        UserProfileDTO dto = createTestUserProfileDTO();
        dto.setMonthlyExpenses(new BigDecimal("3000"));
        dto.setTotalSavings(new BigDecimal("2000")); // < monthly expenses
        dto.setTotalDebt(BigDecimal.ZERO);

        when(mockObjectMapper.writeValueAsString(any())).thenReturn("[]");
        when(mockRepository.save(any(UserProfile.class))).thenReturn(testUserProfile);

        // act
        FinancialPlanResponse response = userProfileService.generatePlan(dto);

        // assert
        assertThat(response.getHealthScore()).isEqualTo(85); // 75 + 5 (good ratio 8000/3000=2.67 > 2) + 10 (low debt) - 5 (poor emergency fund) = 85
    }

    @Test
    void should_handle_zero_and_null_values() throws JsonProcessingException {
        // arrange
        UserProfileDTO dto = createTestUserProfileDTO();
        dto.setMonthlyIncome(null);
        dto.setMonthlyExpenses(BigDecimal.ZERO);
        dto.setTotalSavings(null);
        dto.setTotalDebt(null);

        when(mockObjectMapper.writeValueAsString(any())).thenReturn("[]");
        when(mockRepository.save(any(UserProfile.class))).thenReturn(testUserProfile);

        // act
        FinancialPlanResponse response = userProfileService.generatePlan(dto);

        // assert
        assertThat(response).isNotNull();
        assertThat(response.getHealthScore()).isEqualTo(75); // Base score, no adjustments
    }

    @Test
    void should_enforce_score_boundaries() throws JsonProcessingException {
        // arrange - Create scenario that would result in very low score
        UserProfileDTO dto = createTestUserProfileDTO();
        dto.setMonthlyIncome(new BigDecimal("1000"));
        dto.setMonthlyExpenses(new BigDecimal("900")); // Poor ratio
        dto.setTotalDebt(new BigDecimal("50000")); // Very high debt
        dto.setTotalSavings(new BigDecimal("100")); // Very low savings

        when(mockObjectMapper.writeValueAsString(any())).thenReturn("[]");
        when(mockRepository.save(any(UserProfile.class))).thenReturn(testUserProfile);

        // act
        FinancialPlanResponse response = userProfileService.generatePlan(dto);

        // assert
        assertThat(response.getHealthScore()).isBetween(0, 100); // Should be clamped to valid range
    }

    @Test
    void should_handle_division_by_zero_scenarios() throws JsonProcessingException {
        // arrange
        UserProfileDTO dto = createTestUserProfileDTO();
        dto.setMonthlyIncome(BigDecimal.ZERO);
        dto.setMonthlyExpenses(BigDecimal.ZERO);

        when(mockObjectMapper.writeValueAsString(any())).thenReturn("[]");
        when(mockRepository.save(any(UserProfile.class))).thenReturn(testUserProfile);

        // act & assert - Should not throw exception
        assertThatNoException().isThrownBy(() -> userProfileService.generatePlan(dto));
    }

    @Test
    void should_map_education_level_correctly() throws JsonProcessingException {
        // arrange
        testUserProfileDTO.setEducation("BACHELOR"); // 使用正确的枚举值
        when(mockObjectMapper.writeValueAsString(any())).thenReturn("[]");
        when(mockRepository.save(any(UserProfile.class))).thenReturn(testUserProfile);

        // act
        userProfileService.saveProfile(testUserProfileDTO);

        // assert
        ArgumentCaptor<UserProfile> profileCaptor = ArgumentCaptor.forClass(UserProfile.class);
        verify(mockRepository).save(profileCaptor.capture());

        UserProfile capturedProfile = profileCaptor.getValue();
        assertThat(capturedProfile.getEducationLevel()).isEqualTo(EducationLevel.BACHELOR);
    }

    @Test
    void should_handle_invalid_education_level() throws JsonProcessingException {
        // arrange
        testUserProfileDTO.setEducation("INVALID_EDUCATION");
        lenient().when(mockObjectMapper.writeValueAsString(any())).thenReturn("[]");

        // act & assert
        assertThatThrownBy(() -> userProfileService.saveProfile(testUserProfileDTO))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // Helper methods

    private UserProfileDTO createTestUserProfileDTO() {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setAge(30);
        dto.setOccupation("Software Engineer");
        dto.setEducation("MASTER"); // 使用正确的枚举值
        dto.setMonthlyIncome(new BigDecimal("8000"));
        dto.setMonthlyExpenses(new BigDecimal("5000"));
        dto.setTotalSavings(new BigDecimal("25000"));
        dto.setTotalDebt(new BigDecimal("15000"));
        dto.setRiskTolerance(3);
        dto.setInvestmentHorizon("LONG_TERM");
        dto.setRetirementAge(65);
        dto.setMonthlySavings(new BigDecimal("1500"));

        // Mock lists for JSON serialization
        dto.setCurrentInvestments(Arrays.asList("401k", "IRA"));
        dto.setInvestmentInterests(Arrays.asList("Stocks", "Bonds"));
        dto.setFinancialGoals(Arrays.asList("Retirement", "House"));

        return dto;
    }

        private UserProfile createTestUserProfile() {
            UserProfile profile = new UserProfile();
            profile.setId(1L);
            profile.setAge(30);
            profile.setOccupation("Software Engineer");
            profile.setEducationLevel(EducationLevel.MASTER); // 使用正确的枚举值
            profile.setMonthlyIncome(new BigDecimal("8000"));
            profile.setMonthlyExpenses(new BigDecimal("5000"));
            profile.setTotalSavings(new BigDecimal("25000"));
            profile.setTotalDebt(new BigDecimal("15000"));
            profile.setRiskTolerance(5);
            profile.setInvestmentHorizon("LONG_TERM");
            profile.setRetirementAge(65);
            profile.setMonthlySavings(new BigDecimal("1500"));
            profile.setCurrentInvestmentsJson("[]");
            profile.setInvestmentInterestsJson("[]");
            profile.setFinancialGoalsJson("[]");
            return profile;
        }

        private UserProfileDTO createHighScoreUserProfileDTO() {
            UserProfileDTO dto = createTestUserProfileDTO();
            dto.setMonthlyIncome(new BigDecimal("15000")); // High income
            dto.setMonthlyExpenses(new BigDecimal("5000"));  // Low relative expenses
            dto.setTotalSavings(new BigDecimal("50000"));    // Good emergency fund
            dto.setTotalDebt(new BigDecimal("5000"));        // Low debt
            return dto;
        }

        private UserProfileDTO createLowScoreUserProfileDTO() {
            UserProfileDTO dto = createTestUserProfileDTO();
            dto.setMonthlyIncome(new BigDecimal("3000"));    // Low income
            dto.setMonthlyExpenses(new BigDecimal("2800"));  // High relative expenses
            dto.setTotalSavings(new BigDecimal("1000"));     // Poor emergency fund
            dto.setTotalDebt(new BigDecimal("25000"));       // High debt
            return dto;
        }
    }