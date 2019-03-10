package com.base;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.support.PbRpcService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.zookeeper.CreateMode.EPHEMERAL;
import static org.apache.zookeeper.CreateMode.PERSISTENT;

/**
 * Demo class
 *
 * @author pengbo3
 * @date 2019/1/23 18:31
 */
@Component
public class ServiceBean implements InitializingBean, ApplicationContextAware {

    private ApplicationContext applicationContext;

    private Map<String, Object> serviceClassMap = new HashMap<>();

    private Set<String> serviceList = Sets.newHashSet();

    private ZKUtil zkUtil = new ZKUtil();

    private String localIp;

    private Integer localPort;

    @Value("${zookeeper.address}")
    private String zkAddress;
    private volatile HttpServer httpServer;
    @Override
    public void afterPropertiesSet() throws UnknownHostException, InterruptedException {

        Map<String, Object> map = applicationContext.getBeansWithAnnotation(PbRpcService.class);
        for (Object bean : map.values()) {
            String serviceName = bean.getClass().getAnnotation(PbRpcService.class).serviceName();
            String interFaceName = bean.getClass().getAnnotation(PbRpcService.class).value().getName();
            serviceClassMap.put(interFaceName, bean);
            serviceList.add(serviceName);
        }
        httpServerStart();
        export();
    }

    private void httpServerStart() throws UnknownHostException, InterruptedException {
        httpServer = new NettyServer(serviceClassMap);
        httpServer.start();

    }

    private void export() throws UnknownHostException {
        this.localIp = RegistryUtil.getLocalIp();
        this.localPort = RegistryUtil.getPort();
        register();

    }


    /**
     * 服务的路径
     * /pbrpc
     * /service名称
     * /provider           /consumer
     * /ip：port            /ip：port
     * /ip：port            /ip：port
     * /ip：port            /ip：port
     *
     * @throws UnknownHostException
     */
    private void register() {
        if (!zkUtil.isOpen()) {
            zkUtil.startZk(zkAddress);
        }
        CuratorFramework client = zkUtil.getClient();
        try {
            if (client.checkExists().forPath("/pbrpc") == null) {
                client.create().forPath("/pbrpc");
            }
            for (String serviceName : serviceList) {
                if (client.checkExists().forPath("/pbrpc" + "/" + serviceName) == null) {
                    client.create().withMode(PERSISTENT).forPath("/pbrpc/" + serviceName);
                }
                if (client.checkExists().forPath("/pbrpc" + "/" + serviceName + "/" + "provider") == null) {
                    client.create().withMode(PERSISTENT).forPath("/pbrpc/" + serviceName + "/" + "provider");
                }
                if (client.checkExists().forPath("/pbrpc/" + serviceName + "/provider/" + Joiner.on(":").join(RegistryUtil.getLocalIp(), RegistryUtil.getPort())) == null) {
                    client.create().withMode(EPHEMERAL).forPath("/pbrpc/" + serviceName + "/provider/" + Joiner.on(":").join(RegistryUtil.getLocalIp(), RegistryUtil.getPort()));
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        Map<String, Object> map = applicationContext.getBeansWithAnnotation(PbRpcService.class);
        System.out.println(map);
    }


    private ChannelHandler createChannelInitializer() {
        return new ChannelInitializer<SocketChannel>() {
            // 为accept
            // channel的pipeline预添加的inboundhandler
            // 当新连接accept的时候，这个方法会调用
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline channelPipeline = ch.pipeline();
                channelPipeline.addLast(new PbRpcEncoder(PbRpcResponse.class));
                channelPipeline.addLast(new PbRpcDecoder(PbRpcRequest.class));
                channelPipeline.addLast(new PbRpcServer(serviceClassMap));
            }
        };
    }
}
