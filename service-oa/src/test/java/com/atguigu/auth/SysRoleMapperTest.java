package com.atguigu.auth;

import com.atguigu.auth.mapper.SysRoleMapper;
import com.atguigu.model.system.SysRole;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

/**
 * @author AXiang
 * @create 2023/9/1 9:36
 **/
@SpringBootTest
public class SysRoleMapperTest {
    @Autowired
    private SysRoleMapper mapper;
    @Test
//    查询所有数据
    public void getAll(){
        List<SysRole> list = mapper.selectList(null);
        System.out.println(list);
    }
//    添加一条数据
    @Test
    public void add(){
        SysRole sysRole = new SysRole();
        sysRole.setRoleName("李四");
        sysRole.setRoleCode("role");
        sysRole.setDescription("角色管理");
        int rows = mapper.insert(sysRole);
        System.out.println(rows);
        System.out.println(sysRole.getId());
    }
//    修改数据
    @Test
    public void set(){
        SysRole role = mapper.selectById(9);
        role.setRoleName("atguigu李四");
        int rows = mapper.updateById(role);
        System.out.println(rows);
    }
//    删除操作（这里指的逻辑删除）
    @Test
    public void deleteId(){
        int rows = mapper.deleteById(9);
        System.out.println(rows);
    }
//    批量删除
    @Test
    public void testDeleteBathIds(){
        int rows = mapper.deleteBatchIds(Arrays.asList(1, 2));
        System.out.println(rows);
    }
//    条件查询
    @Test
    public void testQuery1(){
        QueryWrapper<SysRole> wrapper = new QueryWrapper<>();
         wrapper.eq("role_name", "atguigu李四");
        List<SysRole> lists = mapper.selectList(wrapper);
        System.out.println(lists);
    }
    @Test
    public void testQuery2(){
        LambdaQueryWrapper<SysRole>  wrapper = new LambdaQueryWrapper<>();
        wrapper.eq( SysRole::getRoleName, "atguigu李四");
        List<SysRole> lists = mapper.selectList(wrapper);
        System.out.println(lists);
    }
}
