package com.base;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Demo class
 *
 * @author pengbo3
 * @date 2019/2/15 9:57
 */
@Component
public class ZKUtil {

    private static CuratorFramework client;


    public void startZk(String zkAddress) {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.newClient(zkAddress, retryPolicy);
        client.start();
    }

    public boolean isOpen() {
        if (Objects.isNull(client)) {
            return false;
        }
        CuratorFrameworkState state = client.getState();
        return state == CuratorFrameworkState.STARTED;
    }

    public CuratorFramework getClient() {
        return client;
    }

}
