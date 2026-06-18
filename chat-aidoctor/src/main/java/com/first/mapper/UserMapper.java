package com.first.mapper;

import com.first.pojo.User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Param;


@Mapper
public interface UserMapper {
    //根据用户名查询用户
    @Select("SELECT * FROM user WHERE username = #{username}")
    User findByUserName(String username);

    //添加用户
    @Insert("INSERT INTO user (username, password,create_time,update_time)"+
            "values (#{username}, #{password}, now(), now())")
    void add(@Param("username") String username, @Param("password") String password);

    @Update("UPDATE user SET nickname = #{nickname}, height = #{height}, " +
            "weight = #{weight}, sex = #{sex}, blood_type = #{bloodType}, " +
            "age = #{age}, id_card_number = #{idCardNumber}, " +
            "contact_phone = #{contactPhone}, emergency_contact = #{emergencyContact}, " +
            "email = #{email}, update_time = now() WHERE id = #{id}")
    void update(User user);

    @Update("UPDATE user SET user_pic = #{avatarUrl}, update_time = now() WHERE id = #{id}")
    void updateAvatar(@Param("avatarUrl") String avatarUrl, @Param("id") Integer id);

    @Update("UPDATE user SET password = #{md5String}, update_time = now() WHERE id = #{id}")
    void updatePwd(@Param("md5String") String md5String, @Param("id") Integer id);
    
    @Select("SELECT COUNT(*) > 0 FROM user WHERE id = #{id} AND " +
            "height IS NOT NULL AND weight IS NOT NULL AND sex IS NOT NULL AND " +
            "blood_type IS NOT NULL AND age IS NOT NULL AND " +
            "id_card_number IS NOT NULL AND contact_phone IS NOT NULL AND " +
            "emergency_contact IS NOT NULL")
    boolean checkProfileComplete(Integer id);

    @Delete("DELETE FROM user WHERE id = #{id}")
    void deleteById(Integer id);

    //根据ID查询用户
    @Select("SELECT * FROM user WHERE id = #{id}")
    User findById(Integer id);

    // 根据邮箱查询用户
    @Select("SELECT * FROM user WHERE email = #{email}")
    User findByEmail(String email);

    // 更新邮箱
    @Update("UPDATE user SET email = #{email}, update_time = now() WHERE id = #{id}")
    void updateEmail(@Param("id") Integer id, @Param("email") String email);

    // 查询全部用户（用于邮件计划提醒）
    @Select("SELECT * FROM user")
    java.util.List<User> findAllUsers();
}
