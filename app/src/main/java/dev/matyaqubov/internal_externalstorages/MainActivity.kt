package dev.matyaqubov.internal_externalstorages

import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import dev.matyaqubov.internal_externalstorages.adapter.PhotoExternalAdapter
import dev.matyaqubov.internal_externalstorages.adapter.PhotoInternalAdapter
import java.io.*
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private var is_Persistent = false
    private var is_internal = false
    private var readPermission = false
    private var cameraPermission = false
    private var locationPermission = false
    private var writePermission = false
    private var photos= mutableListOf<Uri>()
    private  var adapter=PhotoExternalAdapter(this,photos as ArrayList<Uri>)
    private var images= mutableListOf<Bitmap>()
    private  var adapter2=PhotoInternalAdapter(this,images as ArrayList<Bitmap>)
    private var APP_PERMISSION_CODE = 10025
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
    }

    private fun initViews() {

        var rv_ext=findViewById<RecyclerView>(R.id.rv_ext)
        rv_ext.adapter=adapter
        var rv_int=findViewById<RecyclerView>(R.id.rv_int)
        rv_int.adapter=adapter2

        loadPhotosFromExternalStorage()
        loadPhotosFromInternalStorage()
        createInternalFile()
        findViewById<Button>(R.id.b_save_internal).setOnClickListener {
            saveInternalFile("PDP da o'qish yaxshi ekanuuuuu lekin .....")
        }



        findViewById<Button>(R.id.b_read_internal).setOnClickListener {
            readInternalFile()
        }

        findViewById<Button>(R.id.b_save_ext).setOnClickListener {
            saveExt("PDP da o'qish yaxshi ekanuuuuu lekin .....")
        }

        findViewById<Button>(R.id.b_read_ext).setOnClickListener {
            readFromExt()
        }


        findViewById<Button>(R.id.b_take_photo).setOnClickListener {
            takePhoto.launch()
        }

        findViewById<Button>(R.id.b_del_ext).setOnClickListener {
            deleteExternalFile()
        }

        findViewById<Button>(R.id.b_del_int).setOnClickListener {
            deleteInternalFile()
        }


        //checkStoragePaths()

        requestPermission()
    }

    private fun readFromExt() {
        val fileName = "pdp_external.txt"
        val file: File
        file = if (is_Persistent) {
            File(getExternalFilesDir(null), fileName)
        } else {
            File(externalCacheDir, fileName)
        }
        try {
            val fileInputStream = FileInputStream(file)
            val inputStreamReader = InputStreamReader(fileInputStream, Charset.forName("UTF-8"))
            val lines: MutableList<String> = java.util.ArrayList()
            val reader = BufferedReader(inputStreamReader)
            var line = reader.readLine()
            while (line != null) {
                lines.add(line)
                line = reader.readLine()
            }

            val readText = TextUtils.join("\n", lines)
            Toast.makeText(
                this,
                "Read from file ${fileName} successfull: $readText",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Read from file ${fileName} failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveExt(data: String) {
        val fileName = "pdp_internal.txt"
        val file = if (is_Persistent) {
            File(getExternalFilesDir(null), fileName)
        } else {
            File(externalCacheDir, fileName)
        }

        try {
            val fileOutputStream = FileOutputStream(file)
            fileOutputStream.write(data.toByteArray(Charset.forName("UTF-8")))
            Toast.makeText(this, "write to $fileName successful", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Write to $fileName failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestPermission() {
        val hasReadPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        val hasWritePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        cameraPermission=ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)== PERMISSION_GRANTED
        locationPermission=ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)== PERMISSION_GRANTED
        val minSdk29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        readPermission = hasReadPermission
        writePermission = hasWritePermission || minSdk29

        var permissionsToRequest = mutableListOf<String>()
        if (!readPermission) permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        if (!cameraPermission) permissionsToRequest.add(Manifest.permission.CAMERA)
        if (!locationPermission) permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        if (!writePermission) permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permissionsToRequest.isNotEmpty()) permissionLauncher.launch(permissionsToRequest.toTypedArray())

    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            readPermission = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: readPermission
            writePermission = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: writePermission
            cameraPermission=permissions[Manifest.permission.CAMERA] ?: cameraPermission
            locationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: locationPermission

            if (readPermission) Toast.makeText(this, "READ_EXTERNAL_STORAGE", Toast.LENGTH_SHORT).show()
            if (writePermission) Toast.makeText(this, "WRITE_EXTERNAL_STORAGE", Toast.LENGTH_SHORT).show()
            if (cameraPermission) Toast.makeText(this, "CAMERA", Toast.LENGTH_SHORT).show()
            if (locationPermission) Toast.makeText(this, "LOCATION", Toast.LENGTH_SHORT).show()

        }

    private fun readInternalFile() {
        val fileName = "pdp_internal.txt"
        try {
            val fileInputStream: FileInputStream
            fileInputStream = if (is_Persistent) {
                openFileInput(fileName)
            } else {
                val file = File(cacheDir, fileName)
                FileInputStream(file)
            }

            val inputStreamReader = InputStreamReader(fileInputStream, Charset.forName("UTF-8"))
            val lines: MutableList<String> = ArrayList()
            val reader = BufferedReader(inputStreamReader)
            var line = reader.readLine()
            while (line != null) {
                lines.add(line)
                line = reader.readLine()
            }
            val readText = TextUtils.join("\n", lines)
            Toast.makeText(this, "$readText", Toast.LENGTH_SHORT).show()


        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Read from file $fileName's failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveInternalFile(data: String) {

        val fileName = "pdp_internal.txt"

        try {
            val fileOutputStream: FileOutputStream
            fileOutputStream = if (is_Persistent) {
                openFileOutput(fileName, MODE_PRIVATE)
            } else {
                val file = File(cacheDir, fileName)
                FileOutputStream(file)
            }

            fileOutputStream.write(data.toByteArray(Charset.forName("UTF-8")))
            Toast.makeText(this, "Write to $fileName successful", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Write to $fileName failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createInternalFile() {
        val fileName = "pdp_internal.txt"
        val file: File

        file = if (is_Persistent) {
            File(filesDir, fileName)
        } else {
            File(cacheDir, fileName)
        }

        if (!file.exists()) {
            try {
                file.createNewFile()
                Toast.makeText(this, "File ${fileName}s has been created", Toast.LENGTH_SHORT)
                    .show()
            } catch (e: IOException) {
                Toast.makeText(this, "File ${fileName}s creation failed", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "File ${fileName}s already existed", Toast.LENGTH_SHORT).show()
        }
    }

    private val takePhoto =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            val fileName = UUID.randomUUID().toString()
            val isPhotoSaved = if (is_internal) {
                savePhotoToInternalStorage(fileName, bitmap!!)
            } else {
                if (writePermission) {
                    savePhotoToExternalStorage(fileName, bitmap!!)
                } else false
            }

            if (isPhotoSaved) Toast.makeText(this, "Photo saved successfully", Toast.LENGTH_SHORT)
                .show()
            else Toast.makeText(this, "Failed to save photo", Toast.LENGTH_SHORT).show()
        }

    private fun savePhotoToExternalStorage(filename: String, bmp: Bitmap): Boolean {
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$filename.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.WIDTH, bmp.width)
            put(MediaStore.Images.Media.HEIGHT, bmp.height)
        }

        return try {
            contentResolver.insert(collection, contentValues)?.also { uri ->
                contentResolver.openOutputStream(uri).use { outputStream ->
                    if (!bmp.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)) {
                        throw IOException("Coudn't save bitmap")
                    }
                }
            } ?: throw IOException("Couln't create MediaStore entry")
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun checkStoragePaths() {
        val internal_m1 = getDir("custom", 0)
        val internal_m2 = filesDir

        val external_m1 = getExternalFilesDir(null)
        val external_m2 = externalCacheDir
        val external_m3 = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        Log.d("Storage", internal_m1.absolutePath)
        Log.d("Storage", internal_m2.absolutePath)
        Log.d("Storage", external_m1!!.absolutePath)
        Log.d("Storage", external_m2!!.absolutePath)
        Log.d("Storage", external_m3!!.absolutePath)


    }

    private fun openAppPermission() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", packageName, null)
        intent.data = uri

        startActivityForResult(intent, APP_PERMISSION_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == APP_PERMISSION_CODE) {
            // Here we check if the user granted the permission or not using
            //Manifest and PackageManager as usual
            checkPermissionIsGranted()
        }

    }

    private fun checkPermissionIsGranted() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PERMISSION_GRANTED
        )
            Toast.makeText(this, "Granted", Toast.LENGTH_SHORT).show()
        else
            requestPermission()
    }

    private fun savePhotoToInternalStorage(filename: String, bmp: Bitmap): Boolean {
        return try {
            openFileOutput("$filename.jpg", MODE_PRIVATE).use { stream ->
                if (!bmp.compress(Bitmap.CompressFormat.JPEG, 95, stream)) {
                    throw IOException("Couldn't save bitmap.")
                }
            }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    fun deleteExternalFile(){
        val fileName = "pdp_external.txt"
        val file:File
        file = if (is_Persistent){
            File(getExternalFilesDir(null),fileName)
        }else{
            File(externalCacheDir,fileName)
        }

        if (file.exists()){
            file.delete()
            Toast.makeText(this, "File ${fileName} has been deleted", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(this, "File ${fileName} doesn't exist", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteInternalFile(){
        val fileName = "pdp_internal.txt"
        val file:File
        file = if (is_Persistent){
            File(cacheDir, fileName)
        }else{
            File(cacheDir,fileName)
        }

        if (file.exists()){
            file.delete()
            Toast.makeText(this, "File ${fileName} has been deleted", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(this, "File ${fileName} doesn't exist", Toast.LENGTH_SHORT).show()
        }
    }

    fun loadPhotosFromExternalStorage(): List<Uri> {

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
        )
        //val photos = mutableListOf<Uri>()
        return contentResolver.query(
            collection,
            projection,
            null,
            null,
            "${MediaStore.Images.Media.DISPLAY_NAME} ASC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
            val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val displayName = cursor.getString(displayNameColumn)
                val width = cursor.getInt(widthColumn)
                val height = cursor.getInt(heightColumn)
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                photos.add(contentUri)
            }
            photos.toList()
        } ?: listOf()
        adapter.notifyDataSetChanged()
    }

    fun loadPhotosFromInternalStorage(): List<Bitmap> {
        val files = filesDir.listFiles()
        return files?.filter { it.canRead() && it.isFile && it.name.endsWith(".jpg") }?.map {
            val bytes = it.readBytes()
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            images.add(bmp)
            adapter2.notifyDataSetChanged()
            bmp
        } ?: listOf()
    }



}