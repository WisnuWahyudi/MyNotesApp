package com.example.mynotesapp.helper

import android.database.Cursor
import com.example.mynotesapp.db.DatabaseContract
import com.example.mynotesapp.entity.Note

object MappingHelper {

/*
Pada NoteHelper proses load data dilakukan dengan eksekusi queryAll() menghasilkan objek Cursor,
 namun pada adapter kita membutuhkan dalam bentuk ArrayList, maka dari itu kita harus mengonversi dari
  Cursor ke Arraylist, di sinilah fungsi kelas pembantu MappingHelper. MoveToFirst di sini digunakan untuk
   memindah cursor ke baris pertama sedangkan MoveToNext digunakan untuk memindahkan cursor ke baris selanjutnya.
   Di sini kita ambil datanya satu per satu dan dimasukkan ke dalam ArrayList.
 */
    fun mapCursorToArrayList(notesCursor : Cursor?) : ArrayList<Note> {
        val notesList = ArrayList<Note>()

        notesCursor?.apply {
            while(moveToNext()){
                val id = getInt(getColumnIndexOrThrow(DatabaseContract.NoteColumns._ID))
                val title = getString(getColumnIndexOrThrow(DatabaseContract.NoteColumns.TITLE))
                val description = getString(getColumnIndexOrThrow(DatabaseContract.NoteColumns.DESCRIPTION))
                val date = getString(getColumnIndexOrThrow(DatabaseContract.NoteColumns.DATE))
                notesList.add(Note(id, title, description, date))
            }
        }
        return notesList
    }
}