package com.atguigu.process.service.impl;

import com.atguigu.model.process.ProcessTemplate;
import com.atguigu.model.process.ProcessType;
import com.atguigu.process.mapper.ProcessTypeMapper;
import com.atguigu.process.service.ProcessTemplateService;
import com.atguigu.process.service.ProcessTypeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 审批类型 服务实现类
 * </p>
 *
 * @author aXian
 * @since 2023-09-12
 */
@Service
public class ProcessTypeServiceImpl extends ServiceImpl<ProcessTypeMapper, ProcessType> implements ProcessTypeService {
    @Autowired
    private ProcessTemplateService processTemplateService;
    @Override
    public List<ProcessType> findProcessType() {
        //查询所有审批分类 list集合
        List<ProcessType> processTypeList = baseMapper.selectList(null);
        //遍历返回所有审批分类list集合
        for(ProcessType processType:processTypeList){
            Long id = processType.getId();
            LambdaQueryWrapper<ProcessTemplate> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ProcessTemplate::getProcessTypeId, id);
            List<ProcessTemplate> processTemplateList = processTemplateService.list(wrapper);
            processType.setProcessTemplateList(processTemplateList);
        }
        return processTypeList;
    }
}
