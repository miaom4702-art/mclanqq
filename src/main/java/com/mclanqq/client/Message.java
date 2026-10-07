package com.mclanqq.client;

/** 客户端保存的一条聊天记录。 */
public class Message {

    public final boolean mine;
    public final String fromName;
    public final String text;
    public final long time;

    public Message(boolean mine, String fromName, String text, long time) {
        this.mine = mine;
        this.fromName = fromName;
        this.text = text;
        this.time = time;
    }
}
