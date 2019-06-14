package com.kcirqueapps.chatapp.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class User implements Serializable {
    @SerializedName("id")
    private int id;
    @SerializedName("first_name")
    private String firstName;
    @SerializedName("last_name")
    private String lastName;
    @SerializedName("email")
    private String email;
    @SerializedName("dob")
    private String dateOfBirth;
    @SerializedName("gender")
    private String gender;
    @SerializedName("mobile")
    private String mobile;
    @SerializedName("is_varified")
    private int isVerified;
    @SerializedName("varification_code")
    private String verificationCode;
    @SerializedName("create_date")
    private String createDate;
    @SerializedName("lat")
    private double lat;
    @SerializedName("lng")
    private double lng;
    @SerializedName("token")
    private String token;
    @SerializedName("status")
    private int status;
    @SerializedName("action_user_id")
    private int actionUserId;

    @SerializedName("photo_url")
    private String photoUrl;

    public User(int id, String firstName, String lastName, String email, String dateOfBirth, String gender, String mobile, int isVerified, String verificationCode, String createDate, double lat, double lng, String token,String photoUrl) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.mobile = mobile;
        this.isVerified = isVerified;
        this.verificationCode = verificationCode;
        this.createDate = createDate;
        this.lat = lat;
        this.lng = lng;
        this.token = token;
        this.photoUrl = photoUrl;
    }

    public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public String getMobile() {
        return mobile;
    }

    public int isVerified() {
        return isVerified;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public String getCreateDate() {
        return createDate;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public String getToken() {
        return token;
    }

    public int getStatus() {
        return status;
    }

    public int getActionUserId() {
        return actionUserId;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setActionUserId(int actionUserId) {
        this.actionUserId = actionUserId;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }
}
