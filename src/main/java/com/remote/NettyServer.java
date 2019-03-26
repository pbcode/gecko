package com.remote;

import com.protocol.PbRpcDecoder;
import com.protocol.PbRpcEncoder;
import com.protocol.PbRpcRequest;
import com.protocol.PbRpcResponse;
import com.registry.RegistryUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.UnknownHostException;
import java.util.Map;


/**
 * Demo class
 *
 * @author pengbo3
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
}
