package com.first.controller;

import com.first.pojo.User;
import com.first.service.ChatMemoryService;
import com.first.service.TavilySearchService;
import com.first.service.UserService;
import com.first.utils.JwtUtil;
import com.first.utils.ThreadLocalUtil;
import jakarta.annotation.PreDestroy;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatMemoryService chatMemoryService;
    private final UserService userService;
    private final TavilySearchService tavilySearchService;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public ChatController(ChatMemoryService chatMemoryService, UserService userService,
                          TavilySearchService tavilySearchService) {
        this.chatMemoryService = chatMemoryService;
        this.userService = userService;
        this.tavilySearchService = tavilySearchService;
    }

    @PreDestroy
    public void shutdown() {
        executorService.shutdown();
    }


    @GetMapping(value = "/with-sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatWithSSE(@RequestParam String message,
                                  @RequestParam(required = false) String token,
                                  @RequestParam(required = false) String sessionId) {
        SseEmitter emitter = new SseEmitter(120000L); // 2分钟超时

        executorService.execute(() -> {      // 这里面是处理 AI 回复和推送的全部逻辑
            try {
                // 发送初始连接成功消息
                emitter.send(SseEmitter.event()
                        .name("connect")
                        .data("Connected successfully"));

                // 构建包含用户个性化信息的提示词
                String finalPrompt = buildPromptWithUserInfo(message, token);

                // 联网搜索补充上下文
                String searchContext = "";
                try {
                    searchContext = tavilySearchService.searchAndFormat(message);
                } catch (Exception ignored) {}
                if (!searchContext.isEmpty()) {
                    finalPrompt += searchContext;
                }

                // 获取会话 ID（用于记忆）
                String conversationId;
                if (sessionId != null && !sessionId.isEmpty()) {
                    conversationId = sessionId;
                } else {
                    conversationId = "anon";
                    if (token != null && !token.isEmpty()) {
                        try {
                            java.util.Map<String, Object> claims = JwtUtil.parseToken(token);
                            conversationId = String.valueOf(claims.get("id"));
                        } catch (Exception ignored) {}
                    }
                }

                // 使用流式记忆对话
                reactor.core.publisher.Flux<String> responseFlux = chatMemoryService.chatWithMemoryStream(conversationId, finalPrompt);

                responseFlux.doOnNext(chunk -> {
                    try {
                        emitter.send(SseEmitter.event()
                                .name("message")
                                .data(chunk));
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                }).doOnComplete(() -> {
                    try {
                        emitter.send(SseEmitter.event().name("done").data("completed"));
                    } catch (IOException ignored) {}
                    emitter.complete();
                }).doOnError(emitter::completeWithError)
                  .subscribe();

             }catch (IOException e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    /**
     * 构建包含用户个性化信息的提示词
     */
    private String buildPromptWithUserInfo(String userQuestion, String token) {
        String systemPrompt = "你是【AI健康助手】，一位专业、温暖且知识渊博的医疗AI，同时也能进行日常交流。\n\n"
                + "## 核心能力\n"
                + "1. 疾病分析：根据用户描述的症状，分析可能的病因、鉴别诊断，给出就医建议\n"
                + "2. 用药指导：解释药物作用机制、用法用量、副作用、禁忌症和药物相互作用\n"
                + "3. 检验解读：解读血常规、生化、影像等检查报告，用通俗语言说明指标意义\n"
                + "4. 健康管理：提供饮食营养、运动康复、心理健康、慢病管理等方面的建议\n"
                + "5. 医学科普：用通俗易懂的方式解释医学概念、疾病原理和前沿医学进展\n\n"
                + "## 回复风格\n"
                + "- 专业但不说教，亲切但不随意，像一位耐心的医生在问诊\n"
                + "- 用中文回复，必要时可标注英文术语（如：高血压 hypertension）\n"
                + "- 回答结构清晰：先给结论，再展开解释，最后给行动建议\n"
                + "- 涉及用药或治疗方案时，务必提醒「请遵医嘱，本文仅供参考」\n"
                + "- 遇到紧急症状（胸痛、呼吸困难、意识障碍等），优先建议立即就医\n"
                + "- 每次回复严格控制在800字以内，超过800字会被截断，务必精简\n\n"
                + "## 其他话题\n"
                + "- 用户可以和你聊任何话题：生活、科技、学习、娱乐等\n"
                + "- 非医疗话题正常交流即可，不需要刻意扯回医学\n"
                + "- 始终使用中文，保持友好、有帮助的态度";

        // 获取用户信息 - 优先使用 token 参数，否则尝试 ThreadLocal
        User user = null;
        boolean hasValidToken = false;
        try {
            if (token != null && !token.isEmpty()) {
                java.util.Map<String, Object> claims = JwtUtil.parseToken(token);
                Object idObj = claims.get("id");
                if (idObj instanceof Number) {
                    user = userService.findById(((Number) idObj).intValue());
                    hasValidToken = true;
                }
            } else {
                Integer userId = ThreadLocalUtil.getUserId();
                if (userId != null) {
                    user = userService.findById(userId);
                    hasValidToken = true;
                }
            }
        } catch (Exception e) {
        }

        if (!hasValidToken) {
            // 匿名用户限制：只能问一次，且强制使用公共 prompt
        }

        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append(systemPrompt).append("\n\n");

        if (user != null) {
            promptBuilder.append("以下是患者的基本信息（若字段缺失则表示用户未提供）：\n");
            if (user.getNickname() != null) promptBuilder.append("- 姓名：").append(user.getNickname()).append("\n");
            if (user.getSex() != null) promptBuilder.append("- 性别：").append(user.getSex()).append("\n");
            if (user.getAge() != null) promptBuilder.append("- 年龄：").append(user.getAge()).append("\n");
            if (user.getHeight() != null) promptBuilder.append("- 身高：").append(user.getHeight()).append("\n");
            if (user.getWeight() != null) promptBuilder.append("- 体重：").append(user.getWeight()).append("\n");
            if (user.getBloodType() != null) promptBuilder.append("- 血型：").append(user.getBloodType()).append("\n");
            if (user.getContactPhone() != null) promptBuilder.append("- 联系电话：").append(user.getContactPhone()).append("\n");
            promptBuilder.append("\n");
        }

        promptBuilder.append("病人说：").append(userQuestion);

        // 如果病人直接描述症状，希望 AI 不再追问，而是给出分析
        if (userQuestion != null && userQuestion.trim().startsWith("我的症状是")) {
            promptBuilder.append("\n\n请根据上述症状详细分析可能的疾病/病因、相关注意事项及建议的下一步检查或治疗方案，无需向病人提出额外问题。");
        }

        return promptBuilder.toString();
    }
}