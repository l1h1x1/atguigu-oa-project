package com.atguigu.process.service.impl;

import com.atguigu.model.process.ProcessTemplate;
import com.atguigu.model.process.ProcessType;
import com.atguigu.process.mapper.ProcessTemplateMapper;
import com.atguigu.process.service.ProcessService;
import com.atguigu.process.service.ProcessTemplateService;
import com.atguigu.process.service.ProcessTypeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * <p>
 * 审批模板 服务实现类
 * </p>
 *
 * @author aXian
 * @since 2023-09-12
 */
@Service
public class ProcessTemplateServiceImpl extends ServiceImpl<ProcessTemplateMapper, ProcessTemplate> implements ProcessTemplateService {
    @Autowired
    private ProcessTypeService processTypeService;
    @Autowired
    private ProcessService processService;

    @Override
    public IPage<ProcessTemplate> selectPageProcessTemplate(Page<ProcessTemplate> pageParam) {
         //调用mapper实现分页查询
        Page<ProcessTemplate> processTemplatePage = baseMapper.selectPage(pageParam, null);
        //把得到的分页数据 弄成list集合
        List<ProcessTemplate> processTemplateList = processTemplatePage.getRecords();
        //遍历list的集合 得到审批类型id
        for (ProcessTemplate processTemplate:processTemplateList) {
//            得到审批类型id
            Long processTypeId = processTemplate.getProcessTypeId();
//            根据id查询审批类型的名称
            LambdaQueryWrapper<ProcessType> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ProcessType::getId, processTypeId);
            ProcessType processType = processTypeService.getOne(wrapper);
            if(processType==null){
                continue;
            }
            //完成最终封装processTypeName
            processTemplate.setProcessTypeName(processType.getName());

        }
        return processTemplatePage;
    }

    @Override
    public void publish(Long id) {
        //修改模版发布状态  1表示已发布

        ProcessTemplate processTemplate = baseMapper.selectById(id);
        processTemplate.setStatus(1);
        baseMapper.updateById(processTemplate);
        // 后续完善 流程定义部署
        if(!StringUtils.isEmpty(processTemplate.getProcessDefinitionPath())){
            processService.deployByZip(processTemplate.getProcessDefinitionPath());
        }
    }
}
