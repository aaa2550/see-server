package com.yhl.see.server.aop;

import com.yhl.see.server.command.RequestCommand;
import com.yhl.see.server.exception.InvalidRequestTypeException;

/**
 * Created by yanghailong on 2018/9/4.
 */
public class AspectExecutorFactory {

    public AspectExecutor create(RequestCommand command) {
        switch (command.getType()) {
            case 1:
                return new LogAspectExecutor(command);
            case 2:
                return new TimeAspectExecutor(command);
            case 4:
                return new EnhancerAspectExecutor(command);
            default:
                throw new InvalidRequestTypeException(String.format("this request type cannot be true. type is %s", command.getType()));
        }
    }

}
