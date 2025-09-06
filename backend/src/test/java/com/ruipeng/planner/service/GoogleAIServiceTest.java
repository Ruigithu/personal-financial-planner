package com.ruipeng.planner.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruipeng.planner.dto.FinancialPlanRequest;
import com.ruipeng.planner.entity.UserProfile;
import com.ruipeng.planner.entity.EducationLevel;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleAIServiceTest {

    @Mock
    private GoogleAiGeminiChatModel mockGeminiModel;

    @Mock
    private ChatResponse mockChatResponse;

    @Mock
    private AiMessage mockAiMessage;

    private ObjectMapper objectMapper;
    private GoogleAIService googleAIService;
    private UserProfile mockUserProfile;
    private FinancialPlanRequest mockFinancialPlanRequest;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        // 创建服务实例，使用测试配置
        googleAIService = new GoogleAIService(
                objectMapper,
                "test-api-key",
                0.7,
                2048
        );

        // 使用反射注入mock的geminiModel
        ReflectionTestUtils.setField(googleAIService, "geminiModel", mockGeminiModel);

        // 设置mock用户资料
        setupMockUserProfile();

        // 设置mock请求
        mockFinancialPlanRequest = new FinancialPlanRequest();
        mockFinancialPlanRequest.setPlanName("Retirement Strategy 2025");
        mockFinancialPlanRequest.setAdditionalInstructions("Focus on low-risk investments");
    }

    private void setupMockUserProfile() {
        mockUserProfile = new UserProfile();
        mockUserProfile.setId(1L);
        mockUserProfile.setAge(35);
        mockUserProfile.setOccupation("Software Engineer");
        mockUserProfile.setEducationLevel(EducationLevel.BACHELOR);
        mockUserProfile.setMonthlyIncome(new BigDecimal("15000"));
        mockUserProfile.setMonthlyExpenses(new BigDecimal("8000"));
        mockUserProfile.setTotalSavings(new BigDecimal("200000"));
        mockUserProfile.setTotalDebt(new BigDecimal("50000"));
        mockUserProfile.setRiskTolerance(3); // Integer: 1-5 scale
        mockUserProfile.setInvestmentHorizon("LONG_TERM"); // String value
        mockUserProfile.setRetirementAge(65);
        mockUserProfile.setMonthlySavings(new BigDecimal("5000"));

        // 设置JSON字段
        mockUserProfile.setCurrentInvestmentsJson("[{\"type\":\"stocks\",\"amount\":100000}]");
        mockUserProfile.setInvestmentInterestsJson("[\"technology\",\"healthcare\"]");
        mockUserProfile.setFinancialGoalsJson("[{\"goal\":\"retirement\",\"amount\":2000000,\"timeline\":30}]");
    }

    @Test
    void should_generate_financial_plan_successfully() {
        // arrange
        String mockAIResponse = """
            {
                "healthScore": 85,
                "healthAssessment": "Your financial health is good with strong savings habits.",
                "assetAllocation": {
                    "stocks": 60,
                    "bonds": 30,
                    "cash": 10
                },
                "goalTimeline": [
                    {
                        "goal": "Emergency Fund",
                        "timeline": "6 months",
                        "targetAmount": 50000
                    }
                ],
                "investmentRecommendations": [
                    {
                        "type": "Index Funds",
                        "allocation": "40%",
                        "reasoning": "Low-cost diversification"
                    }
                ],
                "actionPlan": [
                    {
                        "step": 1,
                        "action": "Build emergency fund",
                        "priority": "High"
                    }
                ]
            }""";

        when(mockGeminiModel.chat(any(ChatRequest.class))).thenReturn(mockChatResponse);
        when(mockChatResponse.aiMessage()).thenReturn(mockAiMessage);
        when(mockAiMessage.text()).thenReturn(mockAIResponse);

        // act
        String result = googleAIService.generateFinancialPlan(mockUserProfile, mockFinancialPlanRequest);

        // assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(mockAIResponse);

        verify(mockGeminiModel).chat(any(ChatRequest.class));
        verify(mockChatResponse).aiMessage();
        verify(mockAiMessage).text();
    }

    @Test
    void should_clean_json_response_with_code_blocks() {
        // arrange
        String mockAIResponseWithCodeBlocks = """
            ```json
            {
                "healthScore": 85,
                "healthAssessment": "Good financial health"
            }
            ```
            """;

        String expectedCleanedResponse = """
            {
                "healthScore": 85,
                "healthAssessment": "Good financial health"
            }""";

        when(mockGeminiModel.chat(any(ChatRequest.class))).thenReturn(mockChatResponse);
        when(mockChatResponse.aiMessage()).thenReturn(mockAiMessage);
        when(mockAiMessage.text()).thenReturn(mockAIResponseWithCodeBlocks);

        // act
        String result = googleAIService.generateFinancialPlan(mockUserProfile, mockFinancialPlanRequest);

        // assert
        assertThat(result.trim()).isEqualTo(expectedCleanedResponse);
    }

    @Test
    void should_throw_exception_when_ai_response_is_invalid_json() {
        // arrange
        String invalidJsonResponse = "This is not a valid JSON response";

        when(mockGeminiModel.chat(any(ChatRequest.class))).thenReturn(mockChatResponse);
        when(mockChatResponse.aiMessage()).thenReturn(mockAiMessage);
        when(mockAiMessage.text()).thenReturn(invalidJsonResponse);

        // act & assert
        assertThatThrownBy(() -> googleAIService.generateFinancialPlan(mockUserProfile, mockFinancialPlanRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid JSON response from AI model");

        verify(mockGeminiModel).chat(any(ChatRequest.class));
    }

    @Test
    void should_throw_exception_when_ai_model_fails() {
        // arrange
        when(mockGeminiModel.chat(any(ChatRequest.class)))
                .thenThrow(new RuntimeException("AI model connection failed"));

        // act & assert
        assertThatThrownBy(() -> googleAIService.generateFinancialPlan(mockUserProfile, mockFinancialPlanRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to generate financial plan");

        verify(mockGeminiModel).chat(any(ChatRequest.class));
    }

    @Test
    void should_handle_user_profile_with_null_json_fields() {
        // arrange
        mockUserProfile.setCurrentInvestmentsJson(null);
        mockUserProfile.setInvestmentInterestsJson(null);
        mockUserProfile.setFinancialGoalsJson(null);

        String mockAIResponse = """
            {
                "healthScore": 75,
                "healthAssessment": "Basic financial profile"
            }""";



        when(mockGeminiModel.chat(any(ChatRequest.class))).thenReturn(mockChatResponse);
        when(mockChatResponse.aiMessage()).thenReturn(mockAiMessage);
        when(mockAiMessage.text()).thenReturn(mockAIResponse);

        // act
        String result = googleAIService.generateFinancialPlan(mockUserProfile, mockFinancialPlanRequest);

        // assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(mockAIResponse);

        verify(mockGeminiModel).chat(any(ChatRequest.class));
    }

    @Test
    void should_handle_invalid_json_in_user_profile_gracefully() {
        // arrange
        mockUserProfile.setCurrentInvestmentsJson("invalid json");
        mockUserProfile.setInvestmentInterestsJson("{malformed json");
        mockUserProfile.setFinancialGoalsJson("not json at all");

        String mockAIResponse = """
            {
                "healthScore": 70,
                "healthAssessment": "Profile with parsing issues handled"
            }""";

        when(mockGeminiModel.chat(any(ChatRequest.class))).thenReturn(mockChatResponse);
        when(mockChatResponse.aiMessage()).thenReturn(mockAiMessage);
        when(mockAiMessage.text()).thenReturn(mockAIResponse);

        // act & assert - Should not throw exception, should handle gracefully
        assertThatNoException().isThrownBy(() ->
                googleAIService.generateFinancialPlan(mockUserProfile, mockFinancialPlanRequest));
    }

    @Test
    void should_extract_asset_allocation_correctly() throws Exception {
        // arrange
        String aiResponse = """
            {
                "healthScore": 85,
                "assetAllocation": {
                    "stocks": 60,
                    "bonds": 30,
                    "cash": 10
                }
            }
            """;

        // act
        Map<String, Object> assetAllocation = googleAIService.extractAssetAllocation(aiResponse);

        // assert
        assertThat(assetAllocation).isNotNull();
        assertThat(assetAllocation).containsEntry("stocks", 60);
        assertThat(assetAllocation).containsEntry("bonds", 30);
        assertThat(assetAllocation).containsEntry("cash", 10);
    }

    @Test
    void should_extract_goal_timeline_correctly() throws Exception {
        // arrange
        String aiResponse = """
            {
                "goalTimeline": [
                    {
                        "goal": "Emergency Fund",
                        "timeline": "6 months",
                        "targetAmount": 50000
                    },
                    {
                        "goal": "House Down Payment",
                        "timeline": "3 years",
                        "targetAmount": 200000
                    }
                ]
            }
            """;

        // act
        List<Map<String, Object>> goalTimeline = googleAIService.extractGoalTimeline(aiResponse);

        // assert
        assertThat(goalTimeline).isNotNull();
        assertThat(goalTimeline).hasSize(2);
        assertThat(goalTimeline.get(0)).containsEntry("goal", "Emergency Fund");
        assertThat(goalTimeline.get(1)).containsEntry("goal", "House Down Payment");
    }

    @Test
    void should_extract_investment_recommendations_correctly() throws Exception {
        // arrange
        String aiResponse = """
            {
                "investmentRecommendations": [
                    {
                        "type": "Index Funds",
                        "allocation": "40%",
                        "reasoning": "Low-cost diversification"
                    },
                    {
                        "type": "Bond ETFs",
                        "allocation": "20%",
                        "reasoning": "Stability and income"
                    }
                ]
            }
            """;

        // act
        List<Map<String, Object>> recommendations = googleAIService.extractInvestmentRecommendations(aiResponse);

        // assert
        assertThat(recommendations).isNotNull();
        assertThat(recommendations).hasSize(2);
        assertThat(recommendations.get(0)).containsEntry("type", "Index Funds");
        assertThat(recommendations.get(1)).containsEntry("type", "Bond ETFs");
    }

    @Test
    void should_extract_action_plan_correctly() throws Exception {
        // arrange
        String aiResponse = """
            {
                "actionPlan": [
                    {
                        "step": 1,
                        "action": "Build emergency fund",
                        "priority": "High"
                    },
                    {
                        "step": 2,
                        "action": "Increase 401k contribution",
                        "priority": "Medium"
                    }
                ]
            }
            """;

        // act
        List<Map<String, Object>> actionPlan = googleAIService.extractActionPlan(aiResponse);

        // assert
        assertThat(actionPlan).isNotNull();
        assertThat(actionPlan).hasSize(2);
        assertThat(actionPlan.get(0)).containsEntry("action", "Build emergency fund");
        assertThat(actionPlan.get(1)).containsEntry("action", "Increase 401k contribution");
    }

    @Test
    void should_throw_exception_when_extracting_from_invalid_json() {
        // arrange
        String invalidJson = "not a valid json";

        // act & assert
        assertThatThrownBy(() -> googleAIService.extractAssetAllocation(invalidJson))
                .isInstanceOf(Exception.class);

        assertThatThrownBy(() -> googleAIService.extractGoalTimeline(invalidJson))
                .isInstanceOf(Exception.class);

        assertThatThrownBy(() -> googleAIService.extractInvestmentRecommendations(invalidJson))
                .isInstanceOf(Exception.class);

        assertThatThrownBy(() -> googleAIService.extractActionPlan(invalidJson))
                .isInstanceOf(Exception.class);
    }

    @Test
    void should_handle_missing_fields_in_extraction_gracefully() throws Exception {
        // arrange
        String aiResponseWithMissingFields = """
            {
                "healthScore": 85
            }
            """;

        // act & assert
        Map<String, Object> assetAllocation = googleAIService.extractAssetAllocation(aiResponseWithMissingFields);
        List<Map<String, Object>> goalTimeline = googleAIService.extractGoalTimeline(aiResponseWithMissingFields);
        List<Map<String, Object>> recommendations = googleAIService.extractInvestmentRecommendations(aiResponseWithMissingFields);
        List<Map<String, Object>> actionPlan = googleAIService.extractActionPlan(aiResponseWithMissingFields);

        // assert - Should return null for missing fields, not throw exceptions
        assertThat(assetAllocation).isNull();
        assertThat(goalTimeline).isNull();
        assertThat(recommendations).isNull();
        assertThat(actionPlan).isNull();
    }

    @Test
    void should_handle_different_risk_tolerance_and_investment_horizon_values() {
        // arrange - Test with different risk tolerance and investment horizon values
        mockUserProfile.setRiskTolerance(5); // High risk tolerance
        mockUserProfile.setInvestmentHorizon("SHORT_TERM");

        String mockAIResponse = """
            {
                "healthScore": 80,
                "healthAssessment": "High risk, short term profile handled correctly"
            }
            """;

        when(mockGeminiModel.chat(any(ChatRequest.class))).thenReturn(mockChatResponse);
        when(mockChatResponse.aiMessage()).thenReturn(mockAiMessage);
        when(mockAiMessage.text()).thenReturn(mockAIResponse);

        // act
        String result = googleAIService.generateFinancialPlan(mockUserProfile, mockFinancialPlanRequest);

        // assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(mockAIResponse);

        verify(mockGeminiModel).chat(any(ChatRequest.class));
    }

    @Test
    void should_handle_user_profile_with_all_edge_case_values() {
        // arrange - Test with edge case values
        mockUserProfile.setAge(25); // Young investor
        mockUserProfile.setRiskTolerance(1); // Very conservative
        mockUserProfile.setInvestmentHorizon("MEDIUM_TERM");
        mockUserProfile.setRetirementAge(70); // Later retirement
        mockUserProfile.setMonthlyIncome(new BigDecimal("5000")); // Lower income
        mockUserProfile.setTotalDebt(new BigDecimal("0")); // No debt

        String mockAIResponse = """
            {
                "healthScore": 75,
                "healthAssessment": "Young conservative investor profile"
            }
            """;

        when(mockGeminiModel.chat(any(ChatRequest.class))).thenReturn(mockChatResponse);
        when(mockChatResponse.aiMessage()).thenReturn(mockAiMessage);
        when(mockAiMessage.text()).thenReturn(mockAIResponse);

        // act & assert
        assertThatNoException().isThrownBy(() ->
                googleAIService.generateFinancialPlan(mockUserProfile, mockFinancialPlanRequest));
    }

    @Test
    void should_prepare_complete_input_data_from_profile_and_request() {
        // arrange
        String validAIResponse = """
            {
                "healthScore": 85,
                "healthAssessment": "Good financial health"
            }
            """;

        when(mockGeminiModel.chat(any(ChatRequest.class))).thenReturn(mockChatResponse);
        when(mockChatResponse.aiMessage()).thenReturn(mockAiMessage);
        when(mockAiMessage.text()).thenReturn(validAIResponse);

        // act
        String result = googleAIService.generateFinancialPlan(mockUserProfile, mockFinancialPlanRequest);

        // assert
        assertThat(result).isNotNull();

        // Verify that the chat request was called with proper data
        verify(mockGeminiModel).chat(argThat(chatRequest -> {
            // Check that the request contains user and system messages
            return chatRequest.messages().size() == 2;
        }));
    }
}