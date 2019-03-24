package com.base;


/**
 * Demo class
 *
 * @author pengbo3
 */
public interface Serializer {

    byte[] serialize(Object o);

    <T> T deserialize(byte[] bytes, Class<T> tClass) throws ClassNotFoundException;

}
