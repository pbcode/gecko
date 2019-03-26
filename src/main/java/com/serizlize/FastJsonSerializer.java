package com.serizlize;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.protocol.PbRpcResponse;
import lombok.extern.slf4j.Slf4j;


/**
 * Demo class
 *
 * @author pengbo3
 */
@Slf4j
public class FastJsonSerializer implements Serializer {
    @Override
    public byte[] serialize(Object o) {
        return JSON.toJSONBytes(o, SerializerFeature.SortField);
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> tClass) throws ClassNotFoundException {
        if (tClass != PbRpcResponse.class) {
            return JSON.parseObject(bytes, tClass, Feature.SortFeidFastMatch);
        }
        PbRpcResponse pbRpcResponse = JSON.parseObject(bytes, tClass, Feature.SortFeidFastMatch);
        Object o = pbRpcResponse.getType();
        Class type = Class.forName(o.toString());
        return (T) PbRpcResponse.parseToMap(bytes, type);
    }
}
