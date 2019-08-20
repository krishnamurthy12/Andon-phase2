package com.vvtech.andonphase2.apiresponses.general;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class InteractionResponse {

    @SerializedName("message")
    @Expose
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
