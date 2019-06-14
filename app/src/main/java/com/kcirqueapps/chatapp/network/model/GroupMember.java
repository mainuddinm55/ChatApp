package com.kcirqueapps.chatapp.network.model;

import com.google.gson.annotations.SerializedName;

public class GroupMember {
    @SerializedName("id")
    private int id;
    @SerializedName("group_id")
    private int groupId;
    @SerializedName("member_id")
    private int memberId;
    @SerializedName("add_time")
    private String addedTime;
    @SerializedName("add_by")
    private int addedBy;

    public GroupMember(int id, int groupId, int memberId, String addedTime, int addedBy) {
        this.id = id;
        this.groupId = groupId;
        this.memberId = memberId;
        this.addedTime = addedTime;
        this.addedBy = addedBy;
    }

    public int getId() {
        return id;
    }

    public int getGroupId() {
        return groupId;
    }

    public int getMemberId() {
        return memberId;
    }

    public String getAddedTime() {
        return addedTime;
    }

    public int getAddedBy() {
        return addedBy;
    }
}
