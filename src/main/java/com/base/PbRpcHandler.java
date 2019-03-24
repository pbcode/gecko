package com.base;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;


/**
 * Demo class
 *
 * @author pengbo3
 */
@Slf4j
public class PbRpcHandler extends SimpleChannelInboundHandler<PbRpcResponse> {

    private PbRpcResponse pbRpcResponse;

    public PbRpcHandler(PbRpcResponse pbRpcResponse) {
        this.pbRpcResponse = pbRpcResponse;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, PbRpcResponse pbRpcResponse) {
        this.pbRpcResponse.setRequestId(pbRpcResponse.getRequestId());
        this.pbRpcResponse.setException(pbRpcResponse.getException());
        this.pbRpcResponse.setResult(pbRpcResponse.getResult());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    public PbRpcResponse  getPbRpcResponse() {
        return pbRpcResponse;
    }
}
