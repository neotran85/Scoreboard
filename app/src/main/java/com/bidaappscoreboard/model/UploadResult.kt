package com.bidaappscoreboard.model

import com.google.gson.annotations.SerializedName

data class UploadResult (
    @SerializedName("status"  ) var status  : Int?    = null,
    @SerializedName("message" ) var message : String? = null,
    @SerializedName("data"    ) var data    : UploadData?   = UploadData(),
    @SerializedName("metadata") var metadata: UploadMetadata?   = UploadMetadata()
)

data class UploadData (
    @SerializedName("thumb_url"  ) var thumbUrl  : String? = null,
    @SerializedName("origin_url" ) var originUrl : String? = null,
    @SerializedName("live_url"   ) var liveUrl   : String? = null,
    @SerializedName("upload_id"  ) var uploadId  : String? = null
)

data class UploadMetadata (
    @SerializedName("name"    ) var name  : String? = null,
    @SerializedName("width"   ) var width  : Int? = null,
    @SerializedName("height"  ) var height : Int? = null,
    @SerializedName("duration") var duration : Long? = null,
    // fps - original capture framerate
    @SerializedName("frame_rate") var frameRate : Double? = null,
    // average bitrate (in bits/sec)
    @SerializedName("bit_rate") var bitRate : Int? = null,
)
