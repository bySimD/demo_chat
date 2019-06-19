package com.chatserver.demo.netty.codec;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Sharable
@Component
public class ByteArrayEncoder extends MessageToMessageEncoder<byte[]> {
    public ByteArrayEncoder() {
    }

    protected void encode(ChannelHandlerContext ctx, byte[] msg, List<Object> out) {
        out.add(Unpooled.wrappedBuffer(msg));
    }
}
