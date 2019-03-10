package com.base;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Demo class
 *
 * @author pengbo3
 * @date 2019/1/25 16:55
 */
public class PbRpcEncoder extends MessageToByteEncoder {

    private Class<?> aClass;

    public PbRpcEncoder(Class<?> rpcRequest) {
        this.aClass = rpcRequest;
    }


    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) {
        Serializer serializer = SerializerFactory.load();
        byte[] bytes = serializer.serialize(o);
        int length = bytes.length;
        byteBuf.writeInt(length);
        byteBuf.writeBytes(bytes);
    }
}
