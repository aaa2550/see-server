package com.yhl.see.server.aop;

import com.yhl.see.server.command.RequestCommand;
import javassist.ClassPool;
import lombok.AllArgsConstructor;

/**
 * Created by yanghailong on 2018/9/4.
 */
@AllArgsConstructor
public abstract class AspectExecutor {

    public static final ClassPool CLASS_POOL = ClassPool.getDefault();
    protected RequestCommand command;

    public abstract void execute();

}
