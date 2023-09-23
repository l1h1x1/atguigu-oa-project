package com.atguigu.auth.controller;

import com.atguigu.auth.service.SysMenuService;
import com.atguigu.auth.service.SysUserService;
import com.atguigu.common.config.exception.OwnException;
import com.atguigu.common.jwt.JwtHelper;
import com.atguigu.common.result.Result;
import com.atguigu.common.utils.MD5;
import com.atguigu.model.system.SysUser;
import com.atguigu.vo.system.LoginVo;
import com.atguigu.vo.system.RouterVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import net.bytebuddy.implementation.bytecode.Throw;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author AXiang
 * @create 2023/9/3 10:52
 **/

/**
 * <p>
 * 后台登录登出
 * </p>
 */
@Api(tags = "后台登录管理")
@RestController
@RequestMapping("/admin/system/index")
public class IndexController {
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private SysMenuService sysMenuService;
    /**
     * 登录
     *
     * @return
     */
    @PostMapping("login")
    public Result login(@RequestBody  LoginVo loginVo) {
        String username = loginVo.getUsername();
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        SysUser sysUser = sysUserService.getOne(wrapper);

        //判断
        if(sysUser==null){
            throw new OwnException(201,"输入用户名错误");
        }
        //密码判断
        String password = loginVo.getPassword();
        String password_input = MD5.encrypt(password);
        String password_dp = sysUser.getPassword();
        if(!password_dp.equals(password_input)){
            throw new OwnException(201,"输入密码错误");
        }
        //查看是否被禁用
        if(sysUser.getStatus().intValue()==0){
            throw new OwnException(201,"该用户被禁用");
        }
        //使用JWT对其进行id和用户名生成字符串
        String token = JwtHelper.createToken(sysUser.getId(), sysUser.getUsername());
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        return Result.ok(map);
    }

    /**
     * 获取用户信息
     *
     * @return
     */
    @GetMapping("info")
    public Result info(HttpServletRequest request) {
        //从请求头获取用户信息(获取请求头字符串)
        String token = request.getHeader("token");
        Long userId =JwtHelper.getUserId(token);
        SysUser sysUser = sysUserService.getById(userId);
        List<RouterVo> routerList =sysMenuService.findUserMenuByUserId(userId);
        List<String> permsList=sysMenuService.findUserPermsByUserId(userId);

        Map<String, Object> map = new HashMap<>();
        map.put("roles", "[admin]");
        map.put("name", sysUser.getName());
        //返回用户操作的菜单
        map.put("routers", routerList);
        //返回用户操作的按钮
        map.put("buttons", permsList);
        map.put("avatar", "https://oss.aliyuncs.com/aliyun_id_photo_bucket/default_handsome.jpg");
        return Result.ok(map);
    }

    /**
     * 退出
     *
     * @return
     */
    @PostMapping("logout")
    public Result logout() {
        return Result.ok();
    }
}
