package com.atguigu.process.controller.api;

import com.atguigu.auth.service.SysUserService;
import com.atguigu.common.result.Result;
import com.atguigu.model.process.Process;
import com.atguigu.model.process.ProcessTemplate;
import com.atguigu.model.process.ProcessType;
import com.atguigu.process.service.ProcessService;
import com.atguigu.process.service.ProcessTemplateService;
import com.atguigu.process.service.ProcessTypeService;
import com.atguigu.vo.process.ApprovalVo;
import com.atguigu.vo.process.ProcessFormVo;
import com.atguigu.vo.process.ProcessVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author AXiang
 * @create 2023/9/13 15:22
 **/
@Api("审批流程管理")
@RestController
@RequestMapping("admin/process")
@CrossOrigin
public class OaProcessController {
    @Autowired
    private ProcessTypeService processTypeService;
    @Autowired
    private ProcessTemplateService processTemplateService;
    @Autowired
    private ProcessService processService;
    @Autowired
    private SysUserService sysUserService;
    //查询所有审批分类和每个分类所有审批模板
    @GetMapping("findProcessType")
    public Result findProcessType(){
        List<ProcessType> list =processTypeService.findProcessType();
        return Result.ok(list);
    }
    @ApiOperation("获取审批模板数据")
    @GetMapping("getProcessTemplate/{processTemplateId}")
    public Result getProcessTemplate(@PathVariable Long processTemplateId){
        ProcessTemplate processTemplate = processTemplateService.getById(processTemplateId);
        return Result.ok(processTemplate);

    }
    @ApiOperation("启动流程")
    @PostMapping("startUp")
    public Result startUp(@RequestBody ProcessFormVo processFormVo){
        processService.startUp(processFormVo);
        return Result.ok();
    }
    //查询待处理任务列表
    @ApiOperation(value = "待处理")
    @GetMapping("/findPending/{page}/{limit}")
    public Result findPending(@PathVariable Long page,
                              @PathVariable Long limit){
        Page<Process> pageParam = new Page<>(page, limit);
        IPage<ProcessVo> pageModel=processService.findfindPending(pageParam);
        return Result.ok(pageModel);
    }
    //审批详细
    @ApiOperation("获取审批详情")
    @GetMapping("show/{id}")
    public  Result show(@PathVariable Long id ){
        return Result.ok( processService.show(id));
    }
    //审批
    @ApiOperation("审批")
    @PostMapping("approve")
    public Result approve(@RequestBody ApprovalVo approvalVo){
        processService.approve(approvalVo);
        return Result.ok();
    }
    @ApiOperation("已处理")
    @GetMapping("findProcessed/{page}/{limit}")
    public Result findProcessed(@PathVariable Long page,
                              @PathVariable Long limit){
        Page<Process> pageParam = new Page<>(page,limit);
        IPage<ProcessVo> pageModel=processService.findProcessed(pageParam);
        return Result.ok(pageModel);
    }
    @ApiOperation("已发起")
    @GetMapping("findStarted/{page}/{limit}")
    public Result findStarted(@PathVariable Long page,
                                @PathVariable Long limit){
        Page<ProcessVo> pageParam = new Page<>(page,limit);
        IPage<ProcessVo> pageModel=processService.findStarted(pageParam);
        return Result.ok(pageModel);
    }
    @ApiOperation("获取当前用户的信息")
    @GetMapping("getCurrentUser")
    public  Result getCurrentUser(){
        Map<String,Object> map=sysUserService.getCurrentUser();
        return  Result.ok(map);
    }
}
