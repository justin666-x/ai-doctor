package com.first.controller;

import com.first.pojo.ApiSettings;
import com.first.pojo.Result;
import com.first.service.ApiSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api-settings")
public class ApiSettingsController {

    @Autowired
    private ApiSettingsService apiSettingsService;

    @GetMapping
    public Result<ApiSettings> get() {
        ApiSettings s = apiSettingsService.getSettings();
        return Result.success(s);
    }

    @PutMapping
    public Result update(@RequestBody Map<String, String> payload) {
        String deepseekKey = payload.get("deepseekKey");
        String tavilyKey = payload.get("tavilyKey");
        apiSettingsService.updateSettings(deepseekKey, tavilyKey);
        return Result.success();
    }
}
