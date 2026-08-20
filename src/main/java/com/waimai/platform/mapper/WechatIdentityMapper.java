package com.waimai.platform.mapper;

import com.waimai.platform.model.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface WechatIdentityMapper {

    @Select("""
            SELECT u.id, u.username, u.password_hash, u.nickname, u.phone, u.role, u.status,
                   u.created_at, u.updated_at
            FROM wm_wechat_identity w JOIN wm_user u ON u.id = w.user_id
            WHERE w.openid = #{openid}
            """)
    User findUserByOpenid(String openid);

    @Insert("INSERT INTO wm_wechat_identity (user_id, openid) VALUES (#{userId}, #{openid})")
    int insert(Long userId, String openid);
}
