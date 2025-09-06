package com.ruipeng.planner.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruipeng.planner.dto.FinancialPlanRequest;
import com.ruipeng.planner.entity.EducationLevel;
import com.ruipeng.planner.entity.FinancialPlan;
import com.ruipeng.planner.entity.User;
import com.ruipeng.planner.entity.UserProfile;
import com.ruipeng.planner.repository.FinancialPlanRepository;
import com.ruipeng.planner.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinancialPlanServiceTest {

    @Mock
    private FinancialPlanRepository financialPlanRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GoogleAIService googleAiService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private FinancialPlanService financialPlanService;

    private User testUser;
    private UserProfile testUserProfile;
    private FinancialPlan testFinancialPlan;
    private FinancialPlanRequest testRequest;

    @BeforeEach
    void setUp() {
        // 创建测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setFirstName("testuserFirstName");
        testUser.setLastName("testuserLastName");
        testUser.setEmail("test@example.com");

        // 创建测试用户资料
        testUserProfile = new UserProfile();
        testUserProfile.setId(1L);
        testUserProfile.setUser(testUser);
        testUserProfile.setAge(30);
        testUserProfile.setOccupation("Software Engineer");
        testUserProfile.setEducationLevel(EducationLevel.BACHELOR);
        testUserProfile.setMonthlyIncome(new BigDecimal("8000"));
        testUserProfile.setMonthlyExpenses(new BigDecimal("5000"));
        testUserProfile.setTotalSavings(new BigDecimal("50000"));
        testUserProfile.setTotalDebt(new BigDecimal("10000"));
        testUserProfile.setRiskTolerance(7);
        testUserProfile.setInvestmentHorizon("Long-term");
        testUserProfile.setRetirementAge(65);
        testUserProfile.setMonthlySavings(new BigDecimal("2000"));

        testUser.setProfile(testUserProfile);

        // 创建测试财务计划
        testFinancialPlan = new FinancialPlan();
        testFinancialPlan.setId(1L);
        testFinancialPlan.setUser(testUser);
        testFinancialPlan.setPlanName("Test Financial Plan");
        testFinancialPlan.setCreationDate(LocalDateTime.now());
        testFinancialPlan.setHealthScore(75);
        testFinancialPlan.setHealthAssessment("Good financial health");
        testFinancialPlan.setAssetAllocationJson("{\"stocks\":60,\"bonds\":30,\"cash\":10}");
        testFinancialPlan.setGoalTimelineJson("[{\"goal\":\"Emergency Fund\",\"timeline\":\"6 months\"}]");
        testFinancialPlan.setInvestmentRecommendationsJson("[{\"type\":\"Index Fund\",\"allocation\":\"40%\"}]");
        testFinancialPlan.setActionPlanJson("[{\"action\":\"Increase savings rate\",\"priority\":\"High\"}]");
        testFinancialPlan.setShareCode("abc12345");

        // 创建测试请求
        testRequest = new FinancialPlanRequest();
        testRequest.setPlanName("Test Plan");
        testRequest.setAdditionalInstructions("Focus on retirement planning");
    }

    @Test
    void getUserFinancialPlans_Success() {
        // Arrange
        Long userId = 1L;
        List<FinancialPlan> expectedPlans = Arrays.asList(testFinancialPlan);
        when(financialPlanRepository.findByUserId(userId)).thenReturn(expectedPlans);

        // Act
        List<FinancialPlan> result = financialPlanService.getUserFinancialPlans(userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertNull(result.get(0).getUser()); // User should be set to null
        verify(financialPlanRepository).findByUserId(userId);
    }

    @Test
    void getFinancialPlanById_Success() {
        // Arrange
        Long planId = 1L;
        when(financialPlanRepository.findById(planId)).thenReturn(Optional.of(testFinancialPlan));

        // Act
        FinancialPlan result = financialPlanService.getFinancialPlanById(planId);

        // Assert
        assertNotNull(result);
        assertEquals(testFinancialPlan.getId(), result.getId());
        assertEquals(testFinancialPlan.getPlanName(), result.getPlanName());
        verify(financialPlanRepository).findById(planId);
    }

    @Test
    void getFinancialPlanById_NotFound() {
        // Arrange
        Long planId = 999L;
        when(financialPlanRepository.findById(planId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> financialPlanService.getFinancialPlanById(planId)
        );
        assertEquals("Financial plan not found with id: " + planId, exception.getMessage());
        verify(financialPlanRepository).findById(planId);
    }

    @Test
    void getFinancialPlanByShareCode_Success() {
        // Arrange
        String shareCode = "abc12345";
        when(financialPlanRepository.findByShareCode(shareCode)).thenReturn(Optional.of(testFinancialPlan));

        // Act
        FinancialPlan result = financialPlanService.getFinancialPlanByShareCode(shareCode);

        // Assert
        assertNotNull(result);
        assertEquals(testFinancialPlan.getShareCode(), result.getShareCode());
        verify(financialPlanRepository).findByShareCode(shareCode);
    }

    @Test
    void getFinancialPlanByShareCode_NotFound() {
        // Arrange
        String shareCode = "invalid";
        when(financialPlanRepository.findByShareCode(shareCode)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> financialPlanService.getFinancialPlanByShareCode(shareCode)
        );
        assertEquals("Financial plan not found with share code: " + shareCode, exception.getMessage());
        verify(financialPlanRepository).findByShareCode(shareCode);
    }

    @Test
    void generateFinancialPlan_Success() throws Exception {
        // Arrange
        Long userId = 1L;
        String aiResponse = "{\"healthScore\":75,\"healthAssessment\":\"Good\"}";
        Map<String, Object> mockAssetAllocation = Map.of("stocks", 60, "bonds", 40);
        List<Map<String, Object>> mockGoalTimeline = List.of(Map.of("goal", "retirement", "years", 30));
        List<Map<String, Object>> mockInvestments = List.of(Map.of("type", "ETF", "allocation", "50%"));
        List<Map<String, Object>> mockActionPlan = List.of(Map.of("action", "Increase savings", "priority", "High"));

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(googleAiService.generateFinancialPlan(testUserProfile, testRequest)).thenReturn(aiResponse);
        when(googleAiService.extractAssetAllocation(aiResponse)).thenReturn(mockAssetAllocation);
        when(googleAiService.extractGoalTimeline(aiResponse)).thenReturn(mockGoalTimeline);
        when(googleAiService.extractInvestmentRecommendations(aiResponse)).thenReturn(mockInvestments);
        when(googleAiService.extractActionPlan(aiResponse)).thenReturn(mockActionPlan);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(financialPlanRepository.save(any(FinancialPlan.class))).thenReturn(testFinancialPlan);

        // Act
        FinancialPlan result = financialPlanService.generateFinancialPlan(userId, testRequest);

        // Assert
        assertNotNull(result);
        verify(userRepository).findById(userId);
        verify(googleAiService).generateFinancialPlan(testUserProfile, testRequest);
        verify(googleAiService).extractAssetAllocation(aiResponse);
        verify(googleAiService).extractGoalTimeline(aiResponse);
        verify(googleAiService).extractInvestmentRecommendations(aiResponse);
        verify(googleAiService).extractActionPlan(aiResponse);
        verify(financialPlanRepository).save(any(FinancialPlan.class));
    }

    @Test
    void generateFinancialPlan_UserNotFound() {
        // Arrange
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> financialPlanService.generateFinancialPlan(userId, testRequest)
        );
        assertEquals("User not found with id: " + userId, exception.getMessage());
        verify(userRepository).findById(userId);
        verifyNoInteractions(googleAiService);
        verifyNoInteractions(financialPlanRepository);
    }

    @Test
    void generateFinancialPlan_UserProfileIncomplete() {
        // Arrange
        Long userId = 1L;
        User userWithoutProfile = new User();
        userWithoutProfile.setId(userId);
        userWithoutProfile.setProfile(null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(userWithoutProfile));

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> financialPlanService.generateFinancialPlan(userId, testRequest)
        );
        assertEquals("User profile is incomplete", exception.getMessage());
        verify(userRepository).findById(userId);
        verifyNoInteractions(googleAiService);
        verifyNoInteractions(financialPlanRepository);
    }

    @Test
    void generateFinancialPlan_AIServiceError() throws Exception {
        // Arrange
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(googleAiService.generateFinancialPlan(testUserProfile, testRequest))
                .thenThrow(new RuntimeException("AI service error"));

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> financialPlanService.generateFinancialPlan(userId, testRequest)
        );

        // 打印实际的异常消息以便调试
        System.out.println("Actual exception message: " + exception.getMessage());
        System.out.println("Actual exception cause: " + exception.getCause());

        // 验证异常消息（根据实际的FinancialPlanService实现调整）
        assertTrue(exception.getMessage().contains("Error parsing AI response") ||
                exception.getMessage().contains("AI service error"));
        verify(userRepository).findById(userId);
        verify(googleAiService).generateFinancialPlan(testUserProfile, testRequest);
        verifyNoInteractions(financialPlanRepository);
    }

    @Test
    void generatePdfReport_Success() throws Exception {
        // Arrange
        Long planId = 1L;
        ObjectMapper realObjectMapper = new ObjectMapper(); // 使用真实的ObjectMapper进行PDF测试
        FinancialPlanService serviceWithRealMapper = new FinancialPlanService(
                financialPlanRepository, userRepository, googleAiService, realObjectMapper);

        when(financialPlanRepository.findById(planId)).thenReturn(Optional.of(testFinancialPlan));

        // Act
        ByteArrayResource result = serviceWithRealMapper.generatePdfReport(planId);

        // Assert
        assertNotNull(result);
        assertTrue(result.contentLength() > 0);
        verify(financialPlanRepository).findById(planId);
    }

    @Test
    void generatePdfReport_PlanNotFound() {
        // Arrange
        Long planId = 999L;
        when(financialPlanRepository.findById(planId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                EntityNotFoundException.class,
                () -> financialPlanService.generatePdfReport(planId)
        );
        verify(financialPlanRepository).findById(planId);
    }

    @Test
    void generatePdfReport_JsonFormattingError() throws Exception {
        // Arrange
        Long planId = 1L;
        when(financialPlanRepository.findById(planId)).thenReturn(Optional.of(testFinancialPlan));
        when(objectMapper.readValue(anyString(), eq(Object.class)))
                .thenThrow(new RuntimeException("JSON parsing error"));

        // Act & Assert - Should not throw exception, should handle gracefully
        ByteArrayResource result = financialPlanService.generatePdfReport(planId);

        assertNotNull(result);
        verify(financialPlanRepository).findById(planId);
    }
}
