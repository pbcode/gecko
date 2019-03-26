package com.protocol;

import com.serizlize.Serializer;
import com.serizlize.SerializerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;


/**
 * Demo class
 *
 * @author pengbo3
 */
public class PbRpcDecoder extends ByteToMessageDecoder {

    private Class<?> clazz;

    public PbRpcDecoder(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        if (byteBuf.readableBytes() < 4) {
            return;
        }
        Serializer serializer = SerializerFactory.load();
        int length = byteBuf.readInt();
        int beginIndex = byteBuf.readerIndex();

        if (byteBuf.readableBytes() < length) {
            byteBuf.readerIndex(beginIndex);
            return;
        }

        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes);
        Object o = serializer.deserialize(bytes, clazz);
        list.add(o);
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }
}
