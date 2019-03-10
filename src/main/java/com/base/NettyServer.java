/**
 * 版权所有@: 杭州铭师堂教育科技发展有限公司
 * 注意：本内容仅限于杭州铭师堂教育科技发展有限公司内部使用，禁止外泄以及用于其他的商业目的
 * CopyRight@: 2018 Hangzhou Mistong Educational Technology Co.,Ltd.
 * All Rights Reserved.
 * Note:Just limited to use by Mistong Educational Technology Co.,Ltd. Others are forbidden.
 */
package com.base;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

import java.net.UnknownHostException;
import java.util.Map;

/**
 * NettyJaxrsServer
 * <p>
 * Created by huapeng.hhp on 2018/5/1.
 */
@Slf4j
public class NettyServer implements HttpServer {
    /**
     * 服务启动器
     */
    protected ServerBootstrap bootstrap = new ServerBootstrap();
    private EventLoopGroup eventLoopGroup;
    private EventLoopGroup eventExecutor;
    protected int port = 63914;
    /**
     * 物理机核数
     */
    private int ioWorkerCount = Runtime.getRuntime().availableProcessors() * 2;
    /**
     * 总线程数
     */
    private int executorThreadCount = 16;
    /**
     * 可连接队列
     */
    private int backlog = 128;

    /**
     * 是否测试长连接的状态
     */
    protected boolean keepAlive = true;
    private Map<String, Object> serviceHandle;

    public NettyServer(Map<String, Object> serviceHandle) {
        this.serviceHandle = serviceHandle;
    }

    @Override
    public void start() throws UnknownHostException {
        // 处理请求IO，建立连接
        eventLoopGroup = new NioEventLoopGroup(ioWorkerCount);
        // 处理执行事件，就是处理业务
        eventExecutor = new NioEventLoopGroup(executorThreadCount);
        bootstrap.group(eventExecutor, eventLoopGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, backlog)
                .childOption(ChannelOption.SO_KEEPALIVE, keepAlive)
                .childHandler(createChannelInitializer());
        bootstrap.bind(RegistryUtil.getLocalIp(), RegistryUtil.getPort()).syncUninterruptibly();
    }

    private ChannelHandler createChannelInitializer() {
        return new ChannelInitializer<SocketChannel>() {
            // 为accept
            // channel的pipeline预添加的inboundhandler
            // 当新连接accept的时候，这个方法会调用
            @Override
            protected void initChannel(SocketChannel ch) {
                ChannelPipeline channelPipeline = ch.pipeline();
                channelPipeline.addLast(new PbRpcEncoder(PbRpcResponse.class));
                channelPipeline.addLast(new PbRpcDecoder(PbRpcRequest.class));
                channelPipeline.addLast(new PbRpcServer(serviceHandle));
            }
        };
    }

    @Override
    public void stop() {
        try {
            eventLoopGroup.shutdownGracefully().sync();
            eventExecutor.shutdownGracefully().sync();
        } catch (Exception ignore) { // NOPMD
        }
    }
    /** ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
}
