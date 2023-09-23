package com.atguigu.process.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.auth.service.SysUserService;
import com.atguigu.common.result.LoginUserInfoHelper;
import com.atguigu.model.process.Process;
import com.atguigu.model.process.ProcessRecord;
import com.atguigu.model.process.ProcessTemplate;
import com.atguigu.model.system.SysUser;
import com.atguigu.process.mapper.ProcessMapper;
import com.atguigu.process.service.ProcessRecordService;
import com.atguigu.process.service.ProcessService;
import com.atguigu.process.service.ProcessTemplateService;
import com.atguigu.vo.process.ApprovalVo;
import com.atguigu.vo.process.ProcessFormVo;
import com.atguigu.vo.process.ProcessQueryVo;
import com.atguigu.vo.process.ProcessVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

/**
 * <p>
 * 审批类型 服务实现类
 * </p>
 *
 * @author aXian
 * @since 2023-09-12
 */
@Service
public class ProcessServiceImpl extends ServiceImpl<ProcessMapper, Process> implements ProcessService {
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private ProcessTemplateService processTemplateService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private ProcessRecordService processRecordService;

    @Override
    public IPage<ProcessVo> selectPage(Page<ProcessVo> pageParam, ProcessQueryVo processQueryVo) {
        IPage<ProcessVo> pageModel = baseMapper.selectByPage(pageParam, processQueryVo);
        return pageModel;
    }

    @Override
    public void deployByZip(String deployPath) {
//        定义zip输入流
//        String path = "";
//        try {
//            path = new File(ResourceUtils.getURL("classpath:").getPath()).getAbsolutePath();
//            System.out.println(path+deployPath);
//            InputStream inputStream = new FileInputStream(path + deployPath);
//            ZipInputStream zipInputStream = new ZipInputStream(inputStream);
//            Deployment deployment = repositoryService.createDeployment()
//                    .addZipInputStream(zipInputStream)
//                    .name("请假申请流程")
//                    .deploy();
//        } catch (Exception e) {
//        }
        InputStream inputStream =
                this.getClass().getClassLoader().getResourceAsStream(deployPath);
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        //部署
        Deployment deployment = repositoryService.createDeployment()
                .addZipInputStream(zipInputStream)
                .name("请假申请流程")
                .deploy();
        System.out.println(deployment.getId());
        System.out.println(deployment.getName());
    }

    @Override
    public void startUp(ProcessFormVo processFormVo) {
        //根据用户id获取用户信息
        SysUser sysUser = sysUserService.getById(LoginUserInfoHelper.getUserId());
        //根据审批模板id获取审批模板信息
        ProcessTemplate processTemplate = processTemplateService.getById(processFormVo.getProcessTemplateId());
        //保存提交审批信息到业务表 Oa_process
        Process process = new Process();
        //把processFormVo复制到process(相同属性值)
        BeanUtils.copyProperties(processFormVo, process);
        //其他值
        String workNo = System.currentTimeMillis() + "";
        process.setProcessCode(workNo);
        process.setUserId(LoginUserInfoHelper.getUserId());
        process.setFormValues(processFormVo.getFormValues());
        process.setTitle(sysUser.getName() + "发起" + processTemplate.getName() + "申请");
        process.setStatus(1);
        baseMapper.insert(process);

        //流程定义key
        String processDefinitionKey = processTemplate.getProcessDefinitionKey();
        //业务key->processId
        String businessKey = String.valueOf(process.getId());
        //流程参数form表单json数据 转为map集合
        String formValues = processFormVo.getFormValues();
        //formData
        JSONObject jsonObject = JSON.parseObject(formValues);
        JSONObject formData = jsonObject.getJSONObject("formData");
        //遍历formData得到内容 封装到map集合
        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, Object> entry : formData.entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }
        Map<String, Object> variables = new HashMap<>();
        variables.put("data", map);
        //启动流程实例-RuntimeService
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processDefinitionKey, businessKey, variables);
        //查询下一个审批人
        //审批人可能多个
        List<Task> taskList = this.getCurrentTaskList(processInstance.getId());
        ArrayList<String> nameList = new ArrayList<>();
        //遍历
        for (Task task : taskList) {
            String assigneeName = task.getAssignee();//登入名
            SysUser user = sysUserService.getByUsername(assigneeName);
            String name = user.getUsername();//真实名称
            nameList.add(name);
            //TODO推送消息
        }
        //业务和流程关联 更新Oa_process表
        process.setProcessInstanceId(processInstance.getId());
        process.setDescription("等待" + StringUtils.join(nameList.toArray(), ",") + "审批");//nameList.toString()
        baseMapper.updateById(process);
        //记录操作审批信息记录
        processRecordService.record(process.getId(), 1, "发起申请");
    }

    @Override
    public IPage<ProcessVo> findfindPending(Page<Process> pageParam) {
        //封装查询条件 根据当前用户登入的名称
        TaskQuery query = taskService.createTaskQuery()
                .taskAssignee(LoginUserInfoHelper.getUsername())
                .orderByTaskCreateTime()
                .desc();
        //调用方法分页条件查询 返回list集合 代办任务集合
        int begin = (int) ((pageParam.getCurrent() - 1) * pageParam.getSize());
        int size = (int) pageParam.getSize();
        List<Task> taskList = query.listPage(begin, size);
        long totalCount = query.count();
        //封装返回list集合数据到 list<processVo>里去
        //List<Task>----->list<processVo>
        ArrayList<ProcessVo> processList = new ArrayList<>();
        for (Task task : taskList) {
            //从task中获取流程实例id
            String processInstanceId = task.getProcessInstanceId();
            //根据流程实例id获取实例对象
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
            //根据实例对象获取业务key-就是processId
            String businessKey = processInstance.getBusinessKey();
            if (businessKey == null) {
                continue;
            }
            //根据业务key获取process对象
            long processId = Long.parseLong(businessKey);
            Process process = baseMapper.selectById(processId);
            //process对象复制到processVo对象
            ProcessVo processVo = new ProcessVo();
            BeanUtils.copyProperties(process, processVo);
            processVo.setTaskId(task.getId());
            //放到最终list集合processList里去
            processList.add(processVo);
        }
        //封装返回IPage对象
        IPage<ProcessVo> page = new Page<ProcessVo>(pageParam.getCurrent(), pageParam.getSize(), totalCount);
        page.setRecords(processList);
        return page;
    }

    @Override
    public Map<String, Object> show(Long id) {
        //根据流程id->获取流程信息，获取流程记录信息，查询模版信息
        Process process = baseMapper.selectById(id);
        LambdaQueryWrapper<ProcessRecord>  wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProcessRecord::getProcessId, id);
        List<ProcessRecord> processRecordList = processRecordService.list(wrapper);
        ProcessTemplate processTemplateList = processTemplateService.getById(process.getProcessTemplateId());
        //判断当前用户是否可以审批，不能重复审批
        boolean isApprove=false;
        List<Task> currentTaskList = this.getCurrentTaskList(process.getProcessInstanceId());
        for (Task task:currentTaskList){
            String username = LoginUserInfoHelper.getUsername();
            if(task.getAssignee().equals(username)){
                isApprove=true;
            }
        }
        //查询数据封装到map集合
        Map<String, Object> map = new HashMap<>();
        map.put("process", process);
        map.put("processRecordList", processRecordList);
        map.put("processTemplate", processTemplateList);
        map.put("isApprove", isApprove);
        return map;

    }

    @Override
    public void approve(ApprovalVo approvalVo) {
        //根据approvalVo获取任务id 根据任务id获取流程变量
        String taskId = approvalVo.getTaskId();
        Map<String, Object> variables = taskService.getVariables(taskId);
        for(Map.Entry<String,Object> entry :variables.entrySet()){
            System.out.println(entry.getKey());
            System.out.println(entry.getValue());
        }
        //判断审批状态 1表示 审批通过 -1 表示审批驳回
        if (approvalVo.getStatus()==1){
            Map<String, Object> variable=new HashMap<>();
            taskService.complete(taskId,variable);
        }else {
            //驳回（结束流程）
            this.endTask(taskId);
        }
        //记录审批相关过程信息->oa_process_record
        String description= approvalVo.getStatus().intValue()==1 ? "已通过":"驳回";
        processRecordService.record(approvalVo.getProcessId(), approvalVo.getStatus(),description );
        //查询下一个审批人，更新process表记录
        Process process = baseMapper.selectById(approvalVo.getProcessId());
        //查询任务
        List<Task>  taskList = this.getCurrentTaskList(process.getProcessInstanceId());
        if(!CollectionUtils.isEmpty(taskList)){
            ArrayList<String > assignList = new ArrayList<>(); //真实姓名
            for(Task task : taskList){
                String assignee = task.getAssignee();
                SysUser sysUser = sysUserService.getByUsername(assignee);
                assignList.add(sysUser.getName());
                //TODD公众号推送
            }
            //更新process流程信息
            process.setDescription("等待" + StringUtils.join(assignList.toArray(), ",") + "审批");
            process.setStatus(1);
        }else{
            if(approvalVo.getStatus().intValue()==1){
                process.setDescription("审批通过");
                approvalVo.setStatus(2);
            }
            else {
                process.setDescription("审批驳回");
                approvalVo.setStatus(-1);
            }
        }
        baseMapper.updateById(process);
    }

    @Override
    public IPage<ProcessVo> findProcessed(Page<Process> pageParam) {
        //封装查询条件
        HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(LoginUserInfoHelper.getUsername())
                .finished().orderByTaskCreateTime().desc();
        //调用方法条件分页查询  返回list集合
        //开始页 和每页计入数
        int begin=(int)((pageParam.getCurrent()-1)*(pageParam.getSize()));
        int size=(int)pageParam.getSize();
        List<HistoricTaskInstance> list = query.listPage(begin, size);
        long total = query.count();
        ArrayList<ProcessVo> processVoList = new ArrayList<>();
        for(HistoricTaskInstance temp:list){
            String processInstanceId = temp.getProcessInstanceId();
            LambdaQueryWrapper<Process>  wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Process::getProcessInstanceId, processInstanceId);
            //调方法查询
            Process process = baseMapper.selectOne(wrapper);
            //把process->processVo
            ProcessVo processVo = new ProcessVo();
            BeanUtils.copyProperties(process, processVo);
            //放到list 集合
            processVoList.add(processVo);
        }
        //IPage封装分页查询的数据
        IPage<ProcessVo> page= new Page<ProcessVo>(pageParam.getCurrent(), pageParam.getSize(), total);
        page.setRecords(processVoList);
        return page;
    }

    @Override
    public IPage<ProcessVo> findStarted(Page<ProcessVo> pageParam) {
        ProcessQueryVo processQueryVo = new ProcessQueryVo();
        processQueryVo.setUserId(LoginUserInfoHelper.getUserId());
        IPage<ProcessVo> pageModel=baseMapper.selectByPage(pageParam, processQueryVo);
        return pageModel;
    }

    //驳回（结束流程）
    private void endTask(String taskId) {
        //  当前任务
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        List<EndEvent> endEventList = bpmnModel.getMainProcess().findFlowElementsOfType(EndEvent.class);
        // 并行任务可能为null
        if(CollectionUtils.isEmpty(endEventList)) {
            return;
        }
        FlowNode endFlowNode = (FlowNode) endEventList.get(0);
        FlowNode currentFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(task.getTaskDefinitionKey());

        //  临时保存当前活动的原始方向
        List originalSequenceFlowList = new ArrayList<>();
        originalSequenceFlowList.addAll(currentFlowNode.getOutgoingFlows());
        //  清理活动方向
        currentFlowNode.getOutgoingFlows().clear();

        //  建立新方向
        SequenceFlow newSequenceFlow = new SequenceFlow();
        newSequenceFlow.setId("newSequenceFlowId");
        newSequenceFlow.setSourceFlowElement(currentFlowNode);
        newSequenceFlow.setTargetFlowElement(endFlowNode);
        List newSequenceFlowList = new ArrayList<>();
        newSequenceFlowList.add(newSequenceFlow);
        //  当前节点指向新的方向
        currentFlowNode.setOutgoingFlows(newSequenceFlowList);

        //  完成当前任务
        taskService.complete(task.getId());
    }

    //当前任务列表
    private List<Task> getCurrentTaskList(String id) {
        List<org.activiti.engine.task.Task> taskList = taskService.createTaskQuery().processInstanceId(id).list();
        return taskList;
    }
}
