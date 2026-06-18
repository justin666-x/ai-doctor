package com.first.utils;

import okhttp3.*;
import okio.ByteString;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 讯飞星火X1大模型 WebSocket 客户端工具类
 * 实现鉴权签名、WebSocket连接、消息收发与流式内容拼接
 * 参考官方文档：https://www.xfyun.cn/doc/spark/X1ws.html
 */
public class XunfeiSparkWebSocketClient {
    private OkHttpClient client;
    private WebSocket webSocket;
    private StringBuilder responseBuilder = new StringBuilder();
    private CountDownLatch latch;
    private String appid;
    private String apikey;
    private String apisecret;
    private String wsUrl;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public XunfeiSparkWebSocketClient() {
        client = new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    public XunfeiSparkWebSocketClient(String appid, String apikey, String apisecret, String wsUrl) {
        this();
        this.appid = appid;
        this.apikey = apikey;
        this.apisecret = apisecret;
        this.wsUrl = wsUrl;
    }

    /**
     * 发起WebSocket连接并发送请求，返回AI生成内容
     * @param requestJson 组装好的请求体（含header/payload/parameter）
     * @return AI生成内容
     */
    public String generate(String requestJson) throws InterruptedException {
        latch = new CountDownLatch(1);
        responseBuilder = new StringBuilder();
        Request request = new Request.Builder()
                .url(wsUrl + buildAuthParams()) // 鉴权参数拼接
                .build();
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                webSocket.send(requestJson);
            }
            @Override
            public void onMessage(WebSocket webSocket, String text) {
                try {
                    JsonNode root = objectMapper.readTree(text);
                    int code = root.path("header").path("code").asInt();
                    if (code != 0) {
                        responseBuilder.append("[讯飞API错误:" + code + "]");
                        webSocket.close(1000, null);
                        latch.countDown();
                        return;
                    }
                    JsonNode choices = root.path("payload").path("choices");
                    if (choices.has("text")) {
                        for (JsonNode node : choices.get("text")) {
                            if (node.has("reasoning_content")) {
                                responseBuilder.append(node.get("reasoning_content").asText());
                            } else if (node.has("content")) {
                                responseBuilder.append(node.get("content").asText());
                            }
                        }
                    }
                    int status = choices.path("status").asInt(-1);
                    if (status == 2) { // 2=会话结束
                        webSocket.close(1000, null);
                        latch.countDown();
                    }
                } catch (Exception e) {
                    responseBuilder.append("[解析异常:" + e.getMessage() + "]");
                    webSocket.close(1000, null);
                    latch.countDown();
                }
            }
            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
            }
            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                webSocket.close(1000, null);
                latch.countDown();
            }
            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                latch.countDown();
            }
            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                latch.countDown();
            }
        });
        latch.await(60, TimeUnit.SECONDS);
        return responseBuilder.toString();
    }

    /**
     * 构建鉴权参数（实现ws鉴权签名算法，返回如 ?authorization=xxx&date=xxx&host=xxx）
     */
    private String buildAuthParams() {
        try {
            // 1. 获取当前GMT时间
            SimpleDateFormat sdf = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            String date = sdf.format(new Date());
            // 2. 构造签名字符串
            String host = "spark-api.xf-yun.com";
            String path = new java.net.URI(wsUrl).getPath();
            String signatureOrigin = "host: " + host + "\n" +
                    "date: " + date + "\n" +
                    "GET " + path + " HTTP/1.1";
            // 3. HMAC-SHA256加密
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec spec = new SecretKeySpec(apisecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(spec);
            byte[] hash = mac.doFinal(signatureOrigin.getBytes(StandardCharsets.UTF_8));
            String signature = Base64.getEncoder().encodeToString(hash);
            // 4. 构造authorization
            String authorizationOrigin = String.format("api_key=\"%s\", algorithm=\"hmac-sha256\", headers=\"host date request-line\", signature=\"%s\"", apikey, signature);
            String authorization = Base64.getEncoder().encodeToString(authorizationOrigin.getBytes(StandardCharsets.UTF_8));
            // 5. 拼接URL参数
            String params = String.format("?authorization=%s&date=%s&host=%s",
                    URLEncoder.encode(authorization, "UTF-8"),
                    URLEncoder.encode(date, "UTF-8"),
                    URLEncoder.encode(host, "UTF-8"));
            return params;
        } catch (Exception e) {
            return "";
        }
    }
} 