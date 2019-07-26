package com.demo.cluster.manager.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class ByteBufToBytes {
    private ByteBuf temp;
    private boolean end = true;
    public ByteBufToBytes(int length) {
        temp = Unpooled.buffer(length);
    }
    public void reading(ByteBuf datas) {
        datas.readBytes(temp, datas.readableBytes());
        if (this.temp.writableBytes() != 0) {
            end = false;
        } else {
            end = true;
        }
    }
    public boolean isEnd() {
        return end;
    }
    public byte[] readFull() {
        if (end) {
            byte[] contentByte = new byte[this.temp.readableBytes()];
            this.temp.readBytes(contentByte);
            this.temp.release();
            return contentByte;
        } else {
            return null;
        }
    }
    public byte[] read(ByteBuf datas) {
        byte[] bytes = new byte[datas.readableBytes()];
        datas.readBytes(bytes);
        return bytes;
    }

    public static void main(String[] args) {
        ByteBuf buf = Unpooled.buffer(40);
        for (int i = 0; i < 10; i ++) {
            System.out.println("readIndex is " + buf.readerIndex() + "writeIndex is " + buf.writerIndex());
            buf.writeBytes("hello".getBytes());
        }
        byte[] buffer;
        while (buf.isReadable()) {
            System.out.println("readIndex is " + buf.readerIndex() + " writeIndex is " + buf.writerIndex());
            buffer = new byte[10];
            ByteBuf buf1 = buf.readBytes(buffer);
            System.out.println("buffer is " + new String(buffer));

        }



    }
}