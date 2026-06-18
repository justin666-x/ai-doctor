package com.first.service.impl;

import com.first.mapper.UserMapper;
import com.first.mapper.HealthPlanMapper;
import com.first.mapper.HealthDataMapper;
import com.first.mapper.ChatHistoryMapper;
import com.first.mapper.SymptomRecordMapper;
import com.first.mapper.MedicationPlanMapper;
import com.first.pojo.User;
import com.first.service.UserService;
import com.first.utils.Md5Util;
import com.first.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private HealthPlanMapper healthPlanMapper;
    
    @Autowired
    private HealthDataMapper healthDataMapper;
    
    @Autowired
    private ChatHistoryMapper chatHistoryMapper;
    
    @Autowired
    private SymptomRecordMapper symptomRecordMapper;
    
    @Autowired
    private MedicationPlanMapper medicationPlanMapper;

    @Override
    public User findByUserName(String username) {
      User u = userMapper.findByUserName(username);
        return u;
    }

    @Override
    public void register(String username, String password) {
    //加密
       String md5string = Md5Util.getMD5String(password);
    //添加
        userMapper.add(username, md5string);
    }

    @Override
    public void update(User user) {
        user.setUpdateTime(LocalDateTime.now());
        userMapper.update(user);
    }

    @Override
    public void updateAvatar(String avatarUrl) {
        Map<String, Object> map = ThreadLocalUtil.get();
        Integer id = (Integer) map.get("id");
        userMapper.updateAvatar(avatarUrl, id);
    }

    @Override
    public void updatePwd(String newPwd) {
        Map<String, Object> map = ThreadLocalUtil.get();
        Integer id = (Integer) map.get("id");
        userMapper.updatePwd(Md5Util.getMD5String(newPwd),id);
    }
    
    @Override
    public boolean checkProfileComplete(Integer userId) {
        if (userId == null) {
            System.err.println("Cannot check profile completeness: userId is null");
            return false;
        }
        
        try {
            boolean isComplete = userMapper.checkProfileComplete(userId);
            System.out.println("User " + userId + " profile completeness check result: " + isComplete);
            return isComplete;
        } catch (Exception e) {
            System.err.println("Error in checkProfileComplete for userId " + userId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public User findById(Integer userId) {
        if (userId == null) {
            return null;
        }
        return userMapper.findById(userId);
    }

    @Override
    @Transactional
    public void deleteAccount(Integer userId){
        if(userId != null){
            try {
                // 1. 删除用户的健康计划
                healthPlanMapper.deleteByUserId(userId);
                
                // 2. 删除用户的健康数据
                healthDataMapper.deleteByUserId(userId);
                
                // 3. 删除用户的聊天历史
                chatHistoryMapper.deleteByUserId(userId);
                
                // 4. 删除用户的症状记录
                symptomRecordMapper.deleteByUserId(userId);
                
                // 5. 删除用户的用药提醒
                medicationPlanMapper.deleteByUserId(userId);
                
                // 6. 最后删除用户
                userMapper.deleteById(userId);
                
            } catch (Exception e) {
                throw new RuntimeException("删除用户账户失败: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public java.util.List<User> findAllUsers() {
        return userMapper.findAllUsers();
    }
}
