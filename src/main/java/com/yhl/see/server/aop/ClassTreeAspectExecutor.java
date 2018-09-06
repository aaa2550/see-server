package com.yhl.see.server.aop;

import com.yhl.see.server.command.RemoteCommand;
import com.yhl.see.server.files.FileNode;
import com.yhl.see.server.files.PackageUtil;
import io.netty.channel.ChannelHandlerContext;
import javassist.CtClass;
import javassist.NotFoundException;

import java.util.List;

/**
 * Created by yanghailong on 2018/9/4.
 */
public class ClassTreeAspectExecutor extends AspectExecutor {

    @Override
    public void execute(ChannelHandlerContext ctx, RemoteCommand command) {
        try {
            //TODO 执行逻辑z
            CtClass clazz = CLASS_POOL.getCtClass(command.getClassName());
            clazz.getDeclaredMethod(command.getMethodName());
            List<FileNode> fileNodes = PackageUtil.getClassName(command.getPackageName());
            RemoteCommand response = new RemoteCommand(~command.getType());
            response.setFileNodes(fileNodes);
            ctx.channel().writeAndFlush(response);
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
    }
}
