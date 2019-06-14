package com.kcirqueapps.chatapp.network.model;

import com.google.gson.annotations.SerializedName;

public class Friendship {
    @SerializedName("user_one_id")
    private int userId;
    @SerializedName("user_two_id")
    private int friendId;
    @SerializedName("status")
    private int status;
    @SerializedName("action_user_id")
    private int actionUserId;
    @SerializedName("request_send_time")
    private String requestSendTime;
    @SerializedName("action_time")
    private String actionTime;

    public Friendship(int userId, int friendId, int status, int actionUserId, String requestSendTime, String actionTime) {
        this.userId = userId;
        this.friendId = friendId;
        this.status = status;
        this.actionUserId = actionUserId;
        this.requestSendTime = requestSendTime;
        this.actionTime = actionTime;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getFriendId() {
        return friendId;
    }

    public void setFriendId(int friendId) {
        this.friendId = friendId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getActionUserId() {
        return actionUserId;
    }

    public void setActionUserId(int actionUserId) {
        this.actionUserId = actionUserId;
    }

    public String getRequestSendTime() {
        return requestSendTime;
    }

    public void setRequestSendTime(String requestSendTime) {
        this.requestSendTime = requestSendTime;
    }

    public String getActionTime() {
        return actionTime;
    }

    public void setActionTime(String actionTime) {
        this.actionTime = actionTime;
    }
}

