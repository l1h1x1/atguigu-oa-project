package com.atguigu.process.service;

import com.atguigu.model.process.ProcessType;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 审批类型 服务类
 * </p>
 *
 * @author aXian
 * @since 2023-09-12
 */
public interface ProcessTypeService extends IService<ProcessType> {

    List<ProcessType> findProcessType();
}
