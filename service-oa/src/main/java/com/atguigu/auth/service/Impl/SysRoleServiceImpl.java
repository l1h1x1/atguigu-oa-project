package com.atguigu.auth.service.Impl;

import com.atguigu.auth.mapper.SysRoleMapper;
import com.atguigu.auth.service.SysRoleService;
import com.atguigu.auth.service.SysUserRoleService;
import com.atguigu.model.system.SysRole;
import com.atguigu.model.system.SysUserRole;
import com.atguigu.vo.system.AssginRoleVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author AXiang
 * @create 2023/9/1 11:08
 **/
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {
    @Autowired
    private SysUserRoleService sysUserRoleService;
    //查询所有角色 和当前用户所属角色
    @Override
    public Map<String, Object> findRoleDataByUserId(Long userId) {
        //查询所有角色
        List<SysRole> allRoleList = baseMapper.selectList(null);
        //根据用户id查询，角色用户关系表 查询userid对应所有角色id
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId, userId);
        List<SysUserRole>  existUserRoleList= sysUserRoleService.list(wrapper);
        //stream流与lambada表达式
        List<Long>  existUserRoleIdList = existUserRoleList.stream().map(c -> c.getRoleId()).collect(Collectors.toList());
        List<SysRole> assignRoleList=new ArrayList<>();
        for (SysRole sysRole:allRoleList)
        {
            //比较
            if(existUserRoleIdList.contains(sysRole.getId())){
                assignRoleList.add(sysRole);
            }
        }
        //把得到的两部分数据封装到map集合
        Map<String, Object> roleMap = new HashMap<>();
        roleMap.put("assginRoleList", assignRoleList);
        roleMap.put("allRolesList", allRoleList);
        return roleMap;
    }
//2为用户分配角色
    @Override
    @Transactional
    public void doAssign(AssginRoleVo assginRoleVo) {
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId, assginRoleVo.getUserId());
        sysUserRoleService.remove(wrapper);
        List<Long> roleIdList = assginRoleVo.getRoleIdList();
        for (Long roleId : roleIdList) {
            if (StringUtils.isEmpty(roleId)) {
                continue;
            }
            SysUserRole sysUserRole = new SysUserRole();
            sysUserRole.setUserId(assginRoleVo.getUserId());
            sysUserRole.setRoleId(roleId);
            sysUserRoleService.save(sysUserRole);

        }
     }
    }
