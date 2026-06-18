-- ============================================
-- AI Doctor 3.0 数据库初始化脚本
-- 适用数据库：MySQL 8.0+
-- ============================================

CREATE DATABASE IF NOT EXISTS aidoctordata
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE aidoctordata;

-- ============================================
-- 1. 用户表
-- ============================================
CREATE TABLE IF NOT EXISTS user (
  id         INT UNSIGNED  NOT NULL AUTO_INCREMENT PRIMARY KEY,
  username   VARCHAR(20)   NOT NULL UNIQUE,
  password   VARCHAR(32)   NULL,
  nickname   VARCHAR(10)   NULL DEFAULT '',
  height     VARCHAR(10)   NULL,
  weight     VARCHAR(10)   NULL,
  sex        VARCHAR(10)   NULL,
  blood_type VARCHAR(5)    NULL,
  age        INT           NULL,
  id_card_number    VARCHAR(18)  NULL,
  contact_phone     VARCHAR(20)  NULL,
  emergency_contact VARCHAR(15)  NULL,
  email      VARCHAR(128)  NULL,
  user_pic   VARCHAR(128)  NULL DEFAULT '',
  create_time DATETIME     NOT NULL,
  update_time DATETIME     NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- 2. 健康数据表
-- ============================================
CREATE TABLE IF NOT EXISTS health_data (
  id         INT UNSIGNED  NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id    INT UNSIGNED  NOT NULL,
  record_time DATETIME     NOT NULL,
  create_time DATETIME     NOT NULL,
  update_time DATETIME     NOT NULL,

  -- 生命体征
  temperature       DECIMAL(3,1)  NULL,
  heart_rate        INT           NULL,
  respiratory_rate  INT           NULL,
  blood_pressure    VARCHAR(20)   NULL,

  -- 血常规
  red_blood_cell    DECIMAL(4,2)  NULL,
  hemoglobin        DECIMAL(5,1)  NULL,
  white_blood_cell  DECIMAL(4,2)  NULL,
  platelet          DECIMAL(5,1)  NULL,

  -- 血糖
  fasting_blood_glucose     DECIMAL(3,1)  NULL,
  postprandial_blood_glucose DECIMAL(3,1)  NULL,

  -- 血脂
  total_cholesterol   DECIMAL(3,2)  NULL,
  triglycerides       DECIMAL(3,2)  NULL,
  hdl_cholesterol     DECIMAL(3,2)  NULL,
  ldl_cholesterol     DECIMAL(3,2)  NULL,

  -- AI分析报告
  vital_signs_analysis     TEXT NULL,
  blood_routine_analysis   TEXT NULL,
  blood_glucose_analysis   TEXT NULL,
  blood_lipid_analysis     TEXT NULL,
  overall_health_report    TEXT NULL,

  INDEX idx_user_id (user_id),
  INDEX idx_record_time (record_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- 3. 健康计划表
-- ============================================
CREATE TABLE IF NOT EXISTS health_plan (
  id         INT           NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id    INT UNSIGNED  NOT NULL,
  plan_name  VARCHAR(100)  NOT NULL,
  goal       VARCHAR(100)  NULL,
  content    TEXT          NULL,
  start_date DATE          NOT NULL,
  end_date   DATE          NOT NULL,
  create_time DATETIME     NULL DEFAULT CURRENT_TIMESTAMP,
  cycle      VARCHAR(20)   NULL,
  status     VARCHAR(20)   NULL DEFAULT '进行中',
  reminder   VARCHAR(100)  NULL,
  ai_advice  TEXT          NULL,
  email_reminder TINYINT(1) NOT NULL DEFAULT 1,

  INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- 4. 用药计划表
-- ============================================
CREATE TABLE IF NOT EXISTS medication_plan (
  id          INT UNSIGNED  NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id     INT UNSIGNED  NOT NULL,
  drug_name   VARCHAR(64)   NOT NULL,
  purpose     VARCHAR(128)  NULL,
  dosage_count INT          NULL DEFAULT 1,
  time_points VARCHAR(128)  NOT NULL,
  create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- 5. 症状记录表
-- ============================================
CREATE TABLE IF NOT EXISTS symptom_record (
  id          INT UNSIGNED  NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id     INT UNSIGNED  NOT NULL,
  symptom_text TEXT         NOT NULL,
  ai_analysis  TEXT         NOT NULL,
  create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- 6. 聊天历史表
-- ============================================
CREATE TABLE IF NOT EXISTS chat_history (
  id          INT UNSIGNED  NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id     INT UNSIGNED  NOT NULL,
  title       VARCHAR(100)  NOT NULL,
  messages    LONGTEXT      NOT NULL,
  create_time DATETIME      NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME      NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- 7. API 设置表
-- ============================================
CREATE TABLE IF NOT EXISTS api_settings (
  id            INT           NOT NULL PRIMARY KEY DEFAULT 1,
  deepseek_key  VARCHAR(256)  NULL,
  tavily_key    VARCHAR(256)  NULL,
  update_time   DATETIME      NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO api_settings (id) VALUES (1);
