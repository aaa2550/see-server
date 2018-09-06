package com.yhl.see.server.command;

import lombok.Data;

/**
 * Created by yanghailong on 2018/9/4.
 */
@Data
public class RequestCommand {

    private int type;
    private String className;
    private String methodName;

    public RequestCommand(int type) {
        this.type = type;
    }

}
