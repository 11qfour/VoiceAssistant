package com.example.voiceassistant

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "messageDb"
        const val TABLE_NAME = "messages"

        const val FIELD_ID = "id"
        const val FIELD_MESSAGE = "message"
        const val FIELD_SEND = "send"
        const val FIELD_DATE = "date"
    }

    // Вызывается при первом создании базы данных
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("create table $TABLE_NAME (" +
                "$FIELD_ID integer primary key," +
                "$FIELD_MESSAGE text," +
                "$FIELD_SEND integer," + // Boolean храним как 0 или 1
                "$FIELD_DATE text" + ")")
    }

    // Вызывается при обновлении версии базы данных
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("drop table if exists $TABLE_NAME")
        onCreate(db)
    }
}