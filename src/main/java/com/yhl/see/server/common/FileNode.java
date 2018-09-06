package com.yhl.see.server.common;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Field;

/**
 * Created by yanghailong on 2018/9/6.
 */
@Data
public class FileNode {

    private FileNodeEnum fileNodeEnum;
    private String allName;
    private String name;
    private boolean isClass;

    public FileNode(FileNodeEnum fileNodeEnum, String allName, String name, boolean isClass) {
        this.fileNodeEnum = fileNodeEnum;
        this.allName = allName;
        this.isClass = isClass;
        this.name = name;
    }

}
