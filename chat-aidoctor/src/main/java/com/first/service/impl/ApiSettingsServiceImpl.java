package com.first.service.impl;

import com.first.mapper.ApiSettingsMapper;
import com.first.pojo.ApiSettings;
import com.first.service.ApiSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApiSettingsServiceImpl implements ApiSettingsService {

    @Autowired
    private ApiSettingsMapper mapper;

    private volatile String cachedDeepseekKey;
    private volatile String cachedTavilyKey;
    private volatile boolean loaded = false;

    private void ensureLoaded() {
        if (!loaded) {
            synchronized (this) {
                if (!loaded) {
                    ApiSettings s = mapper.get();
                    if (s != null) {
                        cachedDeepseekKey = s.getDeepseekKey();
                        cachedTavilyKey = s.getTavilyKey();
                    }
                    loaded = true;
                }
            }
        }
    }

    @Override
    public ApiSettings getSettings() {
        return mapper.get();
    }

    @Override
    public void updateSettings(String deepseekKey, String tavilyKey) {
        ApiSettings s = new ApiSettings();
        s.setId(1);
        s.setDeepseekKey(emptyToNull(deepseekKey));
        s.setTavilyKey(emptyToNull(tavilyKey));
        mapper.update(s);
        cachedDeepseekKey = s.getDeepseekKey();
        cachedTavilyKey = s.getTavilyKey();
    }

    @Override
    public String getDeepseekKey() {
        ensureLoaded();
        return cachedDeepseekKey;
    }

    @Override
    public String getTavilyKey() {
        ensureLoaded();
        return cachedTavilyKey;
    }

    private String emptyToNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }
}
