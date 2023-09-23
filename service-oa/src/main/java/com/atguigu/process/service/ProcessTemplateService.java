package com.atguigu.process.service;

import com.atguigu.model.process.ProcessTemplate;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 审批模板 服务类
 * </p>
 *
 * @author aXian
 * @since 2023-09-12
 */
public interface ProcessTemplateService extends IService<ProcessTemplate> {

    IPage<ProcessTemplate> selectPageProcessTemplate(Page<ProcessTemplate> pageParam);

    void publish(Long id);
}
