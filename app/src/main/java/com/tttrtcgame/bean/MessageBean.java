package com.tttrtcgame.bean;

/**
 * Created by Administrator on 2017-12-25.
 */

public class MessageBean {

    public static final int MESSAGE_TYPE_TEXT = 1;
    public static final int MESSAGE_TYPE_VOICE = 2;

    public int userId = 0;
    public int messageType = 1;
    public String message = "";
    public int time = 0;
    public boolean recognize = false;

    public MessageBean(String message) {
        this.message = message;
    }

    public MessageBean(int userId, int messageType, String message) {
        this.userId = userId;
        this.messageType = messageType;
        this.message = message;
    }

    public MessageBean(int userId, int messageType, String message, int time) {
        this.userId = userId;
        this.messageType = messageType;
        this.message = message;
        this.time = time;
    }
}
