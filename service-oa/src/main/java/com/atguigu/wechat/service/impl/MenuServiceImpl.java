package com.atguigu.wechat.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.model.wechat.Menu;
import com.atguigu.vo.wechat.MenuVo;
import com.atguigu.wechat.mapper.MenuMapper;
import com.atguigu.wechat.service.MenuService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 菜单 服务实现类
 * </p>
 *
 * @author aXian
 * @since 2023-09-18
 */
@Service
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuService {
    @Autowired
    private WxMpService wxMpService;

    @Override
    public List<MenuVo> findMenuInfo() {
        ArrayList<MenuVo> list = new ArrayList<>();
        //查询所有菜单list集合
        List<Menu> menuList = baseMapper.selectList(null);
        //查询所有一级菜单 parent_id=0 返回list集合
        List<Menu> oneMenuList = menuList.stream().filter(menu -> menu.getParentId().longValue() == 0)
                .collect(Collectors.toList());
        //把一级菜单list集合进行遍历 得到每个一级菜单
        for (Menu oneMenu : oneMenuList) {
            MenuVo oneMenuVo = new MenuVo();
            //把menu->menuVo
            BeanUtils.copyProperties(oneMenu, oneMenuVo);
            //获取每个一级菜单里的二级菜单id与parent_id 进行比较
            List<Menu> towMenuList = menuList.stream().filter(menu -> menu.getParentId().longValue() == oneMenu.getId())
                    .collect(Collectors.toList());
            //把一级菜单里的二级菜单获取 封装到一级菜单children集合
            ArrayList<MenuVo> children = new ArrayList<>();
            for (Menu towMenu : towMenuList) {
                MenuVo towMenuVo = new MenuVo();
                //把menu->menuVo
                BeanUtils.copyProperties(towMenu, towMenuVo);
                children.add(towMenuVo);
            }
            oneMenuVo.setChildren(children);
            list.add(oneMenuVo);
        }
        return list;
    }

    @Override
    public void syncMenu() {
        List<MenuVo> menuVoList = this.findMenuInfo();
        //菜单
        JSONArray buttonList = new JSONArray();
        for (MenuVo oneMenuVo : menuVoList) {
            JSONObject one = new JSONObject();
            one.put("name", oneMenuVo.getName());
            if (CollectionUtils.isEmpty(oneMenuVo.getChildren())) {
                one.put("type", oneMenuVo.getType());
                one.put("url", "http://oa.atguigu.cn/#" + oneMenuVo.getUrl());//oa.atguigu.cn->端口9090的域名
            } else {
                JSONArray subButton = new JSONArray();
                for (MenuVo twoMenuVo : oneMenuVo.getChildren()) {
                    JSONObject view = new JSONObject();
                    view.put("type", twoMenuVo.getType());
                    if (twoMenuVo.getType().equals("view")) {
                        view.put("name", twoMenuVo.getName());
                        //H5页面地址
                        view.put("url", "http://oa.atguigu.cn#" + twoMenuVo.getUrl());//oa.atguigu.cn->端口9090的域名
                    } else {
                        view.put("name", twoMenuVo.getName());
                        view.put("key", twoMenuVo.getMeunKey());
                    }
                    subButton.add(view);
                }
                one.put("sub_button", subButton);
            }
            buttonList.add(one);
        }
        //菜单
        JSONObject button = new JSONObject();
        button.put("button", buttonList);
        try {
            wxMpService.getMenuService().menuCreate(button.toString());
        } catch (WxErrorException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeMenu() {
        try {
            wxMpService.getMenuService().menuDelete();
        } catch (WxErrorException e) {
            throw new RuntimeException(e);
        }
    }
}
