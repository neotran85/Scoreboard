package com.bidaappscoreboard.ui.watchMatchAgain

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.bidaappscoreboard.R
import com.bidaappscoreboard.model.ResultMatch
import com.bidaappscoreboard.store.CameraStore
import com.bidaappscoreboard.store.CustomerStore
import com.bidaappscoreboard.store.MatchSoloStore
import kotlinx.android.synthetic.main.item_table_watch_again.view.*
import kotlinx.android.synthetic.main.item_table_watch_again.view.tv_number_turn

class WatchMatchAgainTurnAdapter(
    private var context: Context,
    private var list: MutableList<ResultMatch>,
    private var currentTime: Long
) : Adapter<ViewHolder>() {

    private var TAG = "WatchMatchAgainAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return WatchMatchAgainTurnListHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_table_watch_again, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        when (holder) {
            is WatchMatchAgainTurnListHolder -> holder.bind(list[position], context, position, currentTime)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class WatchMatchAgainTurnListHolder constructor(itemView: View) : ViewHolder(itemView) {

        private val tvScorePlayer1 : TextView = itemView.tv_score_player1
        private val tvScorePlayer2 : TextView = itemView.tv_score_player2
        private val tvNumberTurn : TextView = itemView.tv_number_turn
        private val recordRow1 : ConstraintLayout = itemView.record_row1
        private val recordRow2 : ConstraintLayout = itemView.record_row2
        private val selectedResulkt : ResultMatch;
        @SuppressLint("SetTextI18n")
        fun bind(
            result: ResultMatch,
            context: Context,
            position: Int,
            currentTime: Long
        ) {
            // player 1
            if (result.player1TurnScore == MatchSoloStore.TURN_PROGRESSING) {
                tvScorePlayer1.text = "Đang Tiến Hành"
            } else {
                tvScorePlayer1.text = result.player1TurnScore.toString()

                recordRow1.setOnClic                tvScorePlayer1.text = result.player1TurnScore.toString()

                recordRow1.setOnClickListener {
                    // start playback: if time missing -> playback at begin record
                    CameraStore.playbackRecord(result.time1?: 0, result.time2 ?: 0)
                    //recordRow1.setBackgroundResource(R.drawable.border_table_3_green)
                    selectedResult.isSelected = false;
                    result.isSelected = true;
                    selectedResulkt = resultNew;
                }
                kListener {
                    // start playback: if time missing -> playback at begin record
                    CameraStore.playbackRecord(result.time1?: 0, result.time2 ?: 0)
                    //recordRow1.setBackgroundResource(R.drawable.border_table_3_green)
                    selectedResult.isSelected = false;
                    resultNew.isSelected = true;
                    selectedResulkt = resultNew;
                }
            }
            if(reuslt.isSecled) mau xanh
                    else mau den;
            // player 2
            if (result.player2TurnScore == MatchSoloStore.TURN_PROGRESSING) {
                tvScorePlayer2.text = "Đang Tiến Hành"
            } else if (result.player2TurnScore == MatchSoloStore.TURN_WAITING) {
                tvScorePlayer2.text = "Đang Chờ..."
            } else {
                tvScorePlayer2.text = result.player2TurnScore.toString()

                recordRow2.setOnClickListener {
                    // start playback: if time missing -> playback at begin record
                    CameraStore.playbackRecord(result.time2 ?: 0, result.timeEndTurn ?: 0)
                }
            }

            tvNumberTurn.text = result.numberTurn.toString()
        }
    }
}