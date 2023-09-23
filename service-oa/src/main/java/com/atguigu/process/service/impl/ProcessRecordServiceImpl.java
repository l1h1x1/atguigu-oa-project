package com.atguigu.process.service.impl;

import com.atguigu.auth.service.SysUserService;
import com.atguigu.common.result.LoginUserInfoHelper;
import com.atguigu.model.process.ProcessRecord;
import com.atguigu.model.system.SysUser;
import com.atguigu.process.mapper.ProcessRecordMapper;
import com.atguigu.process.service.ProcessRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 审批记录 服务实现类
 * </p>
 *
 * @author aXian
 * @since 2023-09-14
 */
@Service
public class ProcessRecordServiceImpl extends ServiceImpl<ProcessRecordMapper, ProcessRecord> implements ProcessRecordService {
    @Autowired
    private SysUserService sysUserService;

    @Override
    public void record(Long processId, Integer status, String description) {
        Long userId = LoginUserInfoHelper.getUserId();
        SysUser sysUser = sysUserService.getById(userId);
        ProcessRecord processRecord = new ProcessRecord();
        processRecord.setProcessId(processId);
        processRecord.setStatus(status);
        processRecord.setDescription(description);
        processRecord.setOperateUser(sysUser.getName());
        processRecord.setOperateUserId(userId);
        baseMapper.insert(processRecord);
    }
}
