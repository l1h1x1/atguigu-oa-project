package com.atguigu.auth.service.Impl;

import com.atguigu.auth.service.SysMenuService;
import com.atguigu.auth.service.SysUserService;
import com.atguigu.model.system.SysUser;
import com.atguigu.security.custom.CustomUser;
import com.atguigu.security.custom.UserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author AXiang
 * @create 2023/9/9 15:12
 **/
@Service
public class UserDetailsServiceImp implements UserDetailsService {
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private SysMenuService sysMenuService;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser sysUser =sysUserService.getByUsername(username);
        if(null == sysUser) {
            throw new UsernameNotFoundException("用户名不存在！");
        }

        if(sysUser.getStatus().intValue() == 0) {
            throw new RuntimeException("账号已停用");
        }
        //根据用户id查询权限数据
        List<String> userPermsList = sysMenuService.findUserPermsByUserId(sysUser.getId());
        //创建list集合 用于封装权限数据（类型被固定了）
        List<SimpleGrantedAuthority> authList=new ArrayList<>();
        for (String perm:userPermsList){
            authList.add(new SimpleGrantedAuthority(perm.trim()));
        }
        return new CustomUser(sysUser, authList);
    }
}
