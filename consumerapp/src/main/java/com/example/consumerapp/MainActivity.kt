package com.example.consumerapp

import android.content.Intent
import android.database.ContentObserver
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.PersistableBundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.consumerapp.adapter.NoteAdapter
import com.example.consumerapp.db.DatabaseContract.NoteColumns.Companion.CONTENT_URI
import com.example.consumerapp.entity.Note
import com.example.consumerapp.helper.MappingHelper
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var adapter : NoteAdapter // this is null

    companion object{
        private const val EXTRA_STATE = "EXTRA_STATE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.title = "Consumer Notes"

        rv_notes.layoutManager = LinearLayoutManager(this) //vertical
        rv_notes.setHasFixedSize(true)
        adapter = NoteAdapter(this) // pemberian nilai ke adapter yg awalnya null dan mengirim context ke adapter via contsructor
        rv_notes.adapter = adapter

        fab_add.setOnClickListener {
            val intent = Intent(this@MainActivity, NoteAddUpdateActivity::class.java)
            //aksi memberikan nilai balik dari NoteAddUpdateActivity ke MainActivity
            startActivityForResult(intent, NoteAddUpdateActivity.REQUEST_ADD)
        }

        val handlerThread = HandlerThread("DataObserver")
        handlerThread.start()
        val handler = Handler(handlerThread.looper)
        val myObserver = object : ContentObserver(handler){
            override fun onChange(selfChange: Boolean) {
                loadNotesAsync()
            }
        }
        contentResolver.registerContentObserver(CONTENT_URI,true,myObserver)
        // proses ambil data
      //  loadNotesAsync()

        if(savedInstanceState == null){
            //proses ambil data
            loadNotesAsync()
        }else {
            val list = savedInstanceState.getParcelableArrayList<Note>(EXTRA_STATE)
            if(list != null){
                adapter.listNotes = list
            }
        }

    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState.putParcelableArrayList(EXTRA_STATE, adapter.listNotes)
    }
    /*
    Fungsi ini digunakan untuk load data dari tabel dan dan kemudian menampilkannya
    ke dalam list secara asynchronous dengan menggunakan Background process seperti berikut.
     */
    private fun loadNotesAsync(){
        GlobalScope.launch(Dispatchers.Main){
            progressbar.visibility = View.VISIBLE
            val deferredNotes = async(Dispatchers.IO){
             //   val cursor = noteHelper.queryAll()
                // CONTENT_URI = content://com.example.mynotesapp/noteCursor
// obyek Uri dengan nilai CONTENT_URI berarti akan memanggil query select semua data. Maka dari itu kita bisa
// mendapatkan semua data, namun kita perlu mengubahnya menjadi ArrayList supaya bisa ditampilkan di dalam adapter.
// Karena itulah kita memanggil fungsi mapCursorToArrayList untuk convert data dari cursor menjadi ArrayList.
                val cursor = contentResolver?.query(CONTENT_URI,null,null,null,null)
                MappingHelper.mapCursorToArrayList(cursor)
            }
            val notes = deferredNotes.await()
            progressbar.visibility = View.INVISIBLE
            if(notes.size > 0){
                adapter.listNotes = notes
            } else {
                adapter.listNotes = ArrayList()
                showSnackbarMessage("Tidak ada data saat ini")
            }
        }
    }

    private fun showSnackbarMessage(message : String){
        Snackbar.make(rv_notes, message, Snackbar.LENGTH_LONG).show()
    }
}