package com.bidaappscoreboard.ui.matchSolo

import android.annotation.SuppressLint
import android.content.Context
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
import kotlinx.android.synthetic.main.item_table_result.view.*

class TableResultAdapter(private var context: Context,
                         private var list: MutableList<ResultMatch>,
                         private var endMatch: Boolean ) : Adapter<ViewHolder>() {

    private var TAG = "TableResultAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return TableResultListHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_table_result, parent, false))
    }

    private fun <T> concatenate(vararg lists: MutableList<T>): MutableList<T> {
        return mutableListOf(*lists).flatten().toMutableList()
    }

    override fun getItemCount(): Int {
        return createList().size
    }

    private fun createList(): MutableList<ResultMatch> {

        return list
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        when (holder) {
            is TableResultListHolder -> holder.bind(createList()[position], context, position, endMatch)
        }
    }

    class TableResultListHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvScorePlayer1 : TextView = itemView.tv_score_player1
        private val tvScorePlayer2 : TextView = itemView.tv_score_player2
        private val tvNumberTurn : TextView = itemView.tv_number_turn

        @SuppressLint("ResourceAsColor", "SetTextI18n")
        fun bind(result: ResultMatch, context: Context, position: Int, endMatch: Boolean) {

            if (!endMatch && position == 0) {
                tvScorePlayer1.setBackgroundResource(R.drawable.border_table)
                tvScorePlayer2.setBackgroundResource(R.drawable.border_table)
                tvNumberTurn.setBackgroundResource(R.drawable.border_table_2)
            }

            if (result.player1TurnScore == -1) {
                tvScorePlayer1.setTextColor(ContextCompat.getColor(context, R.color.yellow_light))
            }

            if (result.player2TurnScore == -1) {
                tvScorePlayer2.setTextColor(ContextCompat.getColor(context, R.color.yellow_light))
            }

            if (result.numberTurn == MatchSoloStore.TURN_AVG_TITLE || result.numberTurn == MatchSoloStore.TURN_HR_TITLE || result.numberTurn == MatchSoloStore.TOTAL_TITLE) {
                tvNumberTurn.setTextColor(ContextCompat.getColor(context, R.color.greenButtonLogin))
            }

            if (result.player1TurnScore == 0) {
                tvScorePlayer1.text = "-"
            } else {
                when (result.player1TurnScore) {
                    MatchSoloStore.TURN_PROGRESSING -> tvScorePlayer1.text = "Đang tiến hành"
                    MatchSoloStore.TURN_WAITING -> tvScorePlayer1.text = "Đang chờ..."
                    MatchSoloStore.TURN_AVG -> tvScorePlayer1.text = (MatchSoloStore.arrayProfilePlayer[MatchSoloStore.arrayLocationProfilePlayer[0]].avg).toString()
                    MatchSoloStore.TURN_HR -> tvScorePlayer1.text = (MatchSoloStore.arrayProfilePlayer[MatchSoloStore.arrayLocationProfilePlayer[1]].hr).toString()
                    else -> tvScorePlayer1.text = result.player1TurnScore.toString()
                }
            }

            if (result.player2TurnScore == 0) {
                tvScorePlayer2.text = "-"
            } else {
                when (result.player2TurnScore) {
                    MatchSoloStore.TURN_PROGRESSING -> tvScorePlayer2.text = "Đang tiến hành"
                    MatchSoloStore.TURN_WAITING -> tvScorePlayer2.text = "Đang chờ..."
                    MatchSoloStore.TURN_AVG -> tvScorePlayer2.text = (MatchSoloStore.arrayProfilePlayer[MatchSoloStore.arrayLocationProfilePlayer[0]].avg).toString()
                    MatchSoloStore.TURN_HR -> tvScorePlayer2.text = (MatchSoloStore.arrayProfilePlayer[MatchSoloStore.arrayLocationProfilePlayer[1]].hr).toString()
                    else -> tvScorePlayer2.text = result.player2TurnScore.toString()
                }
            }

            when (result.numberTurn) {
                MatchSoloStore.TURN_AVG_TITLE -> tvNumberTurn.text = "AVG"
                MatchSoloStore.TURN_HR_TITLE -> tvNumberTurn.text = "HR"
                MatchSoloStore.TOTAL_TITLE -> tvNumberTurn.text = "TOTAL"
                else -> tvNumberTurn.text = result.numberTurn.toString()
            }
        }
    }
}