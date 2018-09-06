package com.yhl.see.server.aop;

import com.yhl.see.server.command.RequestCommand;
import com.yhl.see.server.exception.InvalidRequestTypeException;

/**
 * Created by yanghailong on 2018/9/4.
 */
public class AspectExecutorFactory {

    public static final int REGISTER_ACTION_CODE = 0;
    public static final int LONG_ACTION_CODE = 1;
    public static final int TIME_ACTION_CODE = 2;
    public static final int ENHANCER_ACTION_CODE = 3;
    public static final int CLASS_TREE_ACTION_CODE = 4;

    public AspectExecutor create(RequestCommand command) {
        switch (command.getType()) {
            case 1:
                return new LogAspectExecutor();
            case 2:
                return new TimeAspectExecutor();
            case 3:
                return new EnhancerAspectExecutor();
            case 4:
                return new ClassTreeAspectExecutor();
            default:
                throw new InvalidRequestTypeException(String.format("this request type cannot be true. type is %s", command.getType()));
        }
    }

}
