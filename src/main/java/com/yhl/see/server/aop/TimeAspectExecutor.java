package com.yhl.see.server.aop;

import com.yhl.see.server.command.RequestCommand;
import javassist.CtClass;
import javassist.NotFoundException;

/**
 * Created by yanghailong on 2018/9/4.
 */
public class TimeAspectExecutor extends AspectExecutor {

    public TimeAspectExecutor(RequestCommand command) {
        super(command);
    }

    @Override
    public void execute() {
        try {
            //TODO 执行逻辑
            CtClass clazz = CLASS_POOL.getCtClass(command.getClassName());
            clazz.getDeclaredMethod(command.getMethodName());
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
    }
}
