package com.bidaappscoreboard.ui.watchMatchAgain

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bidaappscoreboard.R
import com.bidaappscoreboard.store.CameraStore
import kotlinx.android.synthetic.main.item_table_watch_again_time.view.*

class WatchMatchAgainTimeAdapter(private var context: Context,
                                 private var list: MutableList<Int>,
                                 private var currentTime: Long
) : Adapter<ViewHolder>() {

    private var TAG = "WatchMatchAgainTimeAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return WatchMatchAgainTimeListHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_table_watch_again_time, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        when (holder) {
            is WatchMatchAgainTimeListHolder -> holder.bind(list[position], context, position, currentTime)
        }
    }

    override fun getItemCount(): Int {

        return list.size
    }

    class WatchMatchAgainTimeListHolder constructor(itemView: View) : ViewHolder(itemView) {

        private val tvTime : TextView = itemView.tv_time
        private val recordRow : LinearLayout = itemView.record_row

        // note: currentReplayTime => timestamp begin open playback screen
        fun bind(result: Int, context: Context, position: Int, currentReplayTime: Long) {

            // from playback second -> calculate playbackTimestamps
            var playbackTime: Long = currentReplayTime - (result * 1000)

            // set text
            if (result == 0) {
                // from begin
                tvTime.text = "bắt đầu"
                playbackTime = 0
            } else if (result < 60) {
                // show seconds
                tvTime.text = "$result giây trước"
            } else {
                // show minutes
                val minutes = (result / 60).toInt()
                tvTime.text = "$minutes phút trước"
            }

            // check playback time
            if (playbackTime.toInt() == 0 || CameraStore.recordStartTime <  playbackTime) {
                recordRow.setOnClickListener {
                    CameraStore.playbackRecord(playbackTime, currentReplayTime)
                    // TODO: playback slide for video record
                }
            } else {
                recordRow.visibility = View.GONE
            }
        }
    }
}