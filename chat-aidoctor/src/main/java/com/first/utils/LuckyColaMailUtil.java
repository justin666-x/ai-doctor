package com.first.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * LuckyCola 邮件发送工具类，封装了通过 LuckyCola 平台 API 发送邮件的逻辑
 */
@Component
public class LuckyColaMailUtil {
    /** LuckyCola 平台密钥，从配置文件读取 */
    @Value("${luckycola.mail.cola-key}")
    private String colaKey;
    /** 发件人邮箱，从配置文件读取 */
    @Value("${luckycola.mail.smtp-email}")
    private String smtpEmail;
    /** 发件邮箱授权码/密码，从配置文件读取 */
    @Value("${luckycola.mail.smtp-code}")
    private String smtpCode;
    /** 授权码类型（如QQ、163等），从配置文件读取 */
    @Value("${luckycola.mail.smtp-code-type}")
    private String smtpCodeType;

    /** LuckyCola 邮件API接口地址 */
    private static final String API_URL = "https://luckycola.com.cn/tools/customMail";

    /**
     * 发送邮件
     * @param to 收件人邮箱
     * @param fromTitle 发件人显示名称
     * @param subject 邮件主题
     * @param content 邮件内容（支持HTML）
     * @return 发送成功返回true，否则返回false
     */
    public boolean sendMail(String to, String fromTitle, String subject, String content) {
        try {
            // 组装请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("ColaKey", colaKey);
            params.put("tomail", to);
            params.put("fromTitle", fromTitle);
            params.put("subject", subject);
            params.put("content", content);
            params.put("isTextContent", false); // false表示内容为HTML格式
            params.put("smtpCode", smtpCode);
            params.put("smtpEmail", smtpEmail);
            params.put("smtpCodeType", smtpCodeType);

            // 将参数序列化为JSON字符串
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(params);

            // 创建HTTP连接，准备POST请求
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // 发送请求体（JSON数据）
            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes());
            }

            // 获取响应码，200表示发送成功
            int code = conn.getResponseCode();
            return code == 200;
        } catch (Exception e) {
            // 捕获异常，打印错误信息，返回失败
            e.printStackTrace();
            return false;
        }
    }
} 