package com.yhl.see.server.command;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by yanghailong on 2018/9/4.
 */
@AllArgsConstructor
@Getter
public enum RemoteEnum {

    注册(1),
    日志(2),
    计数器(3),
    代码增强(4),
    查询类树(5);

    private int type;

}
