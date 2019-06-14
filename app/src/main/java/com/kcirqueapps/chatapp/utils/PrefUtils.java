package com.kcirqueapps.chatapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.kcirqueapps.chatapp.network.model.User;

public class PrefUtils {
    private SharedPreferences sharedPreferences;

    private static final String PREF_NAME = "chat_pref";
    private static final String KEY_ID = "id";
    private static final String KEY_FIRST_NAME = "first_name";
    private static final String KEY_LAST_NAME = "last_name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_DOB = "dob";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_MOBILE = "mobile";
    private static final String KEY_IS_VERIFIED = "is_verified";
    private static final String KEY_VERIFICATION_CODE = "verification_code";
    private static final String KEY_CREATE_DATE = "create_date";
    private static final String KEY_LAT = "lat";
    private static final String KEY_LNG = "lng";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_PHOTO_URL = "photo";

    public PrefUtils(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void putUser(User user) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_ID, user.getId());
        editor.putString(KEY_FIRST_NAME, user.getFirstName());
        editor.putString(KEY_LAST_NAME, user.getLastName());
        editor.putString(KEY_EMAIL, user.getEmail());
        editor.putString(KEY_DOB, user.getDateOfBirth());
        editor.putString(KEY_GENDER, user.getGender());
        editor.putString(KEY_MOBILE, user.getMobile());
        editor.putInt(KEY_IS_VERIFIED, user.isVerified());
        editor.putString(KEY_VERIFICATION_CODE, user.getVerificationCode());
        editor.putString(KEY_CREATE_DATE, user.getCreateDate());
        editor.putFloat(KEY_LAT, (float) user.getLat());
        editor.putFloat(KEY_LNG, (float) user.getLng());
        editor.putString(KEY_TOKEN, user.getToken());
        editor.putString(KEY_PHOTO_URL, user.getPhotoUrl());
        editor.apply();
    }

    public void clearUser(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_ID, -1);
        editor.putString(KEY_FIRST_NAME, null);
        editor.putString(KEY_LAST_NAME, null);
        editor.putString(KEY_EMAIL, null);
        editor.putString(KEY_DOB, null);
        editor.putString(KEY_GENDER, null);
        editor.putString(KEY_MOBILE, null);
        editor.putInt(KEY_IS_VERIFIED, 0);
        editor.putString(KEY_VERIFICATION_CODE, null);
        editor.putString(KEY_CREATE_DATE, null);
        editor.putFloat(KEY_LAT, 0);
        editor.putFloat(KEY_LNG, 0);
        editor.putString(KEY_TOKEN, null);
        editor.putString(KEY_PHOTO_URL, null);
        editor.apply();
    }

    public User getUser() {
        int id = sharedPreferences.getInt(KEY_ID, -1);
        String firstName = sharedPreferences.getString(KEY_FIRST_NAME, null);
        String lastName = sharedPreferences.getString(KEY_LAST_NAME, null);
        String email = sharedPreferences.getString(KEY_EMAIL, null);
        String dob = sharedPreferences.getString(KEY_DOB, null);
        String gender = sharedPreferences.getString(KEY_GENDER, null);
        String mobile = sharedPreferences.getString(KEY_MOBILE, null);
        int isVerified = sharedPreferences.getInt(KEY_IS_VERIFIED, -1);
        String verificationCode = sharedPreferences.getString(KEY_VERIFICATION_CODE, null);
        String createDate = sharedPreferences.getString(KEY_CREATE_DATE, null);
        double lat = (double) sharedPreferences.getFloat(KEY_LAT, 0);
        double lng = (double)sharedPreferences.getFloat(KEY_LNG, 0);
        String token = sharedPreferences.getString(KEY_TOKEN, null);
        String photo = sharedPreferences.getString(KEY_PHOTO_URL,null);
        if (id != -1) {
            return new User(id, firstName, lastName, email, dob, gender, mobile, isVerified, verificationCode, createDate, lat, lng, token,photo);
        } else {
            return null;
        }
    }
}
