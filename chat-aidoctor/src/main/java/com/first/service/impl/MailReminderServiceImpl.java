package com.first.service.impl;

import com.first.pojo.HealthPlan;
import com.first.pojo.User;
import com.first.service.HealthPlanService;
import com.first.service.MailReminderService;
import com.first.service.UserService;
import com.first.utils.LuckyColaMailUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 健康计划邮件提醒服务实现类，实现定时发送健康计划邮件提醒
 */
@Service
public class MailReminderServiceImpl implements MailReminderService {
    /** 健康计划服务，用于获取用户健康计划 */
    @Autowired
    private HealthPlanService healthPlanService;
    /** 用户服务，用于获取所有用户信息 */
    @Autowired
    private UserService userService;
    /** 邮件工具类，用于发送邮件 */
    @Autowired
    private LuckyColaMailUtil mailUtil;

    /** 日志记录器 */
    private static final Logger logger = LoggerFactory.getLogger(MailReminderServiceImpl.class);

    /**
     * 已提醒记录：planId_yyyy-MM-dd，防止同一天重复提醒同一计划
     */
    private static final Set<String> remindedPlans = new HashSet<>();
    /**
     * 记录上一次提醒的日期，每天零点清空提醒记录
     */
    private static String lastRemindedDate = "";

    /**
     * 定时任务：每1秒执行一次，检查是否有需要发送邮件提醒的健康计划
     */
    @Scheduled(cron = "*/1 * * * * ?")
    @Override
    public void sendDailyHealthPlanReminders() {
        String today = java.time.LocalDate.now().toString();
        // 每天零点清理提醒记录，避免跨天重复提醒
        if (!today.equals(lastRemindedDate)) {
            remindedPlans.clear();
            lastRemindedDate = today;
        }

        // 获取所有用户
        List<User> users = userService.findAllUsers();
        LocalTime now = LocalTime.now();
        for (User user : users) {
            // 跳过没有邮箱的用户
            if (user.getEmail() == null || user.getEmail().isEmpty()) continue;
            // 获取该用户的所有健康计划
            List<HealthPlan> plans = healthPlanService.getHealthPlansByUserId(user.getId());
            if (plans == null || plans.isEmpty()) continue;

            // 过滤出需要发送邮件提醒的健康计划
            List<HealthPlan> emailPlans = plans.stream().filter(p -> {
                // 未开启邮箱提醒的计划跳过
                if (p.getEmailReminder() == null || p.getEmailReminder() != 1) return false;
                // 已经提醒过的计划跳过
                String key = p.getId() + "_" + today;
                if (remindedPlans.contains(key)) return false;
                // 提醒时间为空跳过
                String reminder = p.getReminder();
                if (reminder == null) return false;
                // 判断当前时间是否等于提醒时间（精确到分钟）
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d{1,2}):(\\d{2})").matcher(reminder);
                if (m.find()) {
                    int hour = Integer.parseInt(m.group(1));
                    int minute = Integer.parseInt(m.group(2));
                    return now.getHour() == hour && now.getMinute() == minute;
                }
                return false;
            }).toList();

            // 如果没有需要提醒的计划，跳过
            if (emailPlans.isEmpty()) continue;

            // 组装邮件内容（HTML格式）
            StringBuilder content = new StringBuilder();
            content.append("<div><b>亲爱的用户，以下是您今日的健康计划：</b><br>");
            for (HealthPlan plan : emailPlans) {
                content.append("<b>计划名称：</b>" + plan.getPlanName() + "<br>");
                content.append("<b>目标：</b>" + plan.getGoal() + "<br>");
                content.append("<b>内容：</b><br>" + plan.getContent().replaceAll("\n", "<br>") + "<br>");
                if (plan.getAiAdvice() != null) {
                    content.append("<b>AI建议：</b><br>" + plan.getAiAdvice().replaceAll("\n", "<br>") + "<br>");
                }
                content.append("<b>周期：</b>" + plan.getCycle() + "<br>");
                content.append("<b>提醒时间：</b>" + plan.getReminder() + "<br>");
                content.append("<b>状态：</b>" + plan.getStatus() + "<br>");
                content.append("<b>日期：</b>" + today + "<br><hr>");
                // 记录已提醒，防止重复发送
                remindedPlans.add(plan.getId() + "_" + today);
            }
            content.append("</div>");
            // 发送邮件
            boolean success = mailUtil.sendMail(user.getEmail(), "AI健康计划提醒", "每日健康计划提醒", content.toString());
            if (success) {
                logger.info("[健康计划邮件提醒] 邮件发送成功，收件人：{}，用户ID：{}", user.getEmail(), user.getId());
            } else {
                logger.error("[健康计划邮件提醒] 邮件发送失败，收件人：{}，用户ID：{}", user.getEmail(), user.getId());
            }
        }
    }
} 