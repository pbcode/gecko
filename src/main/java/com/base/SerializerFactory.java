package com.base;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Demo class
 *
 * @author pengbo3
 * @date 2019/2/12 14:46
 */
public class SerializerFactory {


    public static Serializer load() {
        ServiceLoader<Serializer> serviceLoader = ServiceLoader.load(Serializer.class);
        Iterator<Serializer> iterator = serviceLoader.iterator();
        while (iterator.hasNext()){
            return iterator.next();
        }
        return new FastJsonSerializer();
    }
}
