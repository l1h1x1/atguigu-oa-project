package com.atguigu.process.service;

import com.atguigu.model.process.ProcessRecord;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 审批记录 服务类
 * </p>
 *
 * @author aXian
 * @since 2023-09-14
 */
public interface ProcessRecordService extends IService<ProcessRecord> {
    void record(Long processId,Integer status,String description);
}
