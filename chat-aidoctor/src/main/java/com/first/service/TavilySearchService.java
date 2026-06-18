package com.first.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class TavilySearchService {

    private static final Logger log = LoggerFactory.getLogger(TavilySearchService.class);
    private static final String TAVILY_URL = "https://api.tavily.com/search";
    private static final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private ApiSettingsService apiSettingsService;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    public List<String> search(String query) {
        List<String> results = new ArrayList<>();
        String apiKey = apiSettingsService.getTavilyKey();
        if (apiKey == null || apiKey.isEmpty()) {
            return results;
        }
        try {
            String json = mapper.writeValueAsString(new SearchRequest(query));
            RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
            Request request = new Request.Builder()
                    .url(TAVILY_URL)
                    .post(body)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .build();

            Response response = client.newCall(request).execute();
            if (response.isSuccessful() && response.body() != null) {
                String respStr = response.body().string();
                JsonNode root = mapper.readTree(respStr);

                if (root.has("answer") && !root.get("answer").asText().isEmpty()) {
                    results.add("【AI摘要】" + root.get("answer").asText());
                }

                JsonNode items = root.get("results");
                if (items != null) {
                    for (int i = 0; i < items.size() && i < 5; i++) {
                        JsonNode item = items.get(i);
                        String title = item.has("title") ? item.get("title").asText() : "";
                        String content = item.has("content") ? item.get("content").asText() : "";
                        if (!title.isEmpty() || !content.isEmpty()) {
                            results.add("[" + (i + 1) + "] " + title + "\n" + content);
                        }
                    }
                }
            } else {
                log.warn("Tavily search failed: HTTP {}", response.code());
            }
            response.close();
        } catch (IOException e) {
            log.error("Tavily search error: {}", e.getMessage());
        }
        return results;
    }

    public String searchAndFormat(String query) {
        List<String> results = search(query);
        if (results.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        sb.append("\n\n【以下是联网搜索到的最新相关信息，请参考这些信息回答用户问题】\n");
        for (String r : results) {
            sb.append(r).append("\n\n");
        }
        return sb.toString();
    }

    private static class SearchRequest {
        public String query;
        public String search_depth = "basic";
        public int max_results = 5;
        public boolean include_answer = true;

        public SearchRequest(String query) {
            this.query = query;
        }
    }
}
