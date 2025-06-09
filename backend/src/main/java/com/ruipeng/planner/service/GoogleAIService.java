package com.ruipeng.planner.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruipeng.planner.dto.FinancialPlanRequest;
import com.ruipeng.planner.entity.FinancialPlan;
import com.ruipeng.planner.entity.UserProfile;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class GoogleAIService {
    private static final Logger logger = Logger.getLogger(GoogleAIService.class.getName());

    private final ObjectMapper objectMapper;
    private final GoogleAiGeminiChatModel geminiModel;



    public GoogleAIService(
            ObjectMapper objectMapper,
            @Value("${google.ai.api.key}") String apiKey,
            @Value("${google.ai.temperature:0.7}") Double temperature,
            @Value("${google.ai.max-tokens:2048}") Integer maxTokens) {

        this.objectMapper = objectMapper;
        this.geminiModel = GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .temperature(temperature)
                .maxOutputTokens(maxTokens)
                .modelName("gemini-1.5-flash") // 使用最新的模型
                .build();
    }

    public String generateFinancialPlan(UserProfile profile, FinancialPlanRequest request) {
        try {
            // 系统提示词
            String systemPrompt = """
                You are an expert financial advisor specialized in creating personalized financial plans.
                You will analyze the user's financial profile and generate a detailed financial plan with the following sections:
                1. Financial health assessment (with score out of 100)
                2. Recommended asset allocation
                3. Goal timeline
                4. Investment recommendations
                5. Action plan
                
                Provide your response as a well-structured JSON object with the fields:
                - healthScore (integer)
                - healthAssessment (string)
                - assetAllocation (object)
                - goalTimeline (array of objects)
                - investmentRecommendations (array of objects)
                - actionPlan (array of objects)
                """;

            // 添加用户资料数据
            Map<String, Object> inputData = prepareInputData(profile, request);
            String userPrompt = objectMapper.writeValueAsString(inputData);

            // 创建聊天请求
            ChatRequest chatRequest = ChatRequest.builder()
                    .messages(Arrays.asList(
                            SystemMessage.from(systemPrompt),
                            UserMessage.from(userPrompt)
                    ))
                    .build();

            logger.info("Sending request to Google AI Gemini model");

            // 生成响应
            ChatResponse chatResponse = geminiModel.chat(chatRequest);
            String aiResponse = chatResponse.aiMessage().text();
            String cleanedJson = aiResponse
                    .replaceAll("```json\\s*", "")  // 移除开头的 ```json
                    .replaceAll("```\\s*$", "")     // 移除结尾的 ```
                    .trim();


            logger.info("Received response from Google AI Gemini model");

            // 验证响应是有效的JSON
            if (!isValidJson(cleanedJson)) {
                logger.warning("Received invalid JSON response from AI model");
                throw new RuntimeException("Invalid JSON response from AI model");
            }

            return cleanedJson;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error calling Google AI Gemini model", e);
            throw new RuntimeException("Failed to generate financial plan: " + e.getMessage(), e);
        }
    }

    private boolean isValidJson(String json) {
        try {
            objectMapper.readTree(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Map<String, Object> prepareInputData(UserProfile profile, FinancialPlanRequest request) {
        Map<String, Object> inputData = new HashMap<>();

        // 添加用户资料数据
        inputData.put("age", profile.getAge());
        inputData.put("occupation", profile.getOccupation());
        inputData.put("educationLevel", profile.getEducationLevel().getDisplayName());
        inputData.put("monthlyIncome", profile.getMonthlyIncome());
        inputData.put("monthlyExpenses", profile.getMonthlyExpenses());
        inputData.put("totalSavings", profile.getTotalSavings());
        inputData.put("totalDebt", profile.getTotalDebt());
        inputData.put("riskTolerance", profile.getRiskTolerance());
        inputData.put("investmentHorizon", profile.getInvestmentHorizon());

        // 解析并添加JSON字段
        try {
            if (profile.getCurrentInvestmentsJson() != null) {
                inputData.put("currentInvestments",
                        objectMapper.readValue(profile.getCurrentInvestmentsJson(), List.class));
            }

            if (profile.getInvestmentInterestsJson() != null) {
                inputData.put("investmentInterests",
                        objectMapper.readValue(profile.getInvestmentInterestsJson(), List.class));
            }

            if (profile.getFinancialGoalsJson() != null) {
                inputData.put("financialGoals",
                        objectMapper.readValue(profile.getFinancialGoalsJson(), List.class));
            }
        } catch (Exception e) {
            logger.warning("Error parsing JSON fields from user profile: " + e.getMessage());
        }

        inputData.put("retirementAge", profile.getRetirementAge());
        inputData.put("monthlySavings", profile.getMonthlySavings());

        // 添加请求特定的数据
        inputData.put("planName", request.getPlanName());
        inputData.put("additionalInstructions", request.getAdditionalInstructions());

        return inputData;
    }

    // 从AI响应中提取组件的方法
    public Map<String, Object> extractAssetAllocation(String aiResponse) throws Exception {
        Map<String, Object> result = objectMapper.readValue(aiResponse, Map.class);
        return (Map<String, Object>) result.get("assetAllocation");
    }

    public List<Map<String, Object>> extractGoalTimeline(String aiResponse) throws Exception {
        Map<String, Object> result = objectMapper.readValue(aiResponse, Map.class);
        return (List<Map<String, Object>>) result.get("goalTimeline");
    }

    public List<Map<String, Object>> extractInvestmentRecommendations(String aiResponse) throws Exception {
        Map<String, Object> result = objectMapper.readValue(aiResponse, Map.class);
        return (List<Map<String, Object>>) result.get("investmentRecommendations");
    }

    public List<Map<String, Object>> extractActionPlan(String aiResponse) throws Exception {
        Map<String, Object> result = objectMapper.readValue(aiResponse, Map.class);
        return (List<Map<String, Object>>) result.get("actionPlan");
    }
}