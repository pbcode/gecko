package com.remote;

import com.protocol.PbRpcRequest;
import com.protocol.PbRpcResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;


/**
 * Demo class
 *
 * @author pengbo3
 */
@Slf4j
public class PbRpcServer extends SimpleChannelInboundHandler<PbRpcRequest> {

    private Map<String, Object> serviceMap;

    public PbRpcServer(Map<String, Object> serviceMap) {
        this.serviceMap = serviceMap;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, PbRpcRequest pbRpcRequest) {
        PbRpcResponse pbRpcResponse = new PbRpcResponse();
        pbRpcResponse.setRequestId(pbRpcRequest.getRequestId());
        try {
            Object result = handle(pbRpcRequest);
            pbRpcResponse.setType(result.getClass());
            pbRpcResponse.setResult(result);
        } catch (Exception e) {
            pbRpcResponse.setException(e);
        }
        ctx.writeAndFlush(pbRpcResponse).addListener(ChannelFutureListener.CLOSE);
    }

    private Object handle(PbRpcRequest pbRpcRequest) throws Exception {
        Object beanClass = serviceMap.get(pbRpcRequest.getClassName());
        if (Objects.isNull(beanClass)) {
            throw new Exception();
        }
        Method[] methods = beanClass.getClass().getDeclaredMethods();
        return invokeMethod(beanClass, methods, pbRpcRequest);
    }

    private Object invokeMethod(Object beanClass, Method[] methods, PbRpcRequest pbRpcRequest) throws Exception {
        String methodName = pbRpcRequest.getMethodName();
        if (Objects.isNull(methods) || methods.length == 0) {
            throw new Exception();
        }
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                return method.invoke(beanClass, pbRpcRequest.getParams());
            }
        }
        return null;
    }
}
