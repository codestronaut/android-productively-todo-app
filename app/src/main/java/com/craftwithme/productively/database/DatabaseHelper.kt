package com.craftwithme.productively.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.craftwithme.productively.database.DatabaseContract.TodoColumns.Companion.DATE
import com.craftwithme.productively.database.DatabaseContract.TodoColumns.Companion.ID
import com.craftwithme.productively.database.DatabaseContract.TodoColumns.Companion.TABLE_NAME
import com.craftwithme.productively.database.DatabaseContract.TodoColumns.Companion.TIME
import com.craftwithme.productively.database.DatabaseContract.TodoColumns.Companion.TITLE

internal class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "todo_db"
        private const val DATABASE_VERSION = 1
        private const val SQL_CREATE_TODO_TABLE = "CREATE TABLE $TABLE_NAME" +
                " ($ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                " $TITLE TEXT NOT NULL," +
                " $DATE TEXT NOT NULL," +
                " $TIME TEXT NOT NULL)"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(SQL_CREATE_TODO_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

}