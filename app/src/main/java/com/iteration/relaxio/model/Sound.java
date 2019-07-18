package com.iteration.relaxio.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Sound implements Serializable {

    @SerializedName("s_id")
    private String s_id;
    @SerializedName("s_img")
    private String s_img;
    @SerializedName("s_color")
    private String s_color;
    @SerializedName("s_sound")
    private String s_sound;

    private int finalI = -1;
    private int soundProgress = 100;

    public int getSoundProgress() {
        return soundProgress;
    }

    public void setSoundProgress(int soundProgress) {
        this.soundProgress = soundProgress;
    }

    public int getFinalI() {
        return finalI;
    }

    public void setFinalI(int finalI) {
        this.finalI = finalI;
    }

    public String getS_id() {
        return s_id;
    }

    public void setS_id(String s_id) {
        this.s_id = s_id;
    }

    public String getS_img() {
        return s_img;
    }

    public void setS_img(String s_img) {
        this.s_img = s_img;
    }

    public String getS_color() {
        return s_color;
    }

    public void setS_color(String s_color) {
        this.s_color = s_color;
    }

    public String getS_sound() {
        return s_sound;
    }

    public void setS_sound(String s_sound) {
        this.s_sound = s_sound;
    }
}
