package com.atguigu.auth;

import com.atguigu.auth.service.SysRoleService;
import com.atguigu.model.system.SysRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @author AXiang
 * @create 2023/9/1 11:12
 **/
@SpringBootTest
public class SysRoleServiceTest02 {
    @Autowired
    private SysRoleService service;
    @Test
    public void getAll(){
        List<SysRole> lists = service.list();
        System.out.println(lists);
    }
}
