package com.waimai.platform.mapper;

import com.waimai.platform.model.Address;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface AddressMapper {

    @Select("""
            SELECT id, user_id, contact_name, contact_phone, province, city, district, detail_address, is_default
            FROM wm_user_address WHERE user_id = #{userId}
            ORDER BY is_default DESC, id DESC
            """)
    List<Address> findByUserId(Long userId);

    @Select("""
            SELECT id, user_id, contact_name, contact_phone, province, city, district, detail_address, is_default
            FROM wm_user_address WHERE id = #{id} AND user_id = #{userId}
            """)
    Address findOwned(Long id, Long userId);

    @Insert("""
            INSERT INTO wm_user_address
            (user_id, contact_name, contact_phone, province, city, district, detail_address, is_default)
            VALUES (#{userId}, #{contactName}, #{contactPhone}, #{province}, #{city}, #{district},
                    #{detailAddress}, #{isDefault})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Address address);

    @Update("""
            UPDATE wm_user_address SET contact_name = #{contactName}, contact_phone = #{contactPhone},
                province = #{province}, city = #{city}, district = #{district},
                detail_address = #{detailAddress}, is_default = #{isDefault}
            WHERE id = #{id} AND user_id = #{userId}
            """)
    int update(Address address);

    @Update("UPDATE wm_user_address SET is_default = 0 WHERE user_id = #{userId}")
    int clearDefault(Long userId);

    @Update("UPDATE wm_user_address SET is_default = 1 WHERE id = #{id} AND user_id = #{userId}")
    int setDefault(Long id, Long userId);

    @Delete("DELETE FROM wm_user_address WHERE id = #{id} AND user_id = #{userId}")
    int deleteOwned(Long id, Long userId);
}
