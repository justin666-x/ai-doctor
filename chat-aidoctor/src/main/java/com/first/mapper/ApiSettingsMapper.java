package com.first.mapper;

import com.first.pojo.ApiSettings;
import org.apache.ibatis.annotations.*;

@Mapper
public interface ApiSettingsMapper {

    @Select("SELECT * FROM api_settings WHERE id = 1")
    ApiSettings get();

    @Update("UPDATE api_settings SET deepseek_key = #{deepseekKey}, tavily_key = #{tavilyKey}")
    void update(ApiSettings settings);
}
