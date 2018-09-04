package com.yhl.see.server.socket;

import com.yhl.see.server.command.RequestCommand;
import com.yhl.see.server.command.RequestCommandEnum;
import com.yhl.see.server.seriallizer.NettySerializationUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.timeout.IdleStateHandler;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

public class SocketClient {

    private String host;
    private int port;
    private static Channel ch;
    private static final EventLoopGroup group = new NioEventLoopGroup();
    private static volatile Boolean SEND_SWITCH;
    private static int tryTimes;

    public SocketClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @PostConstruct
    public void init() {
        try {
            open();
        } catch (Exception e) {
            temporaryStopSend();
        }
    }

    private void open() throws InterruptedException {
        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("http-codec", new HttpClientCodec());
                        pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
                        pipeline.addLast(new IdleStateHandler(0,0,25, TimeUnit.SECONDS));
                    }
                });

        ch = b.connect(host, port).sync().channel();
        SEND_SWITCH = true;
        //发起注册
        eval(new RequestCommand(RequestCommandEnum.注册));

    }

    @PreDestroy
    public void close() throws InterruptedException {
        ch.writeAndFlush(new CloseWebSocketFrame());
        ch.closeFuture().sync();
        group.shutdownGracefully();
    }

    public void eval(RequestCommand command) {
        if (SEND_SWITCH) {
            if (ch != null && ch.isOpen()) {
                byte[] body = NettySerializationUtils.serializer.serialize(command);
                ch.writeAndFlush(body);
            } else {
                SEND_SWITCH = false;
                try {
                    //重新连接
                    open();
                    tryTimes = 0;
                } catch (Exception e) {
                    temporaryStopSend();
                }
            }
        }
    }

    /**
     * 10秒检测重连
     */
    private void temporaryStopSend() {
        /*SEND_SWITCH = false;
        if (tryTimes++ < 5) {
            log.info("10秒后尝试重新连接web socket,times:{}", tryTimes);
            ThreadUtil.submit(()-> {
                try {
                    Thread.sleep(10000);
                    SEND_SWITCH = true;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }else{
            LocalCacheSwitch.SOCKET_SWITCH = false;
            SEND_SWITCH = true;
            tryTimes = 0;
            MessageSmsUtil.sendMsgWhenException("webSocket 连接失败");
            log.info("尝试多次失败，放弃连接");
        }*/

    }
}