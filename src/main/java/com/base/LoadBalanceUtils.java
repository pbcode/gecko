package com.base;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Random;

public class LoadBalanceUtils {

    private Random random = new Random();

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

    public static void main(String[] args) {
        LoadBalanceUtils loadBalance = new LoadBalanceUtils();
        List<Invoker> invokers = Lists.newArrayList();
        for (int i = 0; i < 6; i++) {
            Invoker invoker = new Invoker();
            invoker.setUrl("127.0.0." + i);
            invoker.setWeight(i);
            invokers.add(invoker);
        }
        System.out.println(loadBalance.randomBalance(invokers));
    }
}
