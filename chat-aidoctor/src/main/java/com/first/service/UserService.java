package com.first.service;

import com.first.pojo.User;

public interface UserService {
    //根据用户名查询用户信息
    User findByUserName(String username);
   //注册
    void register(String username, String password);

    //更新用户信息
    void update(User user);

    //更新头像
    void updateAvatar(String avatarUrl);

    //更新密码
    void updatePwd(String newPwd);

    //检查用户信息是否完整
    boolean checkProfileComplete(Integer userId);

    //根据ID获取用户信息
    User findById(Integer userId);

    //注销账号
    void deleteAccount(Integer userId);

    // 查询所有用户（邮件提醒用）
    java.util.List<User> findAllUsers();
}
