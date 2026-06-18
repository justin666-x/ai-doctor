package com.first.controller;

import com.first.pojo.HealthData;
import com.first.pojo.Result;
import com.first.pojo.User;
import com.first.service.ChatService;
import com.first.service.HealthDataService;
import com.first.service.TavilySearchService;
import com.first.service.UserService;
import com.first.utils.ThreadLocalUtil;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final HealthDataService healthDataService;
    private final UserService userService;
    private final ChatService chatService;
    private final TavilySearchService tavilySearchService;

    public DashboardController(HealthDataService healthDataService, UserService userService,
                               ChatService chatService, TavilySearchService tavilySearchService) {
        this.healthDataService = healthDataService;
        this.userService = userService;
        this.chatService = chatService;
        this.tavilySearchService = tavilySearchService;
    }

    @GetMapping("/health-score")
    public Result<Map<String, Object>> healthScore() {
        Integer userId = ThreadLocalUtil.getUserId();
        if (userId == null) return Result.error("请先登录");

        Map<String, Object> result = new HashMap<>();

        try {
            List<HealthData> list = healthDataService.getHealthDataByUser(userId);
            if (list == null || list.isEmpty()) {
                result.put("score", 65);
                result.put("summary", "请录入健康数据");
                result.put("tag", "待完善");
                result.put("hasData", false);
                return Result.success(result);
            }

            HealthData latest = list.get(0);

            StringBuilder prompt = new StringBuilder();
            prompt.append("你是专业健康管理师。请根据用户健康数据给出0-100综合评分。严格仅输出一行JSON，不要任何额外文字：\n");
            prompt.append("{\"score\":80,\"summary\":\"15字内评语\",\"tag\":\"标签\"}\n");
            prompt.append("tag可选值：优秀/良好/一般/需关注/待完善\n\n健康数据：\n");
            if (latest.getTemperature() != null) prompt.append("体温: ").append(latest.getTemperature()).append("°C\n");
            if (latest.getHeartRate() != null) prompt.append("心率: ").append(latest.getHeartRate()).append("次/分\n");
            if (latest.getBloodPressure() != null) prompt.append("血压: ").append(latest.getBloodPressure()).append("\n");
            if (latest.getRespiratoryRate() != null) prompt.append("呼吸频率: ").append(latest.getRespiratoryRate()).append("次/分\n");
            if (latest.getFastingBloodGlucose() != null) prompt.append("空腹血糖: ").append(latest.getFastingBloodGlucose()).append("mmol/L\n");
            if (latest.getTotalCholesterol() != null) prompt.append("总胆固醇: ").append(latest.getTotalCholesterol()).append("mmol/L\n");

            String aiResponse = chatService.chat(prompt.toString());

            int score = 72;
            String summary = "健康数据正常";
            String tag = "良好";

            Pattern scorePattern = Pattern.compile("\"score\"\\s*:\\s*(\\d+)");
            Matcher m = scorePattern.matcher(aiResponse);
            if (m.find()) score = Math.min(100, Math.max(0, Integer.parseInt(m.group(1))));

            m = Pattern.compile("\"summary\"\\s*:\\s*\"([^\"]+)\"").matcher(aiResponse);
            if (m.find()) summary = m.group(1);
            if (summary.length() > 20) summary = summary.substring(0, 20);

            m = Pattern.compile("\"tag\"\\s*:\\s*\"([^\"]+)\"").matcher(aiResponse);
            if (m.find()) tag = m.group(1);

            result.put("score", score);
            result.put("summary", summary);
            result.put("tag", tag);
            result.put("hasData", true);
        } catch (Exception e) {
            result.put("score", 72);
            result.put("summary", "评分服务暂不可用");
            result.put("tag", "一般");
            result.put("hasData", true);
        }
        return Result.success(result);
    }

    @GetMapping("/daily-advice")
    public Result<Map<String, Object>> dailyAdvice(@RequestParam(required = false) String location) {
        Integer userId = ThreadLocalUtil.getUserId();
        Map<String, Object> result = new HashMap<>();

        try {
            User user = null;
            if (userId != null) {
                user = userService.findById(userId);
            }

            String weatherQuery;
            if (location != null && !location.isEmpty()) {
                weatherQuery = location + " 今天天气";
            } else {
                weatherQuery = "今天天气";
            }

            String weatherInfo = "";
            try {
                weatherInfo = tavilySearchService.searchAndFormat(weatherQuery);
            } catch (Exception ignored) {}

            StringBuilder prompt = new StringBuilder();
            prompt.append("请根据天气搜索结果提取天气并给出一句20字内健康建议。");
            prompt.append("严格仅输出一行JSON：{\"weather\":\"晴\",\"temperature\":\"25°C\",\"weatherIcon\":\"☀️\",\"tag\":\"分类\",\"advice\":\"建议\"}\n");
            prompt.append("tag可选：运动/饮食/作息/防护/出行\n\n");
            if (!weatherInfo.isEmpty()) {
                prompt.append("天气搜索结果：\n").append(weatherInfo).append("\n");
            } else {
                prompt.append("（请按常识给出当前季节典型天气）\n");
            }
            if (user != null) {
                if (user.getAge() != null) prompt.append("用户").append(user.getAge()).append("岁 ");
                if (user.getSex() != null) prompt.append(user.getSex()).append(" ");
                prompt.append("\n");
            }

            String aiResponse = chatService.chat(prompt.toString()).trim();

            String weather = "晴";
            String temperature = "25°C";
            String weatherIcon = "🌤️";
            String tag = "防护";
            String advice = "今天记得多喝水，保持良好作息";

            Matcher m;
            m = Pattern.compile("\"weather\"\\s*:\\s*\"([^\"]+)\"").matcher(aiResponse);
            if (m.find()) weather = m.group(1);
            m = Pattern.compile("\"temperature\"\\s*:\\s*\"([^\"]+)\"").matcher(aiResponse);
            if (m.find()) temperature = m.group(1);
            m = Pattern.compile("\"weatherIcon\"\\s*:\\s*\"([^\"]+)\"").matcher(aiResponse);
            if (m.find()) weatherIcon = m.group(1);
            m = Pattern.compile("\"tag\"\\s*:\\s*\"([^\"]+)\"").matcher(aiResponse);
            if (m.find()) tag = m.group(1);
            m = Pattern.compile("\"advice\"\\s*:\\s*\"([^\"]+)\"").matcher(aiResponse);
            if (m.find()) advice = m.group(1);
            if (advice.length() > 30) advice = advice.substring(0, 30);

            result.put("weather", weather);
            result.put("temperature", temperature);
            result.put("weatherIcon", weatherIcon);
            result.put("tag", tag);
            result.put("advice", advice);

        } catch (Exception e) {
            result.put("weather", "晴");
            result.put("temperature", "25°C");
            result.put("weatherIcon", "🌤️");
            result.put("tag", "防护");
            result.put("advice", "今天记得多喝水，保持良好作息");
        }
        return Result.success(result);
    }
}
