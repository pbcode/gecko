package com.test;

import com.support.PbService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Demo class
 *
 * @author pengbo3
 * @date 2019/1/23 10:42
 */
@RestController
@RequestMapping("/")
@Slf4j
public class HelloController {
    @PbService(name = "helloRpcService")
    private IHelloService helloRpcService;

    @PbService(name = "hiRpcService")
    private IHiService hiService;

    @RequestMapping("test")
    public void test() {
        System.out.println(hiService.sayHi());
        System.out.println(helloRpcService.sayHello());
    }


}
