package com.yhl.see.server.socket;

import com.yhl.see.server.command.RemoteCommand;
import com.yhl.see.server.seriallizer.NettySerializationUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerDecoderHandler extends LengthFieldBasedFrameDecoder {

    private static final int HEADER_SIZE = 4;

    private static final Logger logger = LoggerFactory.getLogger(ServerDecoderHandler.class);

    public ServerDecoderHandler(int maxFrameLength,
                                int lengthFieldOffset, int lengthFieldLength,
                                int lengthAdjustment, int initialBytesToStrip, boolean failFast) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength,
                lengthAdjustment, initialBytesToStrip, failFast);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = (ByteBuf) super.decode(ctx, in);
        if (frame == null) {
            logger.error("解码数据为空");
            return null;
        }

        if (frame.readableBytes() < HEADER_SIZE) {
            logger.error("可读消息段比头部信息小");
            ctx.close();
            return null;
        }

        int bodyLength = frame.readInt();
        byte[] body = new byte[bodyLength];
        frame.readBytes(body);
        return NettySerializationUtils.serializer.deserialize(body, RemoteCommand.class);
    }

}
