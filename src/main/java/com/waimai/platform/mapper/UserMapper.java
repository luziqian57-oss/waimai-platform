package com.waimai.platform.mapper;

import com.waimai.platform.model.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    @Select("""
            SELECT id, username, password_hash, nickname, phone, role, status, created_at, updated_at
            FROM wm_user
            WHERE username = #{username}
            LIMIT 1
            """)
    User findByUsername(String username);

    @Select("""
            SELECT id, username, password_hash, nickname, phone, role, status, created_at, updated_at
            FROM wm_user
            WHERE id = #{id}
            LIMIT 1
            """)
    User findById(Long id);

    @Insert("""
            INSERT INTO wm_user (username, password_hash, nickname, phone, role, status)
            VALUES (#{username}, #{passwordHash}, #{nickname}, #{phone}, #{role}, #{status})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);
}
