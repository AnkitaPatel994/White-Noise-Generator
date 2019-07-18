package com.iteration.relaxio.network;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "MixSound.db";
    public static final String TABLE_NAME = "MixSound";
    public static final String COL_1 = "ID";
    public static final String COL_2 = "NAME";
    public static final String COL_3 = "COLOR";
    public static final String COL_4 = "VOLUME";
    public static final String COL_5 = "SOUND";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME +" (ID INTEGER PRIMARY KEY AUTOINCREMENT,NAME TEXT,COLOR TEXT,VOLUME INTEGER,SOUND TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(String name,String color,String volume, String sound) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_2,name);
        cv.put(COL_3,color);
        cv.put(COL_4,volume);
        cv.put(COL_5,sound);
        long result = db.insert(TABLE_NAME,null,cv);
        if(result == -1)
            return false;
        else
            return true;
    }

    public Cursor getCountData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select SUM(AMOUNT)as total, Count(*) as count from "+TABLE_NAME,null);
        res.moveToFirst();
        return res;
    }
    public Cursor getItemNameData(String itemId) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select Count(*) as count,* from "+TABLE_NAME+" Where ITEMID = "+ itemId,null);
        res.moveToFirst();
        return res;
    }

    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+TABLE_NAME,null);
        return res;
    }

    public Integer deleteTable () {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, null,null);
    }

    public Integer deleteData (String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, "ID = ?",new String[] {id});
    }

}
