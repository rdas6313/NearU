package com.example.rdas6313.nearu.Chat;

public class Message {

    private String reciverId,senderId,msg;
    private long timestamp;

    public Message(String reciverId,String senderId,String msg,long timestamp){
        this.reciverId = reciverId;
        this.senderId = senderId;
        this.msg = msg;
        this.timestamp = timestamp;
    }

    public String getReciverId(){
        return reciverId;
    }

    public String getSenderId(){
        return senderId;
    }

    public String getMsg(){
        return msg;
    }

    public long getTimestamp(){
        return timestamp;
    }
}
