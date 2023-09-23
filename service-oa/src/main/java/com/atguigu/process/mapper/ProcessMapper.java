package com.atguigu.process.mapper;

import com.atguigu.vo.process.ProcessQueryVo;
import com.atguigu.vo.process.ProcessVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.atguigu.model.process.Process;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 审批类型 Mapper 接口
 * </p>
 *
 * @author aXian
 * @since 2023-09-12
 */
public interface ProcessMapper extends BaseMapper<Process> {


    IPage<ProcessVo> selectByPage(Page<ProcessVo> pageParam,@Param("vo") ProcessQueryVo processQueryVo);
}
