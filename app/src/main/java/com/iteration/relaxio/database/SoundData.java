package com.iteration.relaxio.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

@Entity
public class SoundData implements Serializable {

    @PrimaryKey
    @ColumnInfo(name = "s_id")
    private int s_id;

    @ColumnInfo(name = "s_img")
    private String s_img;

    @ColumnInfo(name = "s_color")
    private String s_color;

    @ColumnInfo(name = "s_sound")
    private String s_sound;

    @ColumnInfo(name = "local_url")
    private String local_url;

    @ColumnInfo(name = "status")
    private String status;



    public int getS_id() {
        return s_id;
    }

    public void setS_id(int s_id) {
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

    public String getLocal_url() {
        return local_url;
    }

    public void setLocal_url(String local_url) {
        this.local_url = local_url;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
