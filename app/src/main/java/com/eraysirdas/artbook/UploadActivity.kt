package com.eraysirdas.artbook

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PackageManagerCompat
import androidx.transition.Visibility
import com.eraysirdas.artbook.databinding.ActivityUploadBinding
import com.google.android.material.snackbar.Snackbar
import java.io.ByteArrayOutputStream
import java.sql.Blob

class UploadActivity : AppCompatActivity() {
    private lateinit var binding : ActivityUploadBinding
    private lateinit var activityResultLauncher : ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher : ActivityResultLauncher<String>
    private var selectedBitmap : Bitmap? = null
    private lateinit var myDatabase : SQLiteDatabase



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        registerLauncher()
        myDatabase = this.openOrCreateDatabase("Arts",Context.MODE_PRIVATE,null)


        val intent = intent
        val info = intent.getStringExtra("info")
        val id = intent.getIntExtra("id",0)

        if(info.equals("old")){

            binding.button.visibility=View.INVISIBLE
            val selectId = intent.getIntExtra("id",0)

            val cursor = myDatabase.rawQuery("SELECT * FROM arts WHERE id = ?", arrayOf(selectId.toString()))

            val artNameIx = cursor.getColumnIndex("artname")
            val artistNameIx = cursor.getColumnIndex("artistname")
            val yearIx = cursor.getColumnIndex("year")
            val imageIx = cursor.getColumnIndex("image")

            while(cursor.moveToNext()){
                binding.artNameEt.setText(cursor.getString(artNameIx))
                binding.artistNameEt.setText(cursor.getString(artistNameIx))
                binding.yearEt.setText(cursor.getString(yearIx))

                val byteArray = cursor.getBlob(imageIx)
                val bitmap =BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
                binding.imageView.setImageBitmap(bitmap)
            }
            cursor.close()

        }else{
            binding.imageView.setImageResource(R.drawable.image)
            binding.artNameEt.setText("")
            binding.artistNameEt.setText("")
            binding.yearEt.setText("")
            binding.button.visibility=View.VISIBLE
        }

    }

    fun save (view: View){

        val artName = binding.artNameEt.text.toString().trim()
        val artistName = binding.artistNameEt.text.toString().trim()
        val year = binding.yearEt.text.toString().trim()

        if (artName.isEmpty() || artistName.isEmpty() || year.isEmpty()) {
            Toast.makeText(this, "Fill in the blank fields!", Toast.LENGTH_LONG).show()
            return
        }

        if(selectedBitmap!=null){
            val smallBitmap = makeSmallerBitmap(selectedBitmap!!,300)

            val outputStream =ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteArray=outputStream.toByteArray()

            try{
                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS arts(id INTEGER PRIMARY KEY,artname VARCHAR,artistname VARCHAR,year INT,image BLOB)")

                val sqlString = "INSERT INTO arts(artname,artistname,year,image) VALUES(?,?,?,?)"
                val statement = myDatabase.compileStatement(sqlString)

                statement.bindString(1,artName)
                statement.bindString(2,artistName)
                statement.bindLong(3, year.toLong())
                statement.bindBlob(4,byteArray)
                statement.execute()

            }catch (e : Exception){
                e.printStackTrace()
            }

            val intent = Intent(this@UploadActivity,MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }else{
            Toast.makeText(this, "Please choose a picture!", Toast.LENGTH_LONG).show()
        }





        if(binding.artNameEt.toString().isEmpty()||binding.artistNameEt.toString().isEmpty()||binding.yearEt.toString().isEmpty()){
            Toast.makeText(this,"Boş Alanları Doldurunuz",Toast.LENGTH_LONG).show()
        }else{

        }

    }





    private fun makeSmallerBitmap(image : Bitmap,maximumSize : Int) : Bitmap{
        var width = image.width
        var height = image.height

        val bitmapRatio : Double =width.toDouble() /height.toDouble()

        if(bitmapRatio>1){
            //landscape image
            width = maximumSize
            val scaleHeight = width/bitmapRatio
            height =scaleHeight.toInt()
        }else{
            //portrait image
            height=maximumSize
            val scaledWidth = height/bitmapRatio
            width=scaledWidth.toInt()

        }
        return Bitmap.createScaledBitmap(image,width,height,true)
    }

    fun selectImage(view : View){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU){
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_MEDIA_IMAGES)!= PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_MEDIA_IMAGES)){
                    //rationale
                    Snackbar.make(view,"Permission needned for gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission",View.OnClickListener {
                        //request permission
                        permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)

                    }).show()
                }else{
                    //request permission
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }

            }else{
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        }else{
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                    //rationale
                    Snackbar.make(view,"Permission needned for gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission",View.OnClickListener {
                        //request permission
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)

                    }).show()
                }else{
                    //request permission
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }

            }else{
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        }
    }

    private fun registerLauncher() {
        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

                if (result.resultCode == RESULT_OK) {
                    val intentFromResult = result.data
                    if (intentFromResult != null) {
                        val imageData = intentFromResult.data
                        //binding.imageView.setImageURI(imageData)
                        if (imageData != null) {
                            try {

                                if (Build.VERSION.SDK_INT >= 28) {
                                    val source =
                                        ImageDecoder.createSource(contentResolver, imageData)
                                    selectedBitmap = ImageDecoder.decodeBitmap(source)
                                    binding.imageView.setImageBitmap(selectedBitmap)
                                } else {
                                    selectedBitmap = MediaStore.Images.Media.getBitmap(
                                        contentResolver,
                                        imageData
                                    )
                                    binding.imageView.setImageBitmap(selectedBitmap)
                                }

                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                    }
                }
            }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->

            if(result){
               //permission granted
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }else{
                //permission denied
                Toast.makeText(this@UploadActivity,"Permission needed!",Toast.LENGTH_LONG).show()
            }

        }
    }
}