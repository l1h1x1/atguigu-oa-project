package com.atguigu.auth.service;

import com.atguigu.model.system.SysRole;
import com.atguigu.vo.system.AssginRoleVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * @author AXiang
 * @create 2023/9/1 11:05
 **/
public interface SysRoleService extends IService<SysRole> {
    //查询所有角色 和当前用户所属角色
    Map<String, Object> findRoleDataByUserId(Long userId);
    //为用户分配角色
    void doAssign(AssginRoleVo assginRoleVo);
}
