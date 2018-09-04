package com.yhl.see.server.exception;

/**
 * 序列化异常
 */
public class NettySerializationException extends RuntimeException {

    public NettySerializationException() {
        super();
    }

    public NettySerializationException(String message) {
        super(message);
    }

    public NettySerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
