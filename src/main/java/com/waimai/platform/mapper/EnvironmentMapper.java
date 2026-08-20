package com.waimai.platform.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EnvironmentMapper {

    @Select("SELECT 1")
    int ping();
}
