package com.rbigsoft.sn.unzip.activity;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class AppDatabaseSQL extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "app_database";
    private static final String TABLE_HISTORY = "table_history";

    private static final String LINK = "time";

    public AppDatabaseSQL(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
        getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createTableHistory);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(deleteTableHistory);
    }

    private static final String createTableHistory = "CREATE TABLE " + TABLE_HISTORY + " (" +

            LINK + " TEXT " +
            ")";
    private static final String deleteTableHistory = "DROP TABLE IF EXISTS " + TABLE_HISTORY;


    public void deleteSectionUser(String time) {
        SQLiteDatabase database = getWritableDatabase();
        database.execSQL("DELETE FROM " + TABLE_HISTORY + " WHERE " + LINK + "=\"" + time + "\";");

    }

    public long insertSectionUser(String stringLink) {
        SQLiteDatabase database = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(LINK, stringLink);
        long newId = database.insert(TABLE_HISTORY, null, values);
        return newId;
    }


    public ArrayList<String> getAllHistory() {

        SQLiteDatabase database = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM 'table_history'";
        database.rawQuery(selectQuery, null);
        Cursor cursor = database.rawQuery(selectQuery, null);
        ArrayList<String> history = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String linkFile = cursor.getString(cursor.getColumnIndex(LINK));
            history.add(linkFile);
            cursor.moveToNext();
        }
        database.close();
        return history;
    }
}
