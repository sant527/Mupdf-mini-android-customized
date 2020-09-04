package com.artifex.mupdf.mini.Database;

/**
 * Created by simha on 8/8/20.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class KrishnaDatabaseAdapter {

    public Context context;
    public SacIndiaHelper helper;

    public KrishnaDatabaseAdapter(Context context) {
        this.context = context;
        helper = new SacIndiaHelper(context);
    }

    public void deleteandcreatetable(){
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + SacIndiaHelper.TABLE_NAME_OUTLINE);
        db.execSQL(SacIndiaHelper.CREATE_TABLE_OUTLINE);
    }

    public long insertOutline(
            String jsonarraystring
    ){
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SacIndiaHelper.JSONSTRING_OUTLINE, jsonarraystring);
        long iddb = db.insert("\"" + SacIndiaHelper.TABLE_NAME_OUTLINE + "\"", null, contentValues);
        return iddb;
    }

    public String getDetailsasarrayOutlinetable() {
        SQLiteDatabase db = helper.getReadableDatabase();
        String[] columns = {
                SacIndiaHelper.JSONSTRING_OUTLINE,
        };
        Cursor cursor = db.query("\"" + SacIndiaHelper.TABLE_NAME_OUTLINE + "\"", columns, null, null, null, null, null);
        ArrayList<OutlineItem> outlineItem = new ArrayList<OutlineItem>();
        String jsonString = "";
        while (cursor.moveToNext()) {
            jsonString = cursor.getString(cursor.getColumnIndex(SacIndiaHelper.JSONSTRING_OUTLINE));
        }
        return jsonString;
    }

    /*
    Note:
    - SQLiteOpenHelper is an abstract class. We have to implement all the
    abstract methods
    - SQLiteOpenHelper has constructor: So we have to use super() method
    to intialize the class SQLiteOpenHelper class data
    */
    public static class SacIndiaHelper extends SQLiteOpenHelper {

        // Used in constructor for super
        private static final int DATABASE_VERSION = 11;
        private static final String DATABASE_NAME = "mupdf_mini.db";
        private Context context;

        /*
        Used in
        -- CREATE_TABLE_LEVEL1_PLACES
        -- db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_LEVEL1_PLACES);
        */
        private static final String TABLE_NAME_OUTLINE = "OUTLINE";

        private static final String UID_OUTLINE = "_id";
        private static final String JSONSTRING_OUTLINE = "jsonstring_outline";
        private static final String CREATE_TABLE_OUTLINE = "CREATE TABLE " + TABLE_NAME_OUTLINE
                + " ("
                + UID_OUTLINE + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + JSONSTRING_OUTLINE + " TEXT"
                + ");";

        /*
        - If we dont mention the scope the default scope is package-private
        - Visible only to classes in the same package.
        */
        SacIndiaHelper(Context context) {
        	/*
	    	- SQLiteOpenHelper has constructor: So we have to use super() method to intialize the class SQLiteOpenHelper class data
        	*/
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            this.context = context;
        }

        /*
        - SQLiteOpenHelper is an abstract class. We have to implement all the
    	abstract methods
        */
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_OUTLINE);
                db.execSQL(CREATE_TABLE_OUTLINE);
            } catch (SQLException e) {
                Log.d("com.simha.yatras", "db.execSQL(CREATE_TABLE_OUTLINE);" + e.toString());
            }
        }

        public void onUpgrade(
                SQLiteDatabase db,
                int oldVersion,
                int newVersion
        ) {
            try {
                onCreate(db);
            } catch (SQLException e) {
                Log.d("com.simha.yatras", "error upgrading" + e.toString());
            }
        }
    }

}
