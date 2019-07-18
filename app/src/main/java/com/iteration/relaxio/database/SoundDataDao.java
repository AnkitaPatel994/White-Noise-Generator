package com.iteration.relaxio.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.iteration.relaxio.utility.Constant;

import java.util.List;

@Dao
public interface SoundDataDao {

    @Query("SELECT * FROM sounddata")
    List<SoundData> getAll();

    @Insert
    void insert(SoundData soundData);

    @Delete
    void delete(SoundData soundData);

    @Update
    void update(SoundData soundData);

    @Query("select * from sounddata where s_id = :Id")
    List<SoundData> getSoundData(int Id);

    @Query("select * from sounddata where status='" + Constant.PENDDIG + "'")
    List<SoundData> getPendingTracks();

    @Query("select * from sounddata where status='" + Constant.COMPLETE + "'")
    List<SoundData> getCompletedTracks();
}