package com.bidaappscoreboard.model

import com.google.gson.annotations.SerializedName

data class MatchScoreBoardData(
    @SerializedName("match"        ) var match       : MatchData                = MatchData(),
    @SerializedName("users"        ) var users       : ArrayList<MatchUser>      = arrayListOf(),
    @SerializedName("score_board"  ) var scoreBoard  : ArrayList<MatchScoreBoard> = arrayListOf(),
    @SerializedName("video_record" ) var videoRecord : MatchVideoRecord?          = MatchVideoRecord()
)

data class MatchData (
    @SerializedName("id" ) var id : String? = null,
    @SerializedName("st" ) var startTime : Long?    = null,
    @SerializedName("et" ) var endTime : Long?    = null,
    @SerializedName("mt" ) var matchType : Int?    = null
)

data class MatchUser (
    @SerializedName("i"  ) var index  : Int?    = null,
    @SerializedName("u"  ) var userID  : String? = null,
    @SerializedName("p"  ) var phone  : String? = null,
    @SerializedName("h"  ) var handy  : Int?    = null,
    @SerializedName("ts" ) var totalScore : Int?    = null
)

data class MatchScoreBoard (
    @SerializedName("i"  ) var userIndex  : Int?    = null,
    @SerializedName("s"  ) var turnScore  : Int? = null,
    @SerializedName("t"  ) var turn  : Int?    = null,
    @SerializedName("ti" ) var time : Long?    = null
)


data class MatchVideoRecord (
    @SerializedName("id"       ) var id       : String? = null,
    @SerializedName("live"     ) var live     : String? = null,
    @SerializedName("thumb"    ) var thumb    : String? = null,
    @SerializedName("download" ) var download : String? = null,
    @SerializedName("duration" ) var duration : Int?    = null
)
