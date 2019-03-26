package com.proxy;

import com.route.LoadBalanceUtils;
import com.remote.PbRpcHandler;
import com.protocol.PbRpcDecoder;
import com.protocol.PbRpcEncoder;
import com.protocol.PbRpcRequest;
import com.protocol.PbRpcResponse;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;


/**
 * Demo class
 *
 * @author pengbo3
 */
@Slf4j
public class JdkProxy<T> implements InvocationHandler {

    private Set<String> providers;

    private Random random = new Random(1);

    /**
     * 被代理对象
     */
    private Object target;

    private Class<T> tClass;

    private String serviceName;

    private int balanceType;

    public T newProxy(Class<T> tClass, String remoteServiceName, int balanceType) {
        this.tClass = tClass;
        this.serviceName = remoteServiceName;
        this.balanceType = balanceType;
        if (target != null) {
            return (T) target;
        }
        target = Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{tClass}, this);
        return (T) target;
    }

    public JdkProxy(Set<String> providers) {
        this.providers = providers;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Exception {
        String methodName = method.getName();
        Class[] params = method.getParameterTypes();

        PbRpcRequest rpcRequest = new PbRpcRequest();
        rpcRequest.setClassName(this.tClass.getName());
        rpcRequest.setMethodName(methodName);
        rpcRequest.setRequestId(UUID.randomUUID().toString());
        rpcRequest.setParams(params);

        PbRpcResponse response = new PbRpcResponse();

        EventLoopGroup workerGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);


        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel channel) throws Exception {
                ChannelPipeline channelPipeline = channel.pipeline();
                channelPipeline.addLast(new PbRpcEncoder(PbRpcRequest.class));
                channelPipeline.addLast(new PbRpcDecoder(PbRpcResponse.class));
                channelPipeline.addLast(new PbRpcHandler(response));
            }
        });

        //模拟负载均衡，暂时按IP最后一位模拟权重
        List<Invoker> invokers = Lists.newArrayList();
        for (String url : providers) {
            Invoker invoker = new Invoker();
            invoker.setUrl(url);
            List<String> ips = Splitter.on(".").splitToList(url.split(":")[0]);
            invoker.setWeight(Integer.parseInt(ips.get(ips.size() - 1)));
            invokers.add(invoker);
        }
        String url = null;
        LoadBalanceUtils loadBalanceUtils = new LoadBalanceUtils();
        if (balanceType == 1) {
            url = loadBalanceUtils.randomBalance(invokers);
        }
        if (balanceType == 2) {
            url = loadBalanceUtils.roundRobinLoadBalance(invokers, serviceName);
        }
        if (url == null || url.isEmpty()) {
            throw new Exception();
        }
        String ip = url.split(":")[0];
        int port = Integer.parseInt(url.split(":")[1]);

        final CountDownLatch completedSignal = new CountDownLatch(1);

        ChannelFuture future = bootstrap.connect(ip, port).sync();
        future.channel().writeAndFlush(rpcRequest).addListener((ChannelFutureListener) channelFuture -> completedSignal.countDown());
        try {
            completedSignal.await();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
        }
        return response.getResult();
    }

}
