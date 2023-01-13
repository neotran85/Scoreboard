package com.bidaappscoreboard.service

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.*

private val TAG = "BidaFile"
private const val REQUEST_WRITE_STORAGE = 112
private const val PERMISSION_ALL = 1

fun copyFile(inputPath: String, inputFile: String, outputPath: String, outputFile: String, withDelete: Boolean): Boolean {
    var `in`: InputStream? = null
    var out: OutputStream? = null
    try {
        //create output directory if it doesn't exist
        val dir = File(outputPath)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        `in` = FileInputStream(inputPath + inputFile)
        out = FileOutputStream(outputPath + outputFile)
        val buffer = ByteArray(1024)
        var read: Int
        while (`in`.read(buffer).also { read = it } != -1) {
            out.write(buffer, 0, read)
        }
        `in`.close()
        `in` = null

        // write the output file
        out.flush()
        out.close()
        out = null

        // delete the original file
        if (withDelete) {
            File(inputPath + inputFile).delete()
        }

        return true
    } catch (fnfe1: FileNotFoundException) {
        fnfe1.message?.let { Log.e(TAG, it) }
        return false
    } catch (e: Exception) {
        e.message?.let { Log.e(TAG, it) }
        return false
    }
}

fun deleteFile(inputPath: String, inputFile: String) {
    File(inputPath + inputFile).delete()
}

fun listFiles(inputPath: String) {
    // using extension function walk
    File(inputPath).walk().toList()
}

fun checkFileExits(inputPath: String, inputFile: String): Boolean {
    return File(inputPath + inputFile).exists()
}

fun saveLogcatToFile(context: Context): Process? {
    val fileName = "${Environment.getExternalStorageDirectory()}/${Environment.DIRECTORY_DOCUMENTS}/logcat_" + System.currentTimeMillis() + ".txt"
    Log.i("saveLogcatToFile", "setup log .... $fileName")
    // val outputFile = File(folderName, fileName)
    val process = Runtime.getRuntime().exec("logcat *:I -f " + fileName)

//    try {
//        BufferedReader(InputStreamReader(process.errorStream)).let { b ->
//            var line: String?
//            if (b.readLine().also { line = it } != null) Log.e("saveLogcatToFile", line ?: "")
//        }
//    } catch (e: IOException) {
//        e.printStackTrace()
//    }

    return process
}

fun writeFile(inputPath: String, fileName: String, content: String) {
    File(inputPath + fileName).writeText(content)
}

fun readFile(inputPath: String, fileName: String): String? {
    return try {
        val bufferedReader: BufferedReader = File(inputPath + fileName).bufferedReader()
        bufferedReader.use { it.readText() }
    } catch (e: Exception) {
        Log.e(TAG, "readFile fail: ${e.message}")
        null
    }
}

// fun checkPermission(activityN: Activity): Boolean {
//     var listPermissionsNeeded: ArrayList<String> = arrayListOf()

//     if (ContextCompat.checkSelfPermission(
//         activityN.baseContext,
//         Manifest.permission.WRITE_EXTERNAL_STORAGE
//     ) != PackageManager.PERMISSION_GRANTED) {
//         Log.d(TAG, "WRITE_EXTERNAL_STORAGE not GRANTED")
//         listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
//     }

// //    if (ContextCompat.checkSelfPermission(
// //        activityN.baseContext,
// //        Manifest.permission.MANAGE_EXTERNAL_STORAGE
// //    ) != PackageManager.PERMISSION_GRANTED) {
// //        Log.d(TAG, "MANAGE_EXTERNAL_STORAGE not GRANTED")
// //        listPermissionsNeeded.add(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
// //    }
// //
// //    if (ContextCompat.checkSelfPermission(
// //        activityN.baseContext,
// //        Manifest.permission.READ_LOGS
// //    ) != PackageManager.PERMISSION_GRANTED) {
// //        Log.d(TAG, "READ_LOGS not GRANTED")
// //        listPermissionsNeeded.add(Manifest.permission.READ_LOGS)
// //    }

//     return if (listPermissionsNeeded.isEmpty()) {
//         Log.i(TAG, "PERMISSION accept ${listPermissionsNeeded.size}")
//         true
//     } else {
//         // ask the permission
//         ActivityCompat.requestPermissions(
//             activityN,
//             listPermissionsNeeded.toTypedArray(),
//             PERMISSION_ALL
//         )
//         Log.w(TAG, "PERMISSION request.....")
//         // You have to put nothing here (you can't write here since you don't
//         // have the permission yet and requestPermissions is called asynchronously)
//         false
//     }
// }
