package com.test;

import com.google.common.collect.Lists;
import com.support.PbRpcService;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * Demo class
 *
 * @author pengbo3
 */
@PbRpcService(serviceName = "helloRpcService", value = IHelloService.class)
@Service
public class HelloServiceImpl implements IHelloService {

    @Override
    public List<MovieInfo> sayHello() {
        System.out.println("hello");
        List<MovieInfo> list = Lists.newArrayList();
        MovieInfo movieInfo = new MovieInfo();
        movieInfo.setMovieName("阿丽塔");
        MovieInfo movieInfo2 = new MovieInfo();
        movieInfo2.setMovieName("妖猫传");
        list.add(movieInfo);
        list.add(movieInfo2);
        return list;
    }

    @Override
    public void returnHello() {
        System.out.println("returnHello");
    }
}
