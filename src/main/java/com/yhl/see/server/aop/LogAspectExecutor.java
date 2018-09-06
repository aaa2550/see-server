package com.yhl.see.server.aop;

import com.yhl.see.server.command.RequestCommand;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

/**
 * Created by yanghailong on 2018/9/4.
 */
public class LogAspectExecutor extends AspectExecutor {

    @Override
    public void execute(RequestCommand command) {
        try {
            //TODO 执行逻辑
            CtClass clazz = CLASS_POOL.getCtClass(command.getClassName());
            clazz.getDeclaredMethod(command.getMethodName());
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
    }
}
