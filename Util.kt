package com.example.animatindemoapp.UtillAzaz

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.Window
import android.view.WindowManager
import androidx.core.content.ContextCompat
import android.os.Environment
import java.io.File
import android.provider.MediaStore

import android.content.ContentValues
import android.graphics.Insets

import android.graphics.Matrix
import android.util.DisplayMetrics
import android.view.WindowInsets

import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream


object Util {

// this function for check internet connection is available or wifi is on
    fun isOnline(context: Context?): Boolean {
        val cm =context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val n = cm.activeNetwork
            if (n != null) {
                val nc = cm.getNetworkCapabilities(n)
                //It will check for both wifi and cellular network
                return nc!!.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || nc.hasTransport(
                    NetworkCapabilities.TRANSPORT_WIFI
                )
            }
            return false
        } else {
            val netInfo = cm.activeNetworkInfo
            return netInfo != null && netInfo.isConnectedOrConnecting
        }
    }

    //this function to change status bar color change
    fun  changeStatusbarColor(myActivityReference:Activity,color:Int)
    {
        val window: Window = myActivityReference.getWindow()
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        window.setStatusBarColor(ContextCompat.getColor(myActivityReference, color))
    }



    //this method for fast bitmap convert to Resource
    // parameter getResource resource id , height, width
    fun decodeSampledBitmapFromResource(
        res: Resources, resId: Int, reqWidth: Int, reqHeight: Int): Bitmap {
        // First decode with inJustDecodeBounds=true to check dimensions
        return BitmapFactory.Options().run {
            inJustDecodeBounds = true
            BitmapFactory.decodeResource(res, resId, this)

            // Calculate inSampleSize
            inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)

            // Decode bitmap with inSampleSize set
            inJustDecodeBounds = false

            BitmapFactory.decodeResource(res, resId, this)
        }
    }

    //this method help to decodeSampledBitmapFromResource method
    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }
    // this function use when user permission not granted or granted to in setting
     fun goToSetting(activity: Activity)
    {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package",activity.packageName,null)
        intent.data = uri
       activity.startActivity(intent)

    }
    fun commonDocumentDirPath(FolderName: String): File? {
        var dir: File? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            dir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                    .toString() + "/" + FolderName
            )
        } else {
            dir = File(Environment.getExternalStorageDirectory().toString() + "/" + FolderName)
        }

        // Make sure the path directory exists.
        if (!dir.exists()) {
            // Make it, if it doesn't exit
            val success: Boolean = dir.mkdirs()
            if (!success) {
                dir = null
            }
        }


        return dir
    }



    @Throws(IOException::class)
     fun saveImages(mContext: Context, bitmap: Bitmap, name: String): Uri? {
        val saved: Boolean
        val fos: OutputStream?
        var image: File? = null
        var imageUri: Uri? = null
        var imagesDir: String? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = mContext.contentResolver
            val contentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/gaha")
            imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            fos = resolver.openOutputStream(imageUri!!)
        } else {
            imagesDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM
            ).toString() + File.separator + "gaha"
            val file = File(imagesDir)
            if (!file.exists()) {
                file.mkdir()
            }
            image = File(imagesDir, "$name.png")
            fos = FileOutputStream(image)
        }

//        val bitmaps = BitmapFactory.decodeByteArray(bitmap,0, bitmap.size)

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100 , fos)

        fos!!.flush()
        fos.close()
        return imageUri
    }


    //this function for reduce bitmap size
    // note  this function use to bitmap quality has down min maxSize = 500
    fun getResizedBitmap(image: Bitmap, maxSize: Int): Bitmap? {
        var width = image.width

        var height = image.height
        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
    }


    // this function rotate image or bitmap  when image
    // landscape to portal  use this function
    // angle 90 = portal  and angle 180 = landscape
    fun rotateImage(angle: Int, bitmapSrc: Bitmap): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(angle.toFloat())
        return Bitmap.createBitmap(
            bitmapSrc, 0, 0,
            bitmapSrc.width, bitmapSrc.height, matrix, true
        )
    }

    // this  functin for find height and width display
    // this function use device screen pixel find and set layout


    fun getScreenWidth(activity: Activity): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = activity.windowManager.currentWindowMetrics
            val insets: Insets = windowMetrics.windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            windowMetrics.bounds.width() - insets.left - insets.right

        } else {
            val displayMetrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.widthPixels
        }
    }
    fun getScreenHeight(activity: Activity):Int
    {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        {
            val windowMatrix = activity.windowManager.currentWindowMetrics
            val insets: Insets = windowMatrix.windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            windowMatrix.bounds.height() - insets.top - insets.bottom

        }
        else
        {
            val  displayMetrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.heightPixels

        }
    }


}