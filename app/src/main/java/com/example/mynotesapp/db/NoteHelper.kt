package com.example.mynotesapp.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import java.sql.SQLException

class NoteHelper(context: Context) {
    companion object{
        private const val DATABE_TABLE = DatabaseContract.NoteColumns.TABLE_NAME
        private lateinit var dataBaseHelper : DatabaseHelper

        private var INSTANCE : NoteHelper? = null
        fun getInstance(context: Context) : NoteHelper =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: NoteHelper(context)
            }

        private lateinit var database : SQLiteDatabase

    }

    init{
        dataBaseHelper = DatabaseHelper(context)
    }

    @Throws(SQLException::class)
    fun open(){
        database = dataBaseHelper.writableDatabase
    }

    fun close(){
        dataBaseHelper.close()

        if(database.isOpen)
            database.close()
    }

    fun queryAll() : Cursor {
        return database.query(
            DATABE_TABLE,
            null,
            null,
            null,
            null,
            null,
            "${DatabaseContract.NoteColumns._ID} ASC"
        )
    }

    fun queryById(id : String) : Cursor {
        return database.query(
            DATABE_TABLE,
            null,
            "${DatabaseContract.NoteColumns._ID} = ?",
            arrayOf(id),
            null,
            null,
            null,
            null
        )
    }

    fun insert(values : ContentValues) : Long{
        return database.insert(DATABE_TABLE, null,values)
    }

    fun update(id : String, values : ContentValues?) : Int{
        return database.update(DATABE_TABLE, values, "${DatabaseContract.NoteColumns._ID} = ?", arrayOf(id))
    }

    fun deleteById(id : String) : Int{
        return database.delete(DATABE_TABLE, "${DatabaseContract.NoteColumns._ID} = '$id'",null)
    }
}