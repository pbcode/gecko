package com.base;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.Feature;
import lombok.Data;

import java.io.Serializable;
import java.lang.reflect.Type;


/**
 * Demo class
 *
 * @author pengbo3
 */
@Data
public class PbRpcResponse<T> implements Serializable {

    private static final long serialVersionUID = 233118992136881673L;
    private String requestId;
    private Exception exception;
    private T result;
    private Object type;

    public static <T> PbRpcResponse parseToMap(byte[] json, Class<T> type) {
        return JSON.parseObject(json,new TypeReference<PbRpcResponse<T>>(type){}.getType(),Feature.SortFeidFastMatch);
    }
}
