package com.kcirqueapps.chatapp.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Conversion {
    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("receiver_id")
    @Expose
    private int receiverId;
    @SerializedName("sender_id")
    @Expose
    private int senderId;
    @SerializedName("conversion_type")
    @Expose
    private String conversionType;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("media_url")
    @Expose
    private String mediaUrl;
    @SerializedName("file_name")
    @Expose
    private String fileName;
    @SerializedName("media_type")
    @Expose
    private String mediaType;
    @SerializedName("send_time")
    @Expose
    private String sendTime;
    @SerializedName("receiver_seen")
    @Expose
    private int receiverSeen;
    @SerializedName("sender_seen")
    @Expose
    private int senderSeen;
    @SerializedName("seen_time")
    @Expose
    private String seenTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public String getConversionType() {
        return conversionType;
    }

    public void setConversionType(String conversionType) {
        this.conversionType = conversionType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getSendTime() {
        return sendTime;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }

    public int getReceiverSeen() {
        return receiverSeen;
    }

    public void setReceiverSeen(int receiverSeen) {
        this.receiverSeen = receiverSeen;
    }

    public int getSenderSeen() {
        return senderSeen;
    }

    public void setSenderSeen(int senderSeen) {
        this.senderSeen = senderSeen;
    }

    public String getSeenTime() {
        return seenTime;
    }

    public void setSeenTime(String seenTime) {
        this.seenTime = seenTime;
    }

}