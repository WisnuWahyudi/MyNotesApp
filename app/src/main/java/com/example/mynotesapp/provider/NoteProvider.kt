package com.example.mynotesapp.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import com.example.mynotesapp.db.DatabaseContract.AUTHORITY
import com.example.mynotesapp.db.DatabaseContract.NoteColumns.Companion.CONTENT_URI
import com.example.mynotesapp.db.DatabaseContract.NoteColumns.Companion.TABLE_NAME
import com.example.mynotesapp.db.NoteHelper

/*
Kalau dilihat di dalam metode insert kita memanggil fungsi insert yang berasal dari NoteHelper.
Jadi, aslinya content provider hanya sebagai jembatan sebelum mengakses noteHelper. Namun kelebihan dari
 penggunaan Content Provider yaitu datanya bisa diakses dari aplikasi lain.
 */
class NoteProvider : ContentProvider() {

    companion object{

        /*
        Integer digunakan sebagai identifier antara select all sama select by id
         */
        private const val NOTE = 1
        private const val NOTE_ID = 2
        private val sUriMatcher = UriMatcher(UriMatcher.NO_MATCH)
//mempermudah dalam proses membandingkan obyek Uri yang akan kita gunakan. Mengapa? Menggunakan
// string sebagai perbandingan tentunya akan lebih kompleks jika dibandingkan dengan integer
        /*
        Uri matcher untuk mempermudah identifier dengan menggunakan integer
        misal
        uri com.dicoding.picodiploma.mynotesapp dicocokan dengan integer 1
        uri com.dicoding.picodiploma.mynotesapp/# dicocokan dengan integer 2
         */
        private lateinit var noteHelper: NoteHelper


        init {
            // content://com.dicoding.picodiploma.mynotesapp/note
            sUriMatcher.addURI(AUTHORITY, TABLE_NAME, NOTE)

            // content://com.dicoding.picodiploma.mynotesapp/note/id
            sUriMatcher.addURI(AUTHORITY,
                "$TABLE_NAME/#",
                NOTE_ID)
        }

    }

    override fun onCreate(): Boolean {
        noteHelper = NoteHelper.getInstance(context as Context)
        noteHelper.open()
        return true
    }

    /*
    Method queryAll digunakan ketika ingin menjalanakan queryAll Select
    Return cursor
     */
    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        val cursor : Cursor?
        when(sUriMatcher.match(uri)){
            NOTE -> cursor = noteHelper.queryAll()
            NOTE_ID -> cursor = noteHelper.queryById(uri.lastPathSegment.toString())
            else -> cursor = null
        }
        return cursor
    }

    override fun getType(uri: Uri): String? {
       return null
    }


    override fun insert(uri: Uri, contentValues: ContentValues?): Uri? {
        val added : Long = when(NOTE){
            sUriMatcher.match(uri) -> noteHelper.insert(contentValues!!)
            else -> 0
        }
        context?.contentResolver?.notifyChange(CONTENT_URI,null)
        return  Uri.parse("$CONTENT_URI/$added")
    }


    override fun update(uri: Uri, contentValues: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        val update : Int = when(NOTE_ID){
            sUriMatcher.match(uri) -> noteHelper.update(uri.lastPathSegment.toString(),contentValues)
            else -> 0
        }
        context?.contentResolver?.notifyChange(CONTENT_URI,null)
        return update
    }


    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val delete : Int = when(NOTE_ID){
            sUriMatcher.match(uri) -> noteHelper.deleteById(uri.lastPathSegment.toString())
            else -> 0
        }
        context?.contentResolver?.notifyChange(CONTENT_URI,null)
        return delete
    }
}
