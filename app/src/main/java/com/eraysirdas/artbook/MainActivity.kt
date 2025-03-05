package com.eraysirdas.artbook

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.eraysirdas.artbook.databinding.ActivityMainBinding
import com.eraysirdas.artbook.databinding.ActivityUploadBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var artList : ArrayList<ArtModel>
    private lateinit var artAdapter: ArtAdapter
    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)



        artList = ArrayList()

        artAdapter = ArtAdapter(artList)
        binding.recyclerView.layoutManager=LinearLayoutManager(this)
        binding.recyclerView.adapter=artAdapter

        try{

            val database = this.openOrCreateDatabase("Arts", MODE_PRIVATE,null)

            val cursor = database.rawQuery("SELECT * FROM arts",null)
            val artNameIx = cursor.getColumnIndex("artname")
            val idIx = cursor.getColumnIndex("id")

            while(cursor.moveToNext()){
                val name = cursor.getString(artNameIx)
                val id =cursor.getInt((idIx))

                val art = ArtModel(name,id)
                artList.add(art)
            }
            artAdapter.notifyDataSetChanged()
            cursor.close()



        }catch (e : Exception){
            e.printStackTrace()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.add_menu, menu)
        return true

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==R.id.menuAddBtn){
            val intent = Intent(this,UploadActivity::class.java)
            intent.putExtra("info","new")
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
}