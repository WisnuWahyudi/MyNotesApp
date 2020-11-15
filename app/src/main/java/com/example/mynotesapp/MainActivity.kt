package com.example.mynotesapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mynotesapp.adapter.NoteAdapter
import com.example.mynotesapp.db.NoteHelper
import com.example.mynotesapp.entity.Note
import com.example.mynotesapp.helper.MappingHelper
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var adapter : NoteAdapter
    private lateinit var noteHelper : NoteHelper


    companion object{
        private const val EXTRA_STATE = "EXTRA_STATE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.title = "Notes"

        rv_notes.layoutManager = LinearLayoutManager(this)
        rv_notes.setHasFixedSize(true)
        adapter = NoteAdapter(this)
        rv_notes.adapter = adapter

        fab_add.setOnClickListener {
            val intent = Intent(this@MainActivity, NoteAddUpdateActivity::class.java)
            //aksi memberikan nilai balik dari NoteAddUpdateActivity ke MainActivity
            startActivityForResult(intent, NoteAddUpdateActivity.REQUEST_ADD)
        }

        noteHelper = NoteHelper.getInstance(applicationContext)
        noteHelper.open()

        // proses ambil data
        loadNotesAsync()

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
                val cursor = noteHelper.queryAll()
                MappingHelper.mapCursorToArrayList(cursor)
            }
            progressbar.visibility = View.INVISIBLE
            val notes = deferredNotes.await()
            if(notes.size > 0){
                adapter.listNotes = notes
            } else {
                adapter.listNotes = ArrayList()
                showSnackbarMessage("Tidak ada data saat ini")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        noteHelper.close()
    }


    /*
Metode onActivityResult() akan melakukan penerimaan data dari intent yang
  dikirimkan dan diseleksi berdasarkan jenis requestCode dan resultCode-nya.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(data != null){
            when(requestCode){
                // Akan dipanggil jika request codenya ADD
                NoteAddUpdateActivity.REQUEST_ADD -> if(resultCode == NoteAddUpdateActivity.RESULT_ADD){
                    val note = data.getParcelableExtra<Note>(NoteAddUpdateActivity.EXTRA_NOTE)

                    adapter.addItem(note!!)
                    rv_notes.smoothScrollToPosition(adapter.itemCount - 1)

                    showSnackbarMessage("Satu item berhasil ditambahkan")
                }
                // Update dan Delete memiliki request code sama akan tetapi result codenya berbeda
                NoteAddUpdateActivity.REQUEST_UPDATE ->
                    when(resultCode) {
                        /*
                        Akan dipanggil jika result codenya UPDATE
                        Semua data di load kembali dari awal
                         */

           /*
           baris di bawah akan dijalankan ketika terjadi penambahan data pada NoteAddUpdateActivity.
           Alhasil, ketika metode ini dijalankan maka kita akan membuat objek note baru dan inisiasikan dengan
            getParcelableExtra.
            Lalu panggil metode addItem yang berada di adapter dengan memasukan objek note sebagai argumen.
             Metode tersebut akan menjalankan notifyItemInserted dan penambahan arraylist-nya.
             Lalu objek rvNotes akan melakukan smoothscrolling, dan terakhir muncul notifikasi pesan dengan
              menggunakan Snackbar.
            */
                        NoteAddUpdateActivity.RESULT_UPDATE -> {
                            val note = data.getParcelableExtra<Note>(NoteAddUpdateActivity.EXTRA_NOTE)
                            val position = data.getIntExtra(NoteAddUpdateActivity.EXTRA_POSITION,0)

                            adapter.updateItem(position, note!!)
                            rv_notes.smoothScrollToPosition(position)

                            showSnackbarMessage("Satu item berhasil diubah")
                        }
                        /*
                        Akan dipanggil jika result codenya DELETE
                        Delete akan menghapus data dari list berdasarkan dari position
                         */
                        NoteAddUpdateActivity.RESULT_DELETE -> {
                            val position = data.getIntExtra(NoteAddUpdateActivity.EXTRA_POSITION, 0)

                            adapter.removeItem(position)
                            showSnackbarMessage("Satu item berhasil dihapus")
                        }
                    }
            }
        }
    }

    private fun showSnackbarMessage(message : String){
        Snackbar.make(rv_notes, message, Snackbar.LENGTH_LONG).show()
    }
}