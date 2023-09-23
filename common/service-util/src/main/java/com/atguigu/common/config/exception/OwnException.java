package com.atguigu.common.config.exception;

import com.atguigu.common.result.ResultCodeEnum;
import lombok.Data;

/**
 * @author AXiang
 * @create 2023/9/2 14:20
 **/
@Data
//自定义异常类
public class OwnException extends RuntimeException{
    private Integer code;

    private String message;

    /**
     * 通过状态码和错误消息创建异常对象
     * @param code
     * @param message
     */
    public OwnException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * 接收枚举类型对象
     * @param resultCodeEnum
     */
    public OwnException(ResultCodeEnum resultCodeEnum) {
        super(resultCodeEnum.getMessage());
        this.code = resultCodeEnum.getCode();
        this.message = resultCodeEnum.getMessage();
    }

    @Override
    public String toString() {
        return "OwnException{" +
                "code=" + code +
                ", message=" + this.getMessage() +
                '}';
    }
}
