package com.first.service;

public interface ApiSettingsService {
    com.first.pojo.ApiSettings getSettings();
    void updateSettings(String deepseekKey, String tavilyKey);
    String getDeepseekKey();
    String getTavilyKey();
}
