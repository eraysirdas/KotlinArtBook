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
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.eraysirdas.artbook.databinding.ActivityUploadBinding
import com.google.android.material.snackbar.Snackbar
import java.io.ByteArrayOutputStream

class UploadActivity : AppCompatActivity() {
    private lateinit var binding : ActivityUploadBinding
    private lateinit var activityResultLauncher : ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher : ActivityResultLauncher<String>
    private var selectedBitmap : Bitmap? = null
    private lateinit var myDatabase : SQLiteDatabase
    private var selectId : Int? = null
    private var info : String? = null
    private var currentArtName = ""
    private var currentArtistName = ""
    private var currentYear = ""
    private var currentImage : ByteArray? = null
    private var sqlString =""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        registerLauncher()
        myDatabase = this.openOrCreateDatabase("Arts",Context.MODE_PRIVATE,null)


        val intent = intent
        info = intent.getStringExtra("info")
        selectId = intent.getIntExtra("id",0)

        if(info.equals("old")){

            binding.button.visibility=View.INVISIBLE
            binding.artNameEt.isEnabled=false
            binding.artistNameEt.isEnabled=false
            binding.yearEt.isEnabled=false
            binding.imageView.isEnabled=false
            sqlOperation()

        }else if(info.equals("new")){
            binding.imageView.setImageResource(R.drawable.image)
            binding.artNameEt.setText("")
            binding.artistNameEt.setText("")
            binding.yearEt.setText("")
            binding.button.visibility=View.VISIBLE

        }else{

            binding.button.visibility = View.VISIBLE
            binding.button.text = "Update"
            sqlOperation()
        }

    }

    private fun sqlOperation() {
        val cursor = myDatabase.rawQuery("SELECT * FROM arts WHERE id = ?", arrayOf(selectId.toString()))

        val artNameIx = cursor.getColumnIndex("artname")
        val artistNameIx = cursor.getColumnIndex("artistname")
        val yearIx = cursor.getColumnIndex("year")
        val imageIx = cursor.getColumnIndex("image")

        while(cursor.moveToNext()){
            currentArtName=(cursor.getString(artNameIx))
            currentArtistName=(cursor.getString(artistNameIx))
            currentYear=(cursor.getString(yearIx))

            binding.artNameEt.setText(currentArtName)
            binding.artistNameEt.setText(currentArtistName)
            binding.yearEt.setText(currentYear)

            currentImage = cursor.getBlob(imageIx)
            val byteArray = currentImage
            val bitmap =BitmapFactory.decodeByteArray(byteArray,0,byteArray!!.size)
            binding.imageView.setImageBitmap(bitmap)

        }
        cursor.close()
    }

    fun save (view: View){

        val artName = binding.artNameEt.text.toString()
        val artistName = binding.artistNameEt.text.toString()
        val year = binding.yearEt.text.toString()


        if (artName.isEmpty() || artistName.isEmpty() || year.isEmpty()) {
            Toast.makeText(this, "Fill in the blank fields!", Toast.LENGTH_LONG).show()
            return
        }

        var byteArray: ByteArray?=null

        if(selectedBitmap!=null){
            val smallBitmap = makeSmallerBitmap(selectedBitmap!!,300)

            val outputStream =ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            byteArray=outputStream.toByteArray()

        }else if(info == "new" && byteArray == null){

            Toast.makeText(this, "Please choose a picture!", Toast.LENGTH_LONG).show()
            return

        } else{

            byteArray=currentImage
        }

        try{
            if(info.equals("new")){

                myDatabase.execSQL("CREATE TABLE IF NOT EXISTS arts(id INTEGER PRIMARY KEY,artname VARCHAR,artistname VARCHAR,year VARCHAR,image BLOB)")
                sqlString = "INSERT INTO arts (artname,artistname,year,image) VALUES(?,?,?,?)"

            }else if(info.equals("update")){
                val queryBuilder = StringBuilder()
                queryBuilder.append("UPDATE arts SET ")

                var hasChanges = false

                if(artName != currentArtName){
                    queryBuilder.append("artname = ?, ")
                    hasChanges=true
                }
                if(artistName != currentArtistName){
                    queryBuilder.append("artistname = ?, ")
                    hasChanges=true
                }
                if(year != currentYear){
                    queryBuilder.append("year = ?, ")
                    hasChanges=true
                }
                if(!byteArray.contentEquals((currentImage))){
                    queryBuilder.append("image = ?, ")
                    hasChanges=true
                }

                if (!hasChanges) {
                    Toast.makeText(this, "No changes to update!", Toast.LENGTH_LONG).show()
                    return
                }

                sqlString = queryBuilder.substring(0,queryBuilder.length-2)
                sqlString += " WHERE id = "+ selectId
            }

            val statement = myDatabase.compileStatement(sqlString)

            var bindIndex = 1

            if (artName != currentArtName) {
                statement.bindString(bindIndex++, artName)
            }
            if (artistName != currentArtistName) {
                statement.bindString(bindIndex++, artistName)
            }
            if (year != currentYear) {
                statement.bindString(bindIndex++, year)
            }
            if (!byteArray.contentEquals(currentImage)) {
                statement.bindBlob(bindIndex++, byteArray)
            }

            statement.execute()

        }catch (e : Exception){
            e.printStackTrace()
            Log.e("UploadActivity", "SQL Exception: ${e.message}")
        }

        val intent = Intent(this@UploadActivity,MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.delete_menu, menu)

        if (info == "update") {
            menu?.findItem(R.id.menuDeleteBtn)?.isVisible = false
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==R.id.menuDeleteBtn){
            showAlertDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun deleteSqlOperation() {
        myDatabase.execSQL("DELETE FROM arts WHERE id = ?", arrayOf(selectId.toString()))
        myDatabase.close()
    }

    private fun showAlertDialog() {
        val alertDialog =AlertDialog.Builder(this@UploadActivity)
        alertDialog.setTitle("Art Delete")
        alertDialog.setMessage("Do you want to delete the art?")

        alertDialog.setPositiveButton("Yes"){dialog,which->

            deleteSqlOperation()

            Toast.makeText(this@UploadActivity,"Art Deleted!",Toast.LENGTH_LONG).show()

            val intent = Intent(this@UploadActivity,MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        alertDialog.setNegativeButton("No"){dialog,which->
            dialog.dismiss()
        }
        alertDialog.show()
    }
}