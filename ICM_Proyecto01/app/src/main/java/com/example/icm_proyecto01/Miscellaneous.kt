package com.example.icm_proyecto01

import android.content.Context
import java.io.IOException
import java.io.InputStream

class Miscellaneous {

    companion object {
        const val PERMISSION_CAMERA = 101
        const val PERMISSION_FINE_LOCATION = 102
        const val PERMISSION_BACKGROUND_LOCATION = 103
        const val PERMISSION_GALLERY = 104
        const val PERMISSION_ACTIVITY_RECOGNITION = 105
         const val PERMISSION_MULTIPLE = 3000


        fun loadJSONFromAsset(context: Context, fileName: String): String? {
            return try {
                val inputStream: InputStream = context.assets.open(fileName)
                val size = inputStream.available()
                val buffer = ByteArray(size)
                inputStream.read(buffer)
                inputStream.close()
                String(buffer, charset("UTF-8"))
            } catch (ex: IOException) {
                ex.printStackTrace()
                null
            }
        }

    }


}