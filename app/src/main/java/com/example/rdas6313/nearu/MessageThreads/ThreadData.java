package com.example.rdas6313.nearu.MessageThreads;

public class ThreadData {
    private String msg,receiver_id,sender_id,receiver_name,key;
    private long timestamp;
    private boolean isMsgSeen;

    public ThreadData(String key,String msg,String receiverId,String senderId,String receiverName,long timestamp,boolean msgSeen){
        this.msg = msg;
        this.receiver_id = receiverId;
        this.sender_id = senderId;
        this.receiver_name = receiverName;
        this.timestamp = timestamp;
        this.key = key;
        this.isMsgSeen = msgSeen;
    }

    public boolean isMsgSeen(){
        return isMsgSeen;
    }

    public String getKey() {
        return key;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getMsg() {
        return msg;
    }

    public String getReceiverId() {
        return receiver_id;
    }

    public String getReceiverName() {
        return receiver_name;
    }

    public String getSenderId() {
        return sender_id;
    }
}
