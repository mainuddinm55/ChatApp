package com.kcirqueapps.chatapp.network.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Group implements Serializable {
    @SerializedName("id")
    private int id;
    @SerializedName("name")
    private String name;
    @SerializedName("privacy")
    private String privacy;
    @SerializedName("create_date")
    private String createDate;
    @SerializedName("creator_id")
    private int creatorId;

    public Group(int id, String name, String privacy, String createDate, int creatorId) {
        this.id = id;
        this.name = name;
        this.privacy = privacy;
        this.createDate = createDate;
        this.creatorId = creatorId;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPrivacy() {
        return privacy;
    }

    public String getCreateDate() {
        return createDate;
    }

    public int getCreatorId() {
        return creatorId;
    }
}
