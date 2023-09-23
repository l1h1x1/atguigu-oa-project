package com.atguigu.auth.service.Impl;

import com.atguigu.auth.mapper.SysUserMapper;
import com.atguigu.auth.service.SysUserService;
import com.atguigu.common.result.LoginUserInfoHelper;
import com.atguigu.model.system.SysUser;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2023-09-04
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Override
    public void updateStatus(Long id, Integer status) {
        //根据用户id查询用户对象
        SysUser sysUser = baseMapper.selectById(id);
        //更改状态
        sysUser.setStatus(status);
        //调用方法实现更新
        baseMapper.updateById(sysUser);
    }

    @Override
    public SysUser getByUsername(String username) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        SysUser sysUser = baseMapper.selectOne(wrapper);
        return sysUser;
    }

    @Override
    public Map<String, Object> getCurrentUser() {
        SysUser sysUser = baseMapper.selectById(LoginUserInfoHelper.getUserId());
        HashMap<String, Object> map = new HashMap<>();
        map.put("name",sysUser.getName());
        map.put("phone", sysUser.getPhone());
        return map;
    }
}
