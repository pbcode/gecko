package com.test;

import com.support.PbRpcService;
import org.springframework.stereotype.Service;

/**
 * Demo class
 *
 * @author pengbo3
 * @date 2019/1/23 10:42
 */
@PbRpcService(serviceName = "hiRpcService", value = IHiService.class)
@Service
public class HiServiceImpl implements IHiService {

    @Override
    public String sayHi() {
        System.out.println("hi");
        return "HIHIHIHI";
    }

    @Override
    public void returnHi() {
        System.out.println("returnHi");
    }
}
