package com.yhl.see.server.aop;

import com.yhl.see.server.command.RemoteCommand;
import io.netty.channel.ChannelHandlerContext;
import javassist.ClassPool;

/**
 * Created by yanghailong on 2018/9/4.
 */
public abstract class AspectExecutor {

    public static final ClassPool CLASS_POOL = ClassPool.getDefault();

    public abstract void execute(ChannelHandlerContext ctx, RemoteCommand command);

}
