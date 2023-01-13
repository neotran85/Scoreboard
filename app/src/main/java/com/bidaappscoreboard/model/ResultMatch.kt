package com.bidaappscoreboard.model

import com.google.gson.annotations.SerializedName

data class ResultMatch (
   @SerializedName("player1TurnScore")
   var player1TurnScore : Any? = null,

   @SerializedName("numberTurn")
   var numberTurn: Int? = null,

   @SerializedName("player2TurnScore")
   var player2TurnScore: Any? = null,
   @SerializedName("time1")
   var time1: Long? = null, // time begin turn player 1

   @SerializedName("time2")
   var time2: Long? = null, // time begin turn player 2

   var timeEndTurn: Long? = null, // time end turn
)

data class ScoreBoard (
   @SerializedName("i")
   var userIndex : Int? = null,

   @SerializedName("s")
   var score : Int? = null,

   @SerializedName("t")
   var turn: Int? = null,

   @SerializedName("ti")
   var time: Long? = null
)