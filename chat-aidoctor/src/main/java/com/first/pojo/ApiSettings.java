package com.first.pojo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ApiSettings {
    private Integer id;
    private String deepseekKey;
    private String tavilyKey;
    private LocalDateTime updateTime;
}
