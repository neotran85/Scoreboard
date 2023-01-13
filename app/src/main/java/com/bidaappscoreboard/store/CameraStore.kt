package com.bidaappscoreboard.store

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.net.toUri
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.bidaappscoreboard.MainActivity
import com.bidaappscoreboard.model.UploadResult
import com.bidaappscoreboard.service.*
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout
import java.io.File


object CameraStore {
    private const val TAG = "CameraStore"

    private const val KEY_CAMERA_URL = "CAMERA_URL"
    private const val KEY_CAMERA_USER = "CAMERA_USER"
    private const val KEY_CAMERA_PASS = "CAMERA_PASS"
    private const val KEY_CAMERA_PROTOCOL = "CAMERA_PROTOCOL"
    private val MOVIE_LOCATION_FOLDER = "${Environment.getExternalStorageDirectory()}/${Environment.DIRECTORY_MOVIES}/" // "/storage/emulated/0/movies/"
    var recordFile = ""

    const val CAMERA_PROTOCOL_TCP = 0
    const val CAMERA_PROTOCOL_UDP = 1

    private fun preferences(): SharedPreferences {
        val context = MainActivity.appContext
        val masterKeyAlias: String = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
            "secret_shared_prefs",
            masterKeyAlias,
            context!!,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        return sharedPreferences
    }

    // camera
    private lateinit var libVlc: LibVLC
    private lateinit var libVlc2: LibVLC
    private lateinit var mediaRecord: MediaPlayer
    private lateinit var mediaPlayback: MediaPlayer
    var recordStartTime : Long = 0
        public get
        private set
    var playAtSeconds: Double = 0.0
    var stopAtSeconds: Double = 0.0

    fun initCamera(context: Context) {
        if (cameraURL == null) {
            Log.e(TAG, "cameraURL is null")
        }

        val args: ArrayList<String> = ArrayList()
//        args.add("-vvv")
        libVlc = LibVLC(context, args)
        libVlc2 = LibVLC(context, args)
        mediaRecord = MediaPlayer(libVlc)
        mediaPlayback = MediaPlayer(libVlc2)

        // for test
        //         cameraURL = "rtsp://192.168.2.52:554/stream1"
        //         cameraUser = "camerawidosoft2"
        //         cameraPass = "12345678"
        //         cameraProtocol = CAMERA_PROTOCOL_TCP
    }

    fun checkConnect(function: (check: Boolean) -> Unit) {
        if (cameraURL == null) {
            Log.e(TAG, "cameraURL null")
            function.invoke(false)
            return
        }
        val media = Media(libVlc, Uri.parse(cameraURL))
        media.setHWDecoderEnabled(true, false)
        media.addOption(":network-caching=600")
        media.addOption(":rtsp-user=$cameraUser")
        media.addOption(":rtsp-pwd=$cameraPass")
        if (cameraProtocol == CAMERA_PROTOCOL_TCP) media.addOption(":rtsp-tcp")

        // start record
        mediaRecord.stop()
        mediaRecord.play(media)
        media.release()

        mediaRecord.setEventListener {
//            val type = when(it.type) {
//                MediaPlayer.Event.Buffering -> "Buffering"
//                MediaPlayer.Event.EncounteredError -> "EncounteredError"
//                MediaPlayer.Event.EndReached -> "EndReached"
//                MediaPlayer.Event.Playing -> "Playing"
//                MediaPlayer.Event.ESAdded -> "ESAdded"
//                MediaPlayer.Event.ESDeleted -> "ESDeleted"
//                MediaPlayer.Event.ESSelected -> "ESSelected"
//                MediaPlayer.Event.LengthChanged -> "LengthChanged"
//                MediaPlayer.Event.MediaChanged -> "MediaChanged"
//                MediaPlayer.Event.Opening -> "Opening"
//                MediaPlayer.Event.PausableChanged -> "PausableChanged"
//                MediaPlayer.Event.Paused -> "Paused"
//                MediaPlayer.Event.PositionChanged -> "PositionChanged"
//                MediaPlayer.Event.RecordChanged -> "RecordChanged"
//                MediaPlayer.Event.SeekableChanged -> "SeekableChanged"
//                MediaPlayer.Event.Stopped -> "Stopped"
//                MediaPlayer.Event.TimeChanged -> "TimeChanged"
//                MediaPlayer.Event.Vout -> "Vout"
//                else -> "none"
//            }
//            Log.d(TAG, "media record status:::::::$type")

            if (it.type == MediaPlayer.Event.EncounteredError) {
                Log.e(TAG,"camera fail::::::::")
                mediaRecord.stop()
                // clear event
                mediaRecord.setEventListener {}
                function.invoke(false)
            }

            if (it.type == MediaPlayer.Event.Playing) {
                Log.i(TAG,"camera ok::::::::")
                mediaRecord.stop()
                // clear event
                mediaRecord.setEventListener {}
                function.invoke(true)
            }

            if (it.type == MediaPlayer.Event.Stopped) {
                Log.d(TAG,"camera stop::::::::")
            }
        }
    }

    fun startVideoRecord(matchID: String) {
        if (cameraURL == null) {
            Log.e(TAG, "cameraURL null")
            return
        }

        val media = Media(libVlc, Uri.parse(cameraURL))
        media.setHWDecoderEnabled(true, false)
        media.addOption(":network-caching=600")
        media.addOption(":rtsp-user=$cameraUser")
        media.addOption(":rtsp-pwd=$cameraPass")
//        media.addOption(":aout=directsound") // or --no-audio

        // need research why tcp
        if (cameraProtocol == CAMERA_PROTOCOL_TCP) media.addOption(":rtsp-tcp")

        // write file at dest
        recordFile = "$MOVIE_LOCATION_FOLDER$matchID.ts"
        media.addOption(":sout=#duplicate{dst=file{dst=$recordFile},dst=display}")

        Log.d(TAG, "start record file to: $recordFile")

        // start record
        mediaRecord.stop()
        mediaRecord.play(media)
        media.release()

        // mute sound
        mediaRecord.volume = 0

        mediaRecord.setEventListener {
            if (it.type == MediaPlayer.Event.TimeChanged) {
                mediaRecord.setEventListener {} // clear event
                recordStartTime = System.currentTimeMillis()
            }

            if (it.type == MediaPlayer.Event.EncounteredError) {
                deleteRecord(matchID)
                mediaRecord.setEventListener {} // clear event
                mediaRecord.stop()
            }
        }
    }

    fun stopVideoRecord() {
        mediaRecord.stop()
        mediaRecord.detachViews()
        recordStartTime = 0
    }

    fun saveRecord(matchID: String, function: (result: UploadResult?) -> Unit) {
        // validate video is exist (file not exit when camera setting none)
        if (!checkFileExits(MOVIE_LOCATION_FOLDER, "$matchID.ts")) {
            Log.e(TAG, "saveRecord file not found.")
            function.invoke(null)
            return
        }

        // write progress file for check done late
        writeFile(MOVIE_LOCATION_FOLDER, "$matchID.progress", System.currentTimeMillis().toString())

        // TODO: compress mp4 before upload file
        copyFile(
            "$MOVIE_LOCATION_FOLDER",
            "$matchID.ts",
            MOVIE_LOCATION_FOLDER,
            "$matchID.mp4",
            true
        )
        // MediaService.convertRecord("$MOVIE_LOCATION_FOLDER$matchID.ts", "$matchID.mp4", 0, 0 , null) {
        // deleteFile("$MOVIE_LOCATION_FOLDER", "$matchID.ts")
        MediaService.uploadFile(File("$MOVIE_LOCATION_FOLDER$matchID.mp4")) { result ->
            // upload video success => delete origin file + progress
            deleteFile(MOVIE_LOCATION_FOLDER, "$matchID.mp4")
            deleteFile(MOVIE_LOCATION_FOLDER, "$matchID.progress")
            function.invoke(result)
        }
    }

    fun deleteRecord(matchID: String) {
        deleteFile(MOVIE_LOCATION_FOLDER, "$matchID.ts")
    }

    fun attachLiveView(view: VLCVideoLayout) {
        if (mediaRecord.isPlaying) {
            mediaRecord.detachViews()
            mediaRecord.attachViews(view, null, false, true)
        } else {
            // TODO: show video record error
            Log.e(TAG, "TODO: show video record error")
        }
    }

    fun attachPlaybackView(view: VLCVideoLayout, onStarted: (totalMilliSeconds: Long?) -> Unit, onStopped: () -> Unit,  onProgress: (currentMilliSeconds: Long) -> Unit) {
        mediaPlayback.detachViews()
        mediaPlayback.attachViews(view, null, false, false)
        mediaPlayback.setEventListener {
//            val start: Long = 20000
//            val play: Double = 5.0
//            val stop: Double = 20.0
//            val current: Long = 10000
//
//            val begin = start + (play * 1000)
//            val end = start + (stop * 1000)
//
//            val total = end - begin
//            val time = (start + current) - begin
//            val percent = (time / total) * 1000
//
//            println("start:$start play:$play stop:$stop current:$current begin:$begin end:$end")
//            println("total:$total time:$time percent: ${percent.toInt()}")

            if (it.type == MediaPlayer.Event.PositionChanged) {
                val current =  mediaPlayback.time - (playAtSeconds * 1000)
                onProgress.invoke(current.toLong())
            }

            if (it.type == MediaPlayer.Event.Playing) {
                onStarted.invoke(((stopAtSeconds - playAtSeconds) * 1000).toLong())
            }

            if (it.type == MediaPlayer.Event.Paused) {
                onStarted.invoke(null)
            }

            if (it.type == MediaPlayer.Event.Stopped) {
                onStopped.invoke()
            }
        }
    }

    fun playbackRecord(playbackTime: Long, toTime: Long = 0): Double {
        val record = File(recordFile)
        if (!checkFileExits(MOVIE_LOCATION_FOLDER, record.name)) {
            Log.e(TAG, "saveRecord file not found.")
            return 0.0
        }

        // calculate playback begin seconds of video record
        if (playbackTime > recordStartTime) {
            playAtSeconds = ((playbackTime - recordStartTime) / 1000).toDouble()
        } else {
            playAtSeconds = 0.0 // default start at begin video
        }

        val fd = MainActivity.appContext?.contentResolver?.openFileDescriptor(record.toUri() , "r")
        if (fd == null) {
            // TODO: show read file error
            Log.e(TAG, "recordFile read error.")
            return 0.0
        }

        val media = Media(libVlc2, fd!!.fileDescriptor)
        media.setHWDecoderEnabled(true, false)
//        media.addOption(":no-audio")
        media.addOption(":start-time=$playAtSeconds")

        if (toTime > 0 && toTime > playbackTime) {
            stopAtSeconds = ((toTime - recordStartTime) / 1000).toDouble()
            media.addOption(":stop-time=$stopAtSeconds")
        }

        mediaPlayback.stop()
        mediaPlayback.media = media
        media.release()
        mediaPlayback.play()

        Log.d(TAG, "playback start: ($playbackTime $toTime) - $recordStartTime $playAtSeconds $stopAtSeconds - ${stopAtSeconds - playAtSeconds}s")

        return stopAtSeconds - playAtSeconds
    }

    fun stopPlayback() {
        mediaPlayback.stop()
    }

    fun pausePlayback() {
        mediaPlayback.pause()
    }

    fun resumePlayback() {
        mediaPlayback.play()
    }

    fun reloadPlayback() {
        if (mediaPlayback.isPlaying) {
            mediaPlayback.stop()
        }

        val startTime = (playAtSeconds * 1000).toLong() + recordStartTime
        val endTime = (stopAtSeconds * 1000).toLong() + recordStartTime

        playbackRecord(startTime, endTime)
    }

    fun changePlaybackTime(startMilliseconds: Int, force: Boolean = false) {
        mediaPlayback.time = startMilliseconds.toLong() + (playAtSeconds * 1000).toLong()
    }

    // setting
    var lastError: RuntimeException? = null
    var cameraURL: String?
        get() {
            return preferences().getString(KEY_CAMERA_URL, null)
        }
        set(token: String?) {
            preferences().edit().apply {
                putString(KEY_CAMERA_URL, token)
                apply()
            }
        }
    var cameraUser: String?
        get() {
            return preferences().getString(KEY_CAMERA_USER, null)
        }
        set(token: String?) {
            preferences().edit().apply {
                putString(KEY_CAMERA_USER, token)
                apply()
            }
        }

    var cameraPass: String?
        get() {
            return preferences().getString(KEY_CAMERA_PASS, null)
        }
        set(token: String?) {
            preferences().edit().apply {
                putString(KEY_CAMERA_PASS, token)
                apply()
            }
        }

    var cameraProtocol: Int?
        get() {
            return preferences().getInt(KEY_CAMERA_PROTOCOL, CAMERA_PROTOCOL_TCP)
        }
        set(value: Int?) {
            preferences().edit().apply {
                if (value != null) {
                    putInt(KEY_CAMERA_PROTOCOL, value)
                }
                apply()
            }
        }
}