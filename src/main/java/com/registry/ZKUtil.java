package com.registry;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.stereotype.Component;

import java.util.Objects;


/**
 * Demo class
 *
 * @author pengbo3
 */
@Component
public class ZKUtil {

    private static CuratorFramework client;


    public void startZk(String zkAddress) {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.newClient(zkAddress, 10000, 10000, retryPolicy);
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
