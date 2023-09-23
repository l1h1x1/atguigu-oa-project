package com.atguigu.auth.service.Impl;

import com.atguigu.auth.mapper.SysMenuMapper;
import com.atguigu.auth.service.SysMenuService;
import com.atguigu.auth.service.SysRoleMenuService;
import com.atguigu.auth.utils.MenuHelper;
import com.atguigu.common.config.exception.OwnException;
import com.atguigu.model.system.SysMenu;
import com.atguigu.model.system.SysRoleMenu;
import com.atguigu.vo.system.AssginMenuVo;
import com.atguigu.vo.system.MetaVo;
import com.atguigu.vo.system.RouterVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 菜单表 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2023-09-06
 */
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {
    @Autowired
    private SysRoleMenuService sysRoleMenuService;
    @Override
    public List<SysMenu> findNodes() {
        //1查询所有菜单的数据
        List<SysMenu> sysMenuList = baseMapper.selectList(null);
        //2构建树行结构
        List<SysMenu> resultList=MenuHelper.buildTree(sysMenuList);
        return resultList;
    }

    @Override
    public void removeMenuById(Long id) {
        //判断当前菜单是否有下一级菜单
        LambdaQueryWrapper<SysMenu>  wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMenu::getParentId,id);
        Integer count = baseMapper.selectCount(wrapper);
        if(count>0){
            throw new OwnException(201,"菜单不能删除");
        }
        baseMapper.deleteById(id);
    }
//查询所有菜单和角色分配的菜单
    @Override
    public List<SysMenu> findMenuByRoleId(Long roleId) {
        //1查询所有菜单，+上条件状态status=1
        LambdaQueryWrapper<SysMenu> wrapperSysMenu = new LambdaQueryWrapper<>();
        wrapperSysMenu.eq(SysMenu::getStatus, 1);
        List<SysMenu> allSysMenuList = baseMapper.selectList(wrapperSysMenu);
        //2根据角色id roleId查询 角色菜单关系表里面 角色id对应菜单id
        LambdaQueryWrapper<SysRoleMenu>  wrapperSysRoleMenu = new LambdaQueryWrapper<>();
        wrapperSysRoleMenu.eq(SysRoleMenu::getRoleId, roleId);
        List<SysRoleMenu> sysRoleMenuList = sysRoleMenuService.list(wrapperSysRoleMenu);
        //3根据获取菜单id 获取菜单对象
        List<Long> menuIdList = sysRoleMenuList.stream().map(c -> c.getMenuId()).collect(Collectors.toList());
        //3.1拿着菜单id 和所有菜单集合里的id进行比较 如果相同封装
        allSysMenuList.stream().forEach(item->{
            if(menuIdList.contains(item.getId())){
                item.setSelect(true);
            }
            else {
                item.setSelect(false);
            }
        });
        //4返回规定格式的菜单列表
        List<SysMenu> sysMenuList = MenuHelper.buildTree(allSysMenuList);
        return sysMenuList;
    }
    @Override
    public void doAssign(AssginMenuVo assginMenuVo) {
//根据角色id 删除菜单角色表 分配数据
        LambdaQueryWrapper<SysRoleMenu>  wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleMenu::getRoleId, assginMenuVo.getRoleId());
        sysRoleMenuService.remove(wrapper);
// 从参数里面获取角色新分配的菜单id列表
// 进行遍历 把每个id数据添加角色菜单表中去
        List<Long> menuIdList = assginMenuVo.getMenuIdList();
        for (Long menuId:menuIdList){
            if(StringUtils.isEmpty(menuId)){
                continue;
            }
            SysRoleMenu sysRoleMenu = new SysRoleMenu();
            sysRoleMenu.setMenuId(menuId);
            sysRoleMenu.setRoleId(assginMenuVo.getRoleId());
            sysRoleMenuService.save(sysRoleMenu);
        }
    }

    @Override
    public List<RouterVo> findUserMenuByUserId(Long userId) {
        List<SysMenu> menuList=null;
        //判断当前用户是否是管理员
        if(userId.longValue()==1){
            //查询所有菜单列表
            LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysMenu::getStatus, 1);
            wrapper.orderByAsc(SysMenu::getSortValue);
            menuList = baseMapper.selectList(wrapper);
        }
        //如果不是管理员就根据userId查询可以操作的菜单列表
        else {
            menuList=baseMapper.findMenuListByUserId(userId);
        }
        //把查询出来的数据列表，构建成框架要求的路由结构(这里用了MenuHelper工具类)
        List<SysMenu> sysMenuTreeList = MenuHelper.buildTree(menuList);
        //构建要求结构
        List<RouterVo> routerList=this.buildRouter(sysMenuTreeList);

        return routerList;
    }

    private List<RouterVo> buildRouter(List<SysMenu> menus) {
        List<RouterVo> routers = new LinkedList<RouterVo>();
        for (SysMenu menu : menus) {
            RouterVo router = new RouterVo();
            router.setHidden(false);
            router.setAlwaysShow(false);
            router.setPath(getRouterPath(menu));
            router.setComponent(menu.getComponent());
            router.setMeta(new MetaVo(menu.getName(), menu.getIcon()));
            List<SysMenu> children = menu.getChildren();
            //如果当前是菜单，需将按钮对应的路由加载出来，如：“角色授权”按钮对应的路由在“系统管理”下面
            if(menu.getType().intValue() == 1) {
                List<SysMenu> hiddenMenuList = children.stream()
                        .filter(item -> !StringUtils.isEmpty(item.getComponent()))
                        .collect(Collectors.toList());
                for (SysMenu hiddenMenu : hiddenMenuList) {
                    RouterVo hiddenRouter = new RouterVo();
                    hiddenRouter.setHidden(true);
                    hiddenRouter.setAlwaysShow(false);
                    hiddenRouter.setPath(getRouterPath(hiddenMenu));
                    hiddenRouter.setComponent(hiddenMenu.getComponent());
                    hiddenRouter.setMeta(new MetaVo(hiddenMenu.getName(), hiddenMenu.getIcon()));
                    routers.add(hiddenRouter);
                }
            } else {
                if (!CollectionUtils.isEmpty(children)) {
                    if(children.size() > 0) {
                        router.setAlwaysShow(true);
                    }
                    router.setChildren(buildRouter(children));
                }
            }
            routers.add(router);
        }
        return routers;
    }

    /**
     * 获取路由地址
     *
     * @param menu 菜单信息
     * @return 路由地址
     */
    public String getRouterPath(SysMenu menu) {
        String routerPath = "/" + menu.getPath();
        if(menu.getParentId().intValue() != 0) {
            routerPath = menu.getPath();
        }
        return routerPath;
    }
//根据用户id获取操作按钮列表
    @Override
    public List<String> findUserPermsByUserId(Long userId) {
        List<SysMenu> menuList=null;
        //判断当前用户是否是管理员
        if(userId.longValue()==1){
            //查询所有菜单列表
            LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysMenu::getStatus, 1);
            menuList = baseMapper.selectList(wrapper);
        }
        else{
            menuList=baseMapper.findMenuListByUserId(userId);
        }
        List<String> permsList = menuList.stream()
                .filter(item -> item.getType() == 2)
                .map(item -> item.getPerms())
                .collect(Collectors.toList());

        return permsList;
    }
}
