package com.bidaappscoreboard.service

import android.media.MediaMetadataRetriever
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import com.abedelazizshe.lightcompressorlibrary.CompressionListener
import com.abedelazizshe.lightcompressorlibrary.VideoCompressor
import com.abedelazizshe.lightcompressorlibrary.VideoQuality
import com.abedelazizshe.lightcompressorlibrary.config.Configuration
import com.abedelazizshe.lightcompressorlibrary.config.SaveLocation
import com.abedelazizshe.lightcompressorlibrary.config.SharedStorageConfiguration
import com.bidaappscoreboard.MainActivity
import com.bidaappscoreboard.config.AppConfig
import com.bidaappscoreboard.model.UploadMetadata
import com.bidaappscoreboard.model.UploadResult
import com.bidaappscoreboard.store.ScoreBoard
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

object MediaService {
    private const val TAG = "MediaService"

    private var serverUploadDirectoryPath: String = "${AppConfig.MEDIA_URL}upload-video"
    private val client = OkHttpClient()

    fun uploadFile(sourceFile: File, uploadedFileName: String? = null, function: (result: UploadResult) -> Unit) {
        Thread {
            val mimeType = getMimeType(sourceFile);
            if (mimeType == null) {
                Log.e(TAG, "${sourceFile.path} Not able to get mime type")
                return@Thread
            }
            val fileName: String = uploadedFileName ?: sourceFile.name
            // toggleProgressDialog(true)

            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(sourceFile.path)
            val vWidth = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
            val vHeight = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
            val vDuration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0
            val vFrameRate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)?.toDoubleOrNull() ?: 0.0
            val vBitRate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toIntOrNull() ?: 0

            val uploadMetadata = UploadMetadata(
                name = fileName,
                width = vWidth,
                height = vHeight,
                duration = vDuration,
                frameRate = vFrameRate,
                bitRate = vBitRate
            )

            Log.d(TAG,"upload:::::$uploadMetadata:::::::::${sourceFile.path}")


            try {
                // TODO: check token exp before upload file
                val token = ScoreBoard.token

                val requestBody: RequestBody =
                    MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("data", fileName,sourceFile.asRequestBody(mimeType.toMediaTypeOrNull()))
                        .addFormDataPart("extension", "mp4")
                        .addFormDataPart("mime", mimeType)
                        .addFormDataPart("duration", (vDuration / 1000).toString())
                        .addFormDataPart("width", vWidth.toString())
                        .addFormDataPart("height", vHeight.toString())
                        .build()

                val request: Request = Request.Builder()
                    .url(serverUploadDirectoryPath)
                    .addHeader("Authorization","Bearer $token")
                    .post(requestBody).build()
                val response: Response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    // note: after call response.body?.string(), response will be closed.
                    val resString = response.body?.string();

                    Log.i(TAG,"upload media success, path: $serverUploadDirectoryPath$fileName, $resString")

                    val gson = Gson()
                    var uploadResult: UploadResult = gson.fromJson(resString, UploadResult::class.java)

                    uploadResult.metadata = uploadMetadata

                    function.invoke(uploadResult)
                } else {
                    Log.e(TAG, "upload media fail ${response.code}")
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                Log.e(TAG, "upload media exception ${ex.message}")
            }
        }.start()
    }

    // url = file path or whatever suitable URL you want.
    private fun getMimeType(file: File): String? {
        var type: String? = null
        val extension = MimeTypeMap.getFileExtensionFromUrl(file.path)
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
        return type
    }

    fun convertRecord(
        source: String, destFileName: String, width: Int, height: Int,
        progressCallBack: ((index: Int, percent: Float) -> Unit)?, function: (result: String) -> Unit
    ) {
        if (source != "") {
            val startTime = System.currentTimeMillis()
            VideoCompressor.start(
                context = MainActivity.appContext!!,
                uris = listOf(File(source).toUri()),
                isStreamable = false,
//                appSpecificStorageConfiguration = AppSpecificStorageConfiguration(
//                    videoName = dest,
//                    subFolderName = DEST_LOCATION_FOLDER
//                ),
                sharedStorageConfiguration = SharedStorageConfiguration(
                    saveAt = SaveLocation.movies,
                    videoName = destFileName
                ),
                configureWith = Configuration(
                    quality = VideoQuality.VERY_HIGH,
                    isMinBitrateCheckEnabled = false,
                    //  videoBitrateInMbps = 2,
                    disableAudio = true,
                    keepOriginalResolution = true, // width == 0 || height == 0,
//                    videoWidth = width.toDouble(),
//                    videoHeight = height.toDouble()
                ),
                listener = object : CompressionListener {
                    override fun onProgress(index: Int, percent: Float) {
                        progressCallBack?.invoke(index, percent)
                        Log.i(TAG, "progress encode::$percent")
                    }

                    override fun onStart(index: Int) {
                        // Compression start
                        Log.i(TAG, "start encode::$width $height::::$destFileName")
                    }

                    override fun onSuccess(index: Int, size: Long, path: String?) {
                        // On Compression success
                        val endTime = System.currentTimeMillis()
                        Log.i(TAG,"save file mp4::::::::::::$path::::::${endTime - startTime}")
                        function.invoke(path ?: "")
                    }

                    override fun onFailure(index: Int, failureMessage: String) {
                        // On Failure
                        Log.e(TAG,"error encode::$failureMessage")
                    }

                    override fun onCancelled(index: Int) {
                        // On Cancelled
                        Log.d(TAG,"cancel encode::$index")
                    }
                }
            )
        }
    }

//    fun videoProcessing(rFile: String, matchID: String) {
//        val retriever = MediaMetadataRetriever()
//        retriever.setDataSource(rFile)
//        val vWidth = retriever.extractMetadata(METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull()
//        val vHeight = retriever.extractMetadata(METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull()
//
//
//        // converter record to mp4
//        if (vWidth != null && vHeight != null) {
//            converterRecord("$matchID.origin.mp4", vWidth, vHeight)
//            if (vHeight > 1080) {
//                converterRecord("$matchID.1080p.mp4", 1920, 1080)
//            }
//            if (vHeight > 720) {
//                converterRecord("$matchID.720p.mp4", 1280, 720)
//            }
//            if (vHeight > 480) {
//                converterRecord("$matchID.480p.mp4", 854, 480)
//            }
//        } else {
//
//        }
//    }
}