package com.bidaappscoreboard.model

import com.google.gson.annotations.SerializedName

data class Profile (

    @SerializedName("displayname")
    var displayname : String? = null,

    @SerializedName("handy")
    var handy: Int = 0,

    @SerializedName("avatar_url")
    var url: String? = null,

    @SerializedName("avg")
    var avg: Double? = null,

    @SerializedName("hr")
    var hr: Int? = null,

    @SerializedName("rank")
    var rank: Int? = null,

    @SerializedName("phone")
    var phone: String? = null
)