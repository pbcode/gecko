package com.base;

import com.alibaba.fastjson.JSON;
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
import java.util.UUID;
import java.util.concurrent.CountDownLatch;


/**
 * Demo class
 *
 * @author pengbo3
 */
@Slf4j
public class JdkProxy<T> implements InvocationHandler {

    private List<String> providers;

    private Random random = new Random(1);

    /**
     * 被代理对象
     */
    private Object target;

    private Class<T> tClass;

    public T newProxy(Class<T> tClass) {
        this.tClass = tClass;
        if (target != null) {
            return (T) target;
        }
        target = Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{tClass}, this);
        return (T) target;
    }

    public JdkProxy(List<String> providers) {
        this.providers = providers;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws InterruptedException {
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

        String url = providers.get(random.nextInt(providers.size()));
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

    public static void main(String[] args) {
        MovieInfo movieInfo = new MovieInfo();
        movieInfo.setMovieName("阿丽塔");
        String json = JSON.toJSONString(movieInfo);
        System.out.println(json);

        MovieInfo movieInfo1 = JSON.parseObject(json,MovieInfo.class);
        System.out.println(movieInfo1.getMovieName());
    }
}
