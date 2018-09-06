package com.yhl.see.server.command;

import com.yhl.see.server.files.FileNode;
import lombok.Data;

import java.util.List;

/**
 * Created by yanghailong on 2018/9/4.
 */
@Data
public class RemoteCommand {

    private int type;
    private String className;
    private String methodName;
    private String packageName;
    private List<FileNode> fileNodes;

    public RemoteCommand(int type) {
        this.type = type;
    }

}
