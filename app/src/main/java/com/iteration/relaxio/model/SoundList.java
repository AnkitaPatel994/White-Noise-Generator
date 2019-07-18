package com.iteration.relaxio.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class SoundList {

    @SerializedName("status")
    private String status;
    @SerializedName("message")
    private String message;
    @SerializedName("Sound")
    private ArrayList<Sound> SoundList;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ArrayList<Sound> getSoundList() {
        return SoundList;
    }

    public void setSoundList(ArrayList<Sound> soundList) {
        SoundList = soundList;
    }
}
