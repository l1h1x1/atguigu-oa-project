package com.atguigu.auth.controller;

import com.atguigu.auth.service.SysRoleService;
import com.atguigu.common.config.exception.OwnException;
import com.atguigu.common.result.Result;
import com.atguigu.model.system.SysRole;
import com.atguigu.vo.system.AssginRoleVo;
import com.atguigu.vo.system.SysRoleQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author AXiang
 * @create 2023/9/1 15:07
 **/
@RestController
@RequestMapping("/admin/system/sysRole")
@Api(tags = "角色管理")
public class SysRoleController {
    @Autowired
    private SysRoleService sysRoleService;

    //查询所有角色 和当前用户所属角色
    @ApiOperation(value = "根据用户获取角色数据")
    @GetMapping("/toAssign/{userId}")
    public Result toAssign(@PathVariable Long userId) {
        Map<String, Object> map = sysRoleService.findRoleDataByUserId(userId);
        return Result.ok(map);
    }

    //为用户分配角色
    @ApiOperation(value = "为用户分配角色")
    @PostMapping("/doAssign")
    public Result doAssign(@RequestBody AssginRoleVo assginRoleVo) {
        sysRoleService.doAssign(assginRoleVo);
        return Result.ok();
    }

    //    @GetMapping("/findAll")
//    public List<SysRole> findAll(){
//        List<SysRole> lists = sysRoleService.list();
//        return lists;
//    }
//    统一数据返回结果
    @GetMapping("/findAll")
    @ApiOperation(value = "获取角色全部列表")
    public Result findAll() {
        List<SysRole> lists = sysRoleService.list();
        return Result.ok(lists);
    }

    //    条件分页查询
    //page 当前页  limit 每页显示记录数
    //SysRoleQueryVo 条件对象
    @ApiOperation("条件分页查询")
    @GetMapping("{page}/{limit}")
    public Result pageQueryRole(@PathVariable Long page,
                                @PathVariable Long limit,
                                SysRoleQueryVo sysRoleQueryVo) {
//        1创建page对象，传递分页相关参数
        Page<SysRole> pageParam = new Page<>(page, limit);
//        2封装条件，判断条件是否为空，不为空则进行封装
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        String roleName = sysRoleQueryVo.getRoleName();
        if (!StringUtils.isEmpty(roleName)) {
            wrapper.like(SysRole::getRoleName, roleName);
        }
//        3调用方法
        Page<SysRole> page1 = sysRoleService.page(pageParam, wrapper);
        return Result.ok(page1);
    }

    //    添加角色
    @PreAuthorize("hasAuthority('bnt.sysRole.add')")
    @ApiOperation("添加角色")
    @PostMapping("save")
    public Result save(@RequestBody SysRole role) {
        boolean is_success = sysRoleService.save(role);
        if (is_success) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }

    //修改角色 1根据ID查询
    @PreAuthorize("hasAuthority('bnt.sysRole.list')")
    @ApiOperation("根据id查询")
    @GetMapping("get/{id}")
    public Result get(@PathVariable Long id) {
        SysRole sysRole = sysRoleService.getById(id);
        return Result.ok(sysRole);
    }

    //修改角色 最终的修改
    @PreAuthorize("hasAuthority('bnt.sysRole.update')")
    @ApiOperation("修改角色")
    @PutMapping("update")
    public Result update(@RequestBody SysRole role) {
        boolean is_success = sysRoleService.updateById(role);
        if (is_success) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }

    //根据ID进行删除
    @PreAuthorize("hasAuthority('bnt.sysRole.remove')")
    @ApiOperation("根据id进行删除")
    @DeleteMapping("remove/{id}")
    public Result delete(@PathVariable Long id) {
        boolean is_success = sysRoleService.removeById(id);
        if (is_success) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }

    //    批量删除
    @PreAuthorize("hasAuthority('bnt.sysRole.remove')")
    @ApiOperation("批量删除")
    @DeleteMapping("batchRemove")
    public Result batchRemove(@RequestBody List<Long> idList) {
        boolean is_success = sysRoleService.removeByIds(idList);
        if (is_success) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }
}
