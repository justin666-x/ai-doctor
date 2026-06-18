package com.first.controller;

import com.first.pojo.Result;
import com.first.pojo.User;
import com.first.service.UserService;
import com.first.utils.JwtUtil;
import com.first.utils.Md5Util;
import com.first.utils.ThreadLocalUtil;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Validated
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result register(@Pattern(regexp = "^\\S{2,16}$") String username,
                           @Pattern(regexp = "^\\S{5,16}$") String password) {
        User u = userService.findByUserName(username);
        if (u == null) {
            userService.register(username, password);
            return Result.success();
        } else {
            return Result.error("用户名已被占用");
        }
    }

    @PostMapping("/login")
    public Result<String> login(@Pattern(regexp = "^\\S{2,16}$") String username,
                        @Pattern(regexp = "^\\S{5,16}$") String password) {
        User loginUser = userService.findByUserName(username);
        if (loginUser == null) return Result.error("用户名不存在");
        if (Md5Util.getMD5String(password).equals(loginUser.getPassword())) {
            Map<String, Object> claims = new HashMap<>();
            claims.put("id", loginUser.getId());
            claims.put("username", loginUser.getUsername());
            String token = JwtUtil.genToken(claims);
            return Result.success(token);
        }
        return Result.error("密码错误");
    }

    @GetMapping("/info")
    public Result<User> userInfo() {
        String username = ThreadLocalUtil.getUsername();
        User user = userService.findByUserName(username);
        return Result.success(user);
    }

    @PutMapping("/update")
    public Result update(@RequestBody @Validated User user) {
        userService.update(user);
        return Result.success();
    }

    @PatchMapping("/updateAvatar")
    public Result updateAvatar(@RequestParam @URL String avatarUrl) {
        userService.updateAvatar(avatarUrl);
        return Result.success();
    }

    @PatchMapping("/updatePwd")
    public Result updatePwd(@RequestBody Map<String, String> params) {
        String oldPwd = params.get("old_pwd");
        String newPwd = params.get("new_pwd");
        String rePwd = params.get("re_pwd");

        if (!StringUtils.hasLength(oldPwd) || !StringUtils.hasLength(newPwd) || !StringUtils.hasLength(rePwd)) {
            return Result.error("缺少必要的参数");
        }

        String username = ThreadLocalUtil.getUsername();
        User loginuser = userService.findByUserName(username);
        if (loginuser == null) return Result.error("用户不存在");
        if (!loginuser.getPassword().equals(Md5Util.getMD5String(oldPwd))) {
            return Result.error("原密码填写错误");
        }
        if (!newPwd.equals(rePwd)) return Result.error("两次密码输入不一致");

        userService.updatePwd(newPwd);
        return Result.success();
    }

    @GetMapping("/profile")
    public Result<User> getUserProfile() {
        Integer userId = ThreadLocalUtil.getUserId();
        User user = userService.findById(userId);
        return Result.success(user);
    }

    @PostMapping("/updateProfile")
    public Result updateProfile(@RequestParam(required = false) String height,
                               @RequestParam(required = false) String weight,
                               @RequestParam(required = false) String sex,
                               @RequestParam(required = false) String bloodType,
                               @RequestParam(required = false) Integer age,
                               @RequestParam(required = false) String idCardNumber,
                               @RequestParam(required = false) String contactPhone,
                               @RequestParam(required = false) String emergencyContact,
                               @RequestParam(required = false) @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", message = "邮箱格式不正确") String email,
                               @RequestParam(required = false) String nickname) {

        Integer userId = ThreadLocalUtil.getUserId();
        User user = userService.findById(userId);

        if (StringUtils.hasLength(height)) user.setHeight(height);
        if (StringUtils.hasLength(weight)) user.setWeight(weight);
        if (StringUtils.hasLength(sex)) user.setSex(sex);
        if (StringUtils.hasLength(bloodType)) user.setBloodType(bloodType);
        if (age != null) user.setAge(age);
        if (StringUtils.hasLength(idCardNumber)) user.setIdCardNumber(idCardNumber);
        if (StringUtils.hasLength(contactPhone)) user.setContactPhone(contactPhone);
        if (StringUtils.hasLength(emergencyContact)) user.setEmergencyContact(emergencyContact);
        if (StringUtils.hasLength(email)) user.setEmail(email);
        if (StringUtils.hasLength(nickname)) user.setNickname(nickname);

        userService.update(user);
        return Result.success();
    }

    @GetMapping("/checkProfileComplete")
    public Result<Boolean> checkProfileComplete() {
        Integer userId = ThreadLocalUtil.getUserId();
        log.info("Checking profile completeness for user ID: {}", userId);
        boolean isComplete = userService.checkProfileComplete(userId);
        log.info("Profile complete status: {}", isComplete);
        return Result.success(isComplete);
    }

    @PostMapping("/uploadAvatar")
    public Result<String> uploadAvatar(@RequestPart("file") MultipartFile file, jakarta.servlet.http.HttpServletRequest request) {
        if (file.isEmpty()) return Result.error("文件为空");
        try {
            String originalFilename = file.getOriginalFilename();
            String filename = System.currentTimeMillis() + "_" + originalFilename;
            String baseDir = System.getProperty("user.dir");
            java.nio.file.Path uploadPath = java.nio.file.Paths.get(baseDir, "uploads");
            if (!java.nio.file.Files.exists(uploadPath)) {
                java.nio.file.Files.createDirectories(uploadPath);
            }
            java.nio.file.Path filePath = uploadPath.resolve(filename);
            file.transferTo(filePath.toFile());
            String url = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/uploads/" + filename;
            userService.updateAvatar(url);
            return Result.success(url);
        } catch (Exception e) {
            log.error("Avatar upload failed", e);
            return Result.error("上传失败");
        }
    }

    @DeleteMapping("/deleteAccount")
    public Result deleteAccount() {
        Integer userId = ThreadLocalUtil.getUserId();
        userService.deleteAccount(userId);
        return Result.success();
    }
}
