package com.tutorial.activityrecognition

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SqliteDB(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    // below is the method for creating a database by a sqlite query
    override fun onCreate(db: SQLiteDatabase) {
        // db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME)
        val query = ("CREATE TABLE " + TABLE_NAME + " ("
                + ID_COL + " INTEGER PRIMARY KEY, " +
                DAY + " TEXT, " +
                TIME + " TEXT, " +
                DURATION + " INTEGER, " +
                ACTIVITY + " TEXT" +
                ")")

        db.execSQL(query)
    }

    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME)
        onCreate(db)
    }

    fun addEntry(day : String,
                 time : String,
                 duration: Int,
                 activity: String){

        val values = ContentValues()
        values.put(DAY, day)
        values.put(TIME, time)
        values.put(DURATION, duration)
        values.put(ACTIVITY, activity)

        val db = this.writableDatabase
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    fun getData(): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null)
    }

    companion object{
        // day: 12/12/2003, time 12:24, duration in mins: 30 (0 if less than a min) ,
        // Activity: walking (from question)

        // below is variable for database name
        private val DATABASE_NAME = "ACTIVITY_DATABASE"

        // below is the variable for database version
        private val DATABASE_VERSION = 1

        // define column names in the Database
        val TABLE_NAME = "activity_table"
        val DAY = "day"
        val TIME = "time"
        val DURATION = "duration"
        val ACTIVITY = "activity"
        val ID_COL = "id"
    }
}