package com.kcirqueapps.chatapp.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Chat implements Serializable {

    @SerializedName("receiver_id")
    private int receiverId;
    @SerializedName("conversion_type")
    private String conversionType;

    public Chat(int receiverId, String conversionType) {
        this.receiverId = receiverId;
        this.conversionType = conversionType;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public String getConversionType() {
        return conversionType;
    }
}
