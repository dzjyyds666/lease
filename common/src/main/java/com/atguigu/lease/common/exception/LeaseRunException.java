package com.atguigu.lease.common.exception;

import com.atguigu.lease.common.result.ResultCodeEnum;
import lombok.Data;

@Data
public class LeaseRunException extends RuntimeException {

    Integer code;

    public LeaseRunException(Integer code,String message){
        super(message);
        this.code = code;
    }

    public LeaseRunException(ResultCodeEnum resultCodeEnum){
        super(resultCodeEnum.getMessage());
        this.code = resultCodeEnum.getCode();
    }
}
