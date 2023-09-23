package com.atguigu.process.controller;


import com.atguigu.common.result.Result;
import com.atguigu.process.service.ProcessService;
import com.atguigu.vo.process.ProcessQueryVo;
import com.atguigu.vo.process.ProcessVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 审批类型 前端控制器
 * </p>
 *
 * @author aXian
 * @since 2023-09-12
 */
@Api(tags = "审批管理")
@RestController
@RequestMapping(value = "/admin/process")
public class ProcessController {
    @Autowired
    private ProcessService processService;
    @ApiOperation("获取分页审批列表")
    @GetMapping("{page}/{limit}")
    public Result index(@PathVariable Long page,
                        @PathVariable Long limit,
                        ProcessQueryVo processQueryVo){
        Page<ProcessVo> pageParam = new Page<>(page, limit);
        IPage<ProcessVo> pageModel=processService.selectPage(pageParam,processQueryVo);
        return Result.ok(pageModel);
    }

}

