package com.yhl.see.server.seriallizer;

import com.yhl.see.server.exception.NettySerializationException;

import java.io.*;

/**
 * Created by yanghailong on 2018/9/4.
 */
public class JdkNettySerializer implements NettySerializer {

    @Override
    public byte[] serialize(Object o) throws NettySerializationException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream serializer = new ObjectOutputStream(new ByteArrayOutputStream())) {
            serializer.writeObject(o);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new NettySerializationException(e.getMessage());
        }
    }

    @Override
    public Object deserialize(byte[] bytes, Class clz) throws NettySerializationException {
        try (ObjectInputStream serializer = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return serializer.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new NettySerializationException(e.getMessage());
        }
    }
}
