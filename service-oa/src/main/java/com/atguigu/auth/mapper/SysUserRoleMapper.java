package com.atguigu.auth.mapper;

import com.atguigu.model.system.SysUserRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * <p>
 * 用户角色 Mapper 接口
 * </p>
 *
 * @author atguigu
 * @since 2023-09-04
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

}
