package com.test;

import com.base.MovieInfo;

import java.util.List;

/**
 * Demo class
 *
 * @author pengbo3
 * @date 2019/1/23 19:48
 */
public interface IHelloService {
    List<MovieInfo> sayHello();
    void returnHello();
}
