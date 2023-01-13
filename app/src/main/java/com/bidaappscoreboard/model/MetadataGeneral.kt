package com.bidaappscoreboard.model

import com.google.gson.annotations.SerializedName

data class MetadataGeneral (
    @SerializedName("GENERAL_OPTIONS") var generalOptions: GeneralOptions? = null
) {}

data class GeneralOptions (
    @SerializedName("default_avatar_female") var default_avatar_female: String? = null,
    @SerializedName("default_avatar_male") var default_avatar_male: String? = null
) {}
