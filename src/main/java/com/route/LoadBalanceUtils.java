package com.route;

import com.google.common.collect.Lists;
import com.proxy.Invoker;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class LoadBalanceUtils {

    private Random random = new Random();

    private static ConcurrentHashMap<String, AtomicInteger> concurrentHashMap = new ConcurrentHashMap<>();


    /**
     * 随机权重自己实现
     * 其他组件现成的还有比如https://github.com/grpc/grpc-java/issues/1771
     *
     * @param invokers
     * @return
     */
    public String randomBalance(List<Invoker> invokers) {
        boolean sameWeight = true;
        int totalWeight = 0;
        int size = invokers.size();
        for (int i = 0; i < size; i++) {
            totalWeight = totalWeight + invokers.get(i).getWeight();
            if (i > 0 && invokers.get(i).getWeight() != invokers.get(i - 1).getWeight()) {
                sameWeight = false;
            }
        }
        if (!sameWeight) {
            int offset = random.nextInt(totalWeight);
            System.out.println(offset);//14
            for (int i = 0; i < size; i++) {
                offset = offset - invokers.get(i).getWeight();
                if (offset < 0) {
                    return invokers.get(i).getUrl();
                }
            }
        }
        System.out.println("总权重" + totalWeight);
        return invokers.get(random.nextInt(size)).getUrl();
    }

    public String roundRobinLoadBalance(List<Invoker> invokers, String serviceName) {
        if (concurrentHashMap.get(serviceName) == null) {
            concurrentHashMap.put(serviceName, new AtomicInteger(0));
        }
        AtomicInteger atomicLong = concurrentHashMap.get(serviceName);
        return invokers.get(atomicLong.getAndIncrement() % invokers.size()).getUrl();
    }

    public static void main(String[] args) {
        LoadBalanceUtils loadBalance = new LoadBalanceUtils();
        List<Invoker> invokers = Lists.newArrayList();
//        for (int i = 0; i < 6; i++) {
            Invoker invoker = new Invoker();
            invoker.setUrl("127.0.0." + 1);
            invoker.setWeight(1);
            invokers.add(invoker);
//        }
        for (int i = 0; i < 20; i++) {
            System.out.println(loadBalance.roundRobinLoadBalance(invokers, "hello"));
        }
    }
}
