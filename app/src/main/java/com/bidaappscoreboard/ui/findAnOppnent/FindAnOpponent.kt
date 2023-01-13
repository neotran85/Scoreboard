package com.bidaappscoreboard.ui.findAnOppnent

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.bidaappscoreboard.R
import com.bidaappscoreboard.common.CustomSpinner
import com.bidaappscoreboard.common.HideKeyboard.hideKeyboard
import com.bidaappscoreboard.common.SetImage
import com.bidaappscoreboard.model.Profile
import com.bidaappscoreboard.store.MatchSoloStore
import com.bidaappscoreboard.ui.HomeScreen
import com.bidaappscoreboard.ui.matchSolo.ChosePlayer
import com.bidaappscoreboard.ui.matchSolo.WaitMatchScreen
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_chose_player.*
import kotlinx.android.synthetic.main.activity_find_an_opponent.*
import kotlinx.android.synthetic.main.activity_match_solo_screen.*
import kotlinx.android.synthetic.main.custom_popup_edit_handy.*
import android.text.Editable
import android.text.Selection
import com.bidaappscoreboard.ui.matchSolo.MatchSoloScreen


class FindAnOpponent : AppCompatActivity(), CustomSpinner.OnSpinnerEventsListener {

    private val TAG = "FindAnOpponent"

    private var establishLimitSpinner: CustomSpinner? = null
    private var timeMatchSpinner: CustomSpinner? = null
    private var timeTurnSpinner: CustomSpinner? = null
    private var adapterLimitEstablish: LimitEstablishAdapter? = null
    private var adapterTimeMatch: LimitEstablishAdapter? = null
    private var adapterTimeTurn: LimitEstablishAdapter? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_an_opponent)

        if (MatchSoloStore.nameModel == "CHƠI PHĂNG") {
            tv_name_model.text = MatchSoloStore.nameModel
        } else {
            tv_name_model.text = "CAROM ${MatchSoloStore.nameModel}"
        }

        init()
    }

    override fun onCreateView(
        parent: View?,
        name: String,
        context: Context,
        attrs: AttributeSet
    ): View? {

        return super.onCreateView(parent, name, context, attrs)
    }

    @SuppressLint("SetTextI18n")
    private fun init() {
        chosePlayer()
        cancelMatch()
        setupPlayer()
        changePlayer()
        openEstablish()
        openInfoPlayer()
        changeLocation()
        startMatch()
        resetHandy()

        contain_establish.visibility = View.GONE

        tv_handy_1.text = MatchSoloStore.arrayHandy[MatchSoloStore.arrayLocationProfilePlayer[0]].toString()
        tv_handy_2.text = MatchSoloStore.arrayHandy[MatchSoloStore.arrayLocationProfilePlayer[1]].toString()

        contain_change_ball.setOnClickListener {
            if (MatchSoloStore.arrayChoseBall[0] == "white") {
                MatchSoloStore.arrayChoseBall = arrayListOf("yellow", "white")
            } else {
                MatchSoloStore.arrayChoseBall = arrayListOf("white", "yellow")
            }

            showColorBallPlayer()
        }
    }

    override fun onBackPressed() {
        MatchSoloStore.arrayProfilePlayer = arrayListOf(
            Profile("", 25, "", 0.0, 0, 0),
            Profile("", 25, "", 0.0, 0, 0)
        )
        val intent = Intent(this, MatchSoloScreen::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun resetHandy() {
        MatchSoloStore.arrayHandy = arrayListOf(20, 20)
    }

    private fun changeLocation() {
        contain_change_location.setOnClickListener {
                if (MatchSoloStore.arrayLocationProfilePlayer[0] == 0) {
                    MatchSoloStore.arrayLocationProfilePlayer = arrayListOf(1, 0)
                } else {
                    MatchSoloStore.arrayLocationProfilePlayer = arrayListOf(0, 1)
                }

                setupPlayer()
                openInfoPlayer()
                tv_handy_1.text = MatchSoloStore.arrayHandy[MatchSoloStore.arrayLocationProfilePlayer[0]].toString()
                tv_handy_2.text = MatchSoloStore.arrayHandy[MatchSoloStore.arrayLocationProfilePlayer[1]].toString()
        }
    }

    private fun showColorBallPlayer() {
        if (MatchSoloStore.arrayChoseBall[0] == "white") {
            constrain_player_1.setBackgroundResource(R.drawable.custom_edittext_1)
            constrain_player_2.setBackgroundResource(R.drawable.custom_background_player_2)
        } else {
            constrain_player_1.setBackgroundResource(R.drawable.custom_background_player_2)
            constrain_player_2.setBackgroundResource(R.drawable.custom_edittext_1)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun openInfoPlayer() {
        val dialog = Dialog(this@FindAnOpponent)
        dialog.setContentView(R.layout.custom_popup_show_detail_player)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val avatarPlayer1 = dialog.findViewById<CircleImageView>(R.id.avatar_player_1)
        val avatarPlayer2 = dialog.findViewById<CircleImageView>(R.id.avatar_player_2)

        val tvNamePlayer1 = dialog.findViewById<TextView>(R.id.tv_name_player_1)
        val tvNamePlayer2 = dialog.findViewById<TextView>(R.id.tv_name_player_2)
        val tvAvgPlayer1 = dialog.findViewById<TextView>(R.id.tv_avg_player_1)
        val tvAvgPlayer2 = dialog.findViewById<TextView>(R.id.tv_avg_player_2)
        val tvHrPlayer1 = dialog.findViewById<TextView>(R.id.tv_hr_player_1)
        val tvHrPlayer2 = dialog.findViewById<TextView>(R.id.tv_hr_player_2)
        val tvRankPlayer1 = dialog.findViewById<TextView>(R.id.tv_rank_player_1)
        val tvRankPlayer2 = dialog.findViewById<TextView>(R.id.tv_rank_player_2)

        val btnIcClose = dialog.findViewById<ImageView>(R.id.ic_close_detail_player)
        val btnCancelDetailPlayer = dialog.findViewById<Button>(R.id.btnCancelDetailPlayer)

        SetImage.setImage(avatarPlayer1, MatchSoloStore.arrayProfilePlayer[MatchSoloStore.arrayLocationProfilePlayer[0]].url, this)
        SetImage.setImage(avatarPlayer2, MatchSoloStore.arrayProfilePlayer[MatchSoloStore.arrayLocationProfilePlayer[1]].url, this)

        //Set name
        tvNamePlayer1.text = MatchSoloStore.arrayProfilePlayer[MatchSoloStore.arrayLocationProfilePlayer[0]].displayname
        tvNamePlayer2.text = MatchSoloStore.arrayProfilePlayer[MatchSoloStore.arrayLocationProfilePlayer[1]].displayname

        //Set avg
        tvAvgPlayer1.text = MatchSoloStore.arrayProfilePlayer[MatchSoloStore.arrayLocationProfilePlayer[0]].avg.toString()
        tvAvgPlayer2.text = MatchSoloStore.arrayProfilePlayer[MatchSoloStore.arrayLocationProfilePlayer[1]].avg.toString()

        //Set hr
        tvHrPlayer1.text = MatchSoloStore.arrayProfilePlayer[MatchSoloStore.arrayLocationProfilePlayer[0]].hr.toString()
        tvHrPlayer2.text = MatchSoloStore.arrayProfilePlayer[MatchSoloStore.arrayLocationProfilePlayer[1]].hr.toString()

        //Set rank
        tvRankPlayer1.text = "${MatchSoloStore.arrayProfilePlayer[MatchSoloStore.arrayLocationProfilePlayer[0]].rank}th"
        tvRankPlayer2.text = "${MatchSoloStore.arrayProfilePlayer[MatchSoloStore.arrayLocationProfilePlayer[1]].rank}th"

        //Close dialog
        btnIcClose.setOnClickListener {
            dialog.dismiss()
        }
        btnCancelDetailPlayer.setOnClickListener {
            dialog.dismiss()
        }

        contain_detail_info_player.setOnClickListener {
            dialog.show()
        }
    }

    @SuppressLint("CutPasteId")
    private fun openEstablish() {
        val dialog = Dialog(this@FindAnOpponent)
        dialog.setContentView(R.layout.custom_popup_edit_establish)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        establishLimitSpinner = dialog.findViewById<Spinner>(R.id.spinner_limit_turn) as CustomSpinner
        timeMatchSpinner = dialog.findViewById<Spinner>(R.id.spinner_time_match) as CustomSpinner
        timeTurnSpinner = dialog.findViewById<Spinner>(R.id.spinner_time_turn) as CustomSpinner

        val btnCancel = dialog.findViewById<Button>(R.id.btnCancelEstablish)
        val btnSave = dialog.findViewById<Button>(R.id.btnSaveEstablish)
        val icClose = dialog.findViewById<ImageView>(R.id.ic_close_edit_establish)

        val listLimitEstablish: List<String> = listOf("Không giới hạn", "1", "2", "3")
        val listTimeMatch: List<String> = listOf("Không giới hạn", "01:00", "02:00", "03:00")
        val listTimeTurn: List<String> = listOf("40 giây", "50 giây", "60 giây", "70 giây")

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnSave.setOnClickListener {
            dialog.dismiss()
        }

        icClose.setOnClickListener {
            dialog.dismiss()
        }

        establishLimitSpinner!!.setSpinnerEventsListener(this@FindAnOpponent)
        timeMatchSpinner!!.setSpinnerEventsListener(this@FindAnOpponent)
        timeTurnSpinner!!.setSpinnerEventsListener(this@FindAnOpponent)

        adapterLimitEstablish = LimitEstablishAdapter(this@FindAnOpponent, listLimitEstablish)
        adapterTimeMatch = LimitEstablishAdapter(this@FindAnOpponent, listTimeMatch)
        adapterTimeTurn = LimitEstablishAdapter(this@FindAnOpponent, listTimeTurn)

        establishLimitSpinner!!.adapter = adapterLimitEstablish
        timeMatchSpinner!!.adapter = adapterTimeMatch
        timeTurnSpinner!!.adapter = adapterTimeTurn

        contain_establish.setOnClickListener {
            dialog.show()
        }
    }

    private fun changePlayer() {
        btn_change_player1.setOnClickListener {
            val intent = Intent(this, ChosePlayer::class.java)
            intent.putExtra("ChangePlayer", true)
            startActivity(intent)
        }

        btn_change_player2.setOnClickListener {
            val intent = Intent(this, ChosePlayer::class.java)
            intent.putExtra("ChangePlayer", true)
            startActivity(intent)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupPlayer() {
        //Log.d(TAG, "Player Number :: ${MatchSoloStore.arrayStatusSetupPlayer}")

        val dialog = Dialog(this@FindAnOpponent )
        dialog.setContentView(R.layout.custom_popup_edit_handy)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnSaveHandy = dialog.findViewById<Button>(R.id.btn_save_edit_handy)
        val btnCancelHandy = dialog.findViewById<Button>(R.id.btn_cancel_edit_handy)
        val edtHandy = dialog.findViewById<EditText>(R.id.edt_handy)
        val titleHandy = dialog.findViewById<TextView>(R.id.tv_title_handy)

        //Setup player 1
        if(MatchSoloStore.arrayProfilePlayer[MatchSoloStore.arrayLocationProfilePlayer[0]].displayname?.isNotEmpty() == true) {
            SetImage.setImage(avatar1_find_opponent, MatchSoloStore.arrayProfilePlayer[MatchSoloStore.arrayLocationProfilePlayer[0]].url, this)
            tvNamePlayer1.text = MatchSoloStore.arrayProfilePlayer[MatchSoloStore.arrayLocationProfilePlayer[0]].displayname
            ic_question1.visibility = View.INVISIBLE

            tv_handy_1.setOnClickListener {

                titleHandy.text = "ĐIỂM TỚI PLAYER 1"
                edtHandy.setText(MatchSoloStore.arrayHandy[MatchSoloStore.arrayLocationProfilePlayer[0]].toString())
                val position: Int = edtHandy.length()
                val eText: Editable = edtHandy.text
                Selection.setSelection(eText, position)
                dialog.show()

                btnSaveHandy.setOnClickListener {
                    tv_handy_1.text = edtHandy.text
                    MatchSoloStore.arrayHandy[MatchSoloStore.arrayLocationProfilePlayer[0]] = edtHandy.text.toString().toInt() ?: 0
                    dialog.dismiss()
                }
                btnCancelHandy.setOnClickListener {
                    dialog.dismiss()
                }
            }
            tv_handy_1.visibility = View.VISIBLE
            tv_choose_player1.visibility = View.INVISIBLE
            btn_change_player1.visibility = View.VISIBLE
        } else {
            avatar1_find_opponent.setImageResource(R.color.black75)
            ic_question1.visibility = View.VISIBLE
            tv_handy_1.visibility = View.GONE
            tv_choose_player1.visibility = View.VISIBLE
            tvNamePlayer1.text = "PLAYER 1"
            btn_change_player1.visibility = View.INVISIBLE
        }

        //Setup player 2
        if(MatchSoloStore.arrayProfilePlayer[MatchSoloStore.arrayLocationProfilePlayer[1]].displayname?.isNotEmpty() == true) {
            SetImage.setImage(avatar2_find_opponent, MatchSoloStore.arrayProfilePlayer[MatchSoloStore.arrayLocationProfilePlayer[1]].url, this)
            tvNamePlayer2.text = MatchSoloStore.arrayProfilePlayer[MatchSoloStore.arrayLocationProfilePlayer[1]].displayname
            ic_question2.visibility = View.INVISIBLE
            tv_handy_2.setOnClickListener {

                titleHandy.text = "ĐIỂM TỚI PLAYER 2"
                edtHandy.setText(MatchSoloStore.arrayHandy[MatchSoloStore.arrayLocationProfilePlayer[1]].toString())
                val position: Int = edtHandy.length()
                val eText: Editable = edtHandy.text
                Selection.setSelection(eText, position)
                dialog.show()

                btnSaveHandy.setOnClickListener {
                    tv_handy_2.text = edtHandy.text
                    MatchSoloStore.arrayHandy[MatchSoloStore.arrayLocationProfilePlayer[1]] = edtHandy.text.toString().toString().toInt() ?: 0
                    dialog.dismiss()
                }
                btnCancelHandy.setOnClickListener {
                    dialog.dismiss()
                }
            }
            tv_handy_2.visibility = View.VISIBLE
            tv_choose_player2.visibility = View.INVISIBLE
            btn_change_player2.visibility = View.VISIBLE
        } else {
            avatar2_find_opponent.setImageResource(R.color.black75)
            ic_question2.visibility = View.VISIBLE
            tv_handy_2.visibility = View.GONE
            tv_choose_player2.visibility = View.VISIBLE
            tvNamePlayer2.text = "PLAYER 2"
            btn_change_player2.visibility = View.INVISIBLE
        }
    }

    private fun chosePlayer() {
        constrain_player_1.setOnClickListener {
            if (MatchSoloStore.arrayProfilePlayer[MatchSoloStore.arrayLocationProfilePlayer[0]].displayname == "") {
                if (MatchSoloStore.arrayLocationProfilePlayer[0] == 0) {
                    MatchSoloStore.getPlayer1()
                    MatchSoloStore.arrayProfilePlayer[MatchSoloStore.arrayLocationProfilePlayer[0]] = MatchSoloStore.player1Result
                } else {
                    MatchSoloStore.getPlayer2()
                    MatchSoloStore.arrayProfilePlayer[MatchSoloStore.arrayLocationProfilePlayer[0]] = MatchSoloStore.player2Result
                }

                val intent = Intent(this, ChosePlayer::class.java)
                intent.putExtra("NumberPlayer", MatchSoloStore.arrayLocationProfilePlayer[0])
                Log.d(TAG, "List location :: ${MatchSoloStore.arrayLocationProfilePlayer}")
                startActivity(intent)
            }
        }

        constrain_player_2.setOnClickListener {
            if (MatchSoloStore.arrayProfilePlayer[MatchSoloStore.arrayLocationProfilePlayer[1]].displayname == "") {
                if (MatchSoloStore.arrayLocationProfilePlayer[1] == 0) {
                    MatchSoloStore.getPlayer1()
                    MatchSoloStore.arrayProfilePlayer[MatchSoloStore.arrayLocationProfilePlayer[1]] = MatchSoloStore.player1Result
                } else {
                    MatchSoloStore.getPlayer2()
                    MatchSoloStore.arrayProfilePlayer[MatchSoloStore.arrayLocationProfilePlayer[1]] = MatchSoloStore.player2Result
                }

                val intent = Intent(this, ChosePlayer::class.java)
                intent.putExtra("NumberPlayer", MatchSoloStore.arrayLocationProfilePlayer[1])
                startActivity(intent)
            }
        }
    }

    private fun startMatch() {
        btnStart.setOnClickListener {
            if(MatchSoloStore.arrayProfilePlayer[0].displayname!!.isNotEmpty() && MatchSoloStore.arrayProfilePlayer[1].displayname!!.isNotEmpty()) {
                val intent = Intent(this, WaitMatchScreen::class.java)
                startActivity(intent)
            }
        }
    }

    private fun cancelMatch() {
        val dialog = Dialog(this@FindAnOpponent)
        dialog.setContentView(R.layout.custom_popup_cancel_match)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val containCloseDialog = dialog.findViewById<ConstraintLayout>(R.id.contain_close_cancel_match)
        val containConfirmCancelMatch = dialog.findViewById<ConstraintLayout>(R.id.contain_confirm_cancel_match)

        //Confirm cancel match
        containConfirmCancelMatch.setOnClickListener {
            dialog.dismiss()
            MatchSoloStore.arrayProfilePlayer = arrayListOf(
                Profile("", 25, "", 0.0, 0, 0),
                Profile("", 25, "", 0.0, 0, 0)
            )
            val intent = Intent(this, HomeScreen::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        //Close dialog
        containCloseDialog.setOnClickListener {
            dialog.dismiss()
        }

        //Show dialog
        contain_cancel_match.setOnClickListener {
            dialog.show()
        }
    }

    override fun onPopupWindowOpened(spinner: Spinner?) {
        establishLimitSpinner?.setBackgroundResource(R.drawable.background_spinner_bottom)
        timeMatchSpinner?.setBackgroundResource(R.drawable.background_spinner_bottom)
        timeTurnSpinner?.setBackgroundResource(R.drawable.background_spinner_bottom)
    }

    override fun onPopupWindowClosed(spinner: Spinner?) {
        establishLimitSpinner?.setBackgroundResource(R.drawable.background_spinner_right)
        timeMatchSpinner?.setBackgroundResource(R.drawable.background_spinner_right)
        timeTurnSpinner?.setBackgroundResource(R.drawable.background_spinner_right)
    }
}