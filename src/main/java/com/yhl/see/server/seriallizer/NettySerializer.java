package com.yhl.see.server.seriallizer;


import com.yhl.see.server.exception.NettySerializationException;

/**
 * 对象序列化接口
 */
public interface NettySerializer<T> {

    byte[] serialize(T t) throws NettySerializationException;

    <T> T deserialize(byte[] bytes, Class<T> clz) throws NettySerializationException;
}
