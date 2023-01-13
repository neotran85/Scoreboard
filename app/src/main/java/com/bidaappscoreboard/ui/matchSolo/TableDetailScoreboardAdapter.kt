package com.bidaappscoreboard.ui.matchSolo

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bidaappscoreboard.R
import com.bidaappscoreboard.model.ResultMatch
import com.bidaappscoreboard.store.MatchSoloStore
import kotlinx.android.synthetic.main.item_table_detail_scoreboard.view.*
import kotlinx.android.synthetic.main.item_table_result.view.*
import kotlinx.android.synthetic.main.item_table_result.view.tv_number_turn
import kotlinx.android.synthetic.main.item_table_result.view.tv_score_player1
import kotlinx.android.synthetic.main.item_table_result.view.tv_score_player2

class TableDetailScoreboardAdapter(private var context: Context,
                                   private var list: MutableList<ResultMatch>) : Adapter<ViewHolder>() {

    private var TAG = "TableDetailScoreboardAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return TableResultListHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_table_detail_scoreboard, parent, false))
    }

    override fun getItemCount(): Int {
        return createList().size
    }

    private fun createList(): MutableList<ResultMatch> {

        return list
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        when (holder) {
            is TableResultListHolder -> holder.bind(createList()[position], context, position)
        }
    }

    class TableResultListHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvScorePlayer1 : TextView = itemView.tv_score_player1_detail_scoreboard
        private val tvScorePlayer2 : TextView = itemView.tv_score_player2_detail_scoreboard
        private val tvNumberTurn : TextView = itemView.tv_number_turn_detail_scoreboard

        @SuppressLint("ResourceAsColor", "SetTextI18n")
        fun bind(result: ResultMatch, context: Context, position: Int) {

            tvNumberTurn.text = result.numberTurn.toString()

            when (result.player1TurnScore) {
                MatchSoloStore.TURN_PROGRESSING -> {
                    tvScorePlayer1.text = "Đang tiến hành"
                    tvScorePlayer1.setTextColor(ContextCompat.getColor(context, R.color.yellow_light))
                }
                MatchSoloStore.TURN_WAITING -> tvScorePlayer1.text = "Đang chờ..."
                else -> tvScorePlayer1.text = result.player1TurnScore.toString()
            }

            when (result.player2TurnScore) {
                MatchSoloStore.TURN_PROGRESSING -> {
                    tvScorePlayer2.text = "Đang tiến hành"
                    tvScorePlayer2.setTextColor(ContextCompat.getColor(context, R.color.yellow_light))
                }
                MatchSoloStore.TURN_WAITING -> tvScorePlayer2.text = "Đang chờ..."
                else -> tvScorePlayer2.text = result.player2TurnScore.toString()
            }
        }
    }
}