package com.base;

/**
 * Demo class
 *
 * @author pengbo3
 * @date 2019/2/12 14:50
 */
public interface Serializer {

    byte[] serialize(Object o);

    <T> T deserialize(byte[] bytes, Class<T> tClass) throws ClassNotFoundException;

}
