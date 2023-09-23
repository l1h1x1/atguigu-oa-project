package com.atguigu.common.result;

import lombok.Getter;

/**
 * @author AXiang
 * @create 2023/9/1 16:48
 **/
@Getter
public enum ResultCodeEnum {
    SUCCESS(200,"成功"),
    FAIL(201, "失败"),
    SERVICE_ERROR(2012, "服务异常"),
    DATA_ERROR(204, "数据异常"),

    PERMISSION(209, "没有权限"),
    LOGIN_ERROR(208,"认证失败")
    ;

    private Integer code;

    private String message;

    private ResultCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
