package com.xinqi.bean;

/**
 * @author XinQi
 */
public class User {
    String chatId;
    String userName;
    String message;
    String reMessage;
    long startTime;
    long endTime;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getStartTime() {
        return startTime;
    }

    @Override
    public String toString() {
        return "User{" +
                "chatId='" + chatId + '\'' +
                ", userName='" + userName + '\'' +
                ", message='" + message + '\'' +
                ", reMessage='" + reMessage + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }

    public String getReMessage() {
        return reMessage;
    }

    public void setReMessage(String reMessage) {
        this.reMessage = reMessage;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public User() {
    }

}
