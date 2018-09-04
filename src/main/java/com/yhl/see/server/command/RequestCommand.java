package com.yhl.see.server.command;

import lombok.Data;

/**
 * Created by yanghailong on 2018/9/4.
 */
@Data
public class RequestCommand {

    private RequestCommandEnum commandType;
    private String className;
    private String methodName;

    public RequestCommand(RequestCommandEnum commandType) {
        this.commandType = commandType;
    }

}
