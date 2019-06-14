package com.kcirqueapps.chatapp.network.model;

import com.google.gson.annotations.SerializedName;

public class HttpResponse<T> {
    @SerializedName("status")
    private int status;
    @SerializedName("error")
    private boolean error;
    @SerializedName("message")
    private String message;
    @SerializedName("response")
    private T response;

    public HttpResponse(int status, boolean error, String message, T response) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.response = response;
    }

    public int getStatus() {
        return status;
    }

    public boolean isError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public T getResponse() {
        return response;
    }
}
