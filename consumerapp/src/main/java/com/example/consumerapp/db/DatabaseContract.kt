package com.example.consumerapp.db

import android.net.Uri
import android.provider.BaseColumns

/*
Kelas DatabaseContract berperan penting di dalam aplikasi
 */
object DatabaseContract {

    const val AUTHORITY = "com.example.mynotesapp"
    const val SCHEME = "content"

        class NoteColumns : BaseColumns {
        companion object{
            const val TABLE_NAME = "note"
            const val _ID = "_id"
            const val TITLE = "title"
            const val DESCRIPTION = "description"
            const val DATE = "date"


            // untuk membuat URI content://com.example.mynotesapp/note
            val CONTENT_URI : Uri = Uri.Builder().scheme(SCHEME)
                .authority(AUTHORITY)
                .appendPath(TABLE_NAME)
                .build()
            /*
 Nah lalu apa sebenarnya variabel CONTENT_URI? Di sini kita menggabungkan base authority dengan scheme dan nama tabel,
  nanti string yang akan tercipta adalah "content://com.example.mynotesapp/note".

Artinya dari string "content://com.example.mynotesapp/note" berarti kita akan mencoba untuk akses data tabel
 Note dari provider NoteProvider.

Simpel bukan? Yup konsep dari Uri sangatlah simple.

             */
        }
    }
}