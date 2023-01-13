package com.bidaappscoreboard.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import com.bidaappscoreboard.CustomerPlaceQuery
import com.bidaappscoreboard.R
import com.bidaappscoreboard.common.CustomSpinner
import com.bidaappscoreboard.common.HideKeyboard.hideKeyboard
import com.bidaappscoreboard.store.CameraStore
import com.bidaappscoreboard.store.CustomerStore
import com.bidaappscoreboard.store.HttpHeaders
import com.bidaappscoreboard.store.ScoreBoard
import kotlinx.android.synthetic.main.activity_login_screen.*
import kotlinx.android.synthetic.main.activity_register_screen.*
import kotlinx.android.synthetic.main.activity_setting_screen.*
import kotlinx.android.synthetic.main.custom_popup_connect_notice.*
import kotlinx.android.synthetic.main.custom_popup_edit_device.*
import kotlinx.android.synthetic.main.custom_popup_pass_protect.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SettingScreen : AppCompatActivity(), CustomSpinner.OnSpinnerEventsListener {

    private val TAG = "SettingScreen"

    private var adapter: EstablishmentAdapter? = null
    private var establishmentSpinner: CustomSpinner? = null
    private var choseProtocol: Array<Boolean> = arrayOf(false, false)
    private var statusConnectCamera = false
    private var checkInfoSetting : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_screen)

        constrainShowPass.visibility = View.INVISIBLE

        val callFirst = intent.getBooleanExtra("CallFirst", false)

        if (callFirst) {
            containBackButtonSetting.visibility = View.INVISIBLE
        } else {
            containBackButtonSetting.visibility = View.VISIBLE
        }

        init(callFirst)
    }

    private fun init(callFirst: Boolean) {
        editEstablishmentName()
        editPasswordProtection()
        getTextIDAndUID()
        activePasswordProtection()
        setupLogout()
        setupCamera()
        showSetupCameraWhenConnectSuccess()

        dataNameDevice.text = ScoreBoard.nameDevice
        dataEstablishmentName.text = ScoreBoard.nameEstablishment

        containBackButtonSetting.setOnClickListener {
            if (callFirst) {
                val intent = Intent(this@SettingScreen, HomeScreen::class.java)
                startActivity(intent)
            } else {
                this.finish()
            }
        }

        dataShowPass.text = ScoreBoard.passwordProtect
        Log.d(TAG, "Device name :: ${Settings.Secure.getString(contentResolver, "bluetooth_name")}")
    }

    private fun showSetupCameraWhenConnectSuccess() {
        if (statusConnectCamera) {
            contain_setup_before_connect_camera.visibility = View.GONE
            contain_setup_after_connect_camera.visibility = View.VISIBLE
        } else {
            contain_setup_before_connect_camera.visibility = View.VISIBLE
            contain_setup_after_connect_camera.visibility = View.GONE
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupCamera() {

        // Create dialog setup camera
        val dialog = Dialog(this@SettingScreen)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(R.layout.custom_popup_setup_camera)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Create dialog notice connect camera
        val dialogNoticeConnect = Dialog(this@SettingScreen)
        dialogNoticeConnect.setContentView(R.layout.custom_popup_connect_notice)
        dialogNoticeConnect.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Dialog setup camera
        val icCheckTCP = dialog.findViewById<ImageView>(R.id.ic_checkbox_tcp)
        val icCheckUDP = dialog.findViewById<ImageView>(R.id.ic_checkbox_udp)
        val btnCancelSetupCamera = dialog.findViewById<Button>(R.id.btn_cancel_setup_camera)
        val btnSaveSetupCamera = dialog.findViewById<Button>(R.id.btn_save_setup_camera)
        val icCloseSetupCamera = dialog.findViewById<ImageView>(R.id.ic_close_setup_camera)

        val cameraLink = dialog.findViewById<EditText>(R.id.tv_link)
        val cameraUser = dialog.findViewById<EditText>(R.id.camera_id)
        val cameraPassword = dialog.findViewById<EditText>(R.id.camera_password)

        // Dialog notice connect camera
        val btnCancelNotice = dialogNoticeConnect.findViewById<Button>(R.id.btn_close_notice_setup_camera)
        val icWarnings = dialogNoticeConnect.findViewById<ImageView>(R.id.ic_warning_connect_camera)
        val icWarningSetting = ic_warning_setting
        val tvTitleStatusConnect = dialogNoticeConnect.findViewById<TextView>(R.id.tv_title_status_connect)
        tv_status_connect.text = "Checking..."

        GlobalScope.launch {
            CameraStore.checkConnect() {
                statusConnectCamera = it

                //Show notice when connect camera
                if (!statusConnectCamera) {
                    icWarnings.setImageResource(R.drawable.ic_warning_3)
                    tvTitleStatusConnect.text = "Kết nối không thành công"
                    tv_status_connect.text = "Chưa có kết nối"
                    icWarningSetting.setImageResource(R.drawable.ic_warning_2)
                } else {
                    icWarnings.setImageResource(R.drawable.ic_check_2)
                    tvTitleStatusConnect.text = "Kết nối thành công"
                    tv_status_connect.text = "Đã có kết nối"
                    icWarningSetting.setImageResource(R.drawable.ic_check_2)
                }
            }
        }

        // get value from local
        if (CameraStore.cameraProtocol == CameraStore.CAMERA_PROTOCOL_TCP) {
            choseProtocol = arrayOf(true, false)
        } else if (CameraStore.cameraProtocol == CameraStore.CAMERA_PROTOCOL_UDP) {
            choseProtocol = arrayOf(false, true)
        }

        cameraLink.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                v.hideKeyboard()
            }
        }

        cameraUser.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                v.hideKeyboard()
            }
        }

        cameraPassword.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                v.hideKeyboard()
            }
        }

        cameraLink.setText(CameraStore.cameraURL)
        cameraUser.setText(CameraStore.cameraUser)
        cameraPassword.setText(CameraStore.cameraPass)

        //Choose protocol connect camera
        icCheckTCP.setOnClickListener {
            choseProtocol = arrayOf(true, false)
            checkStatusProtocol(icCheckTCP, icCheckUDP)
        }
        icCheckUDP.setOnClickListener {
            choseProtocol = arrayOf(false, true)
            checkStatusProtocol(icCheckTCP, icCheckUDP)
        }
        checkStatusProtocol(icCheckTCP, icCheckUDP)
        btnCancelSetupCamera.setOnClickListener {
            dialog.dismiss()
        }
        icCloseSetupCamera.setOnClickListener {
            dialog.dismiss()
        }

        btnCancelNotice.setOnClickListener {
            dialogNoticeConnect.dismiss()
        }

        btnSaveSetupCamera.setOnClickListener {
            if (choseProtocol.contentEquals(arrayOf(true, false))) {
                CameraStore.cameraProtocol = CameraStore.CAMERA_PROTOCOL_TCP
            } else {
                CameraStore.cameraProtocol = CameraStore.CAMERA_PROTOCOL_UDP
            }
            CameraStore.cameraURL = cameraLink.text.toString()
            CameraStore.cameraUser = cameraUser.text.toString()
            CameraStore.cameraPass = cameraPassword.text.toString()

            CameraStore.checkConnect() {
                statusConnectCamera = it

                //Show notice when connect camera
                if (!statusConnectCamera) {
                    icWarnings.setImageResource(R.drawable.ic_warning_3)
                    tvTitleStatusConnect.text = "Kết nối không thành công"
                    tv_status_connect.text = "Chưa có kết nối"
                    icWarningSetting.setImageResource(R.drawable.ic_warning_2)
                } else {
                    icWarnings.setImageResource(R.drawable.ic_check_2)
                    tvTitleStatusConnect.text = "Kết nối thành công"
                    tv_status_connect.text = "Đã có kết nối"
                    icWarningSetting.setImageResource(R.drawable.ic_check_2)
                }
            }

            tvTitleStatusConnect.text = "Checking...."
            dialogNoticeConnect.show()
            dialog.dismiss()
        }

        //Open setup camera
        btn_setup_camera.setOnClickListener {
            dialog.show()
        }
        btn_edit_link_connect_camera.setOnClickListener {
            dialog.show()
        }
    }

    private fun checkStatusProtocol(icCheckTCP: ImageView, icCheckUDP: ImageView) {
        when {
            choseProtocol[0] -> {
                icCheckTCP.setImageResource(R.drawable.radio_check)
                icCheckUDP.setImageResource(R.drawable.radio_uncheck)
            }
            choseProtocol[1] -> {
                icCheckTCP.setImageResource(R.drawable.radio_uncheck)
                icCheckUDP.setImageResource(R.drawable.radio_check)
            }
            else -> {
                icCheckTCP.setImageResource(R.drawable.radio_uncheck)
                icCheckUDP.setImageResource(R.drawable.radio_uncheck)
            }
        }
    }

    private fun getTextIDAndUID() {
        tv_id.text = CustomerStore.loginID
        tv_uID.text = HttpHeaders.deviceID
    }

    private fun editEstablishmentName() {
        btn_edit_establishment.setOnClickListener {
            dataEstablishmentName.text = ScoreBoard.nameEstablishment

            lifecycleScope.launch {
                //create dialog
                val dialog = Dialog(this@SettingScreen)
                dialog.setCanceledOnTouchOutside(false)
                dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                dialog.setContentView(R.layout.custom_popup_edit_establishment)
                val btnSave = dialog.findViewById<Button>(R.id.btn_save_edit_establishment)
                val btnCancel = dialog.findViewById<Button>(R.id.btn_cancel_edit_establishment)
                val icClose = dialog.findViewById<ImageView>(R.id.ic_close_edit_establishment)
                establishmentSpinner = dialog.findViewById<Spinner>(R.id.spinner_establishment_name) as CustomSpinner

                //get list establishment
                CustomerStore.apiCustomerPlace()

                val listEstablishment: List<CustomerPlaceQuery.Customer_place>? = CustomerStore.customerPlaceList
                establishmentSpinner!!.setSpinnerEventsListener(this@SettingScreen)
                adapter = EstablishmentAdapter(this@SettingScreen, listEstablishment)

                establishmentSpinner!!.adapter = adapter

                if (CustomerStore.locationID != null && listEstablishment != null) {
                    listEstablishment.forEachIndexed { index, place ->
                        if (place.id == CustomerStore.customerID) {
                            establishmentSpinner!!.setSelection(index)
                        }
                    }
                }

                btnSave.setOnClickListener {
                    checkInfoSetting++
                    if (checkInfoSetting == 1) {
                        containBackButtonSetting.visibility = View.VISIBLE
                        checkInfoSetting = 0
                    }

                    if (establishmentSpinner!!.selectedItem != null) {
                        ScoreBoard.nameEstablishment = listEstablishment!![establishmentSpinner!!.selectedItemPosition].name
                        dataEstablishmentName.text = listEstablishment!![establishmentSpinner!!.selectedItemPosition].name
                        CustomerStore.locationID = listEstablishment!![establishmentSpinner!!.selectedItemPosition].id.toString()
                    }

                    dialog.dismiss()
                }

                btnCancel.setOnClickListener {
                    dialog.dismiss()
                }

                icClose.setOnClickListener {
                    dialog.dismiss()
                }

                dialog.show()
            }
        }
    }

    private fun editPasswordProtection() {

        val dialogChangePassProtect = Dialog(this@SettingScreen)
        dialogChangePassProtect.setContentView(R.layout.custom_popup_pass_protect)
        dialogChangePassProtect.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val editPassPresent = dialogChangePassProtect.findViewById<EditText>(R.id.edt_pass_present)
        val editNewPass = dialogChangePassProtect.findViewById<EditText>(R.id.edt_new_pass)
        val editNewPass2 = dialogChangePassProtect.findViewById<EditText>(R.id.edt_new_pass_2)
        val btnSave = dialogChangePassProtect.findViewById<Button>(R.id.btn_save)
        val btnCancel = dialogChangePassProtect.findViewById<Button>(R.id.btn_cancel)
        val tvError = dialogChangePassProtect.findViewById<TextView>(R.id.tv_error_pass_protect)
        val tvErrorNewPass = dialogChangePassProtect.findViewById<TextView>(R.id.tv_error_new_pass_protect)

        btnShowPass.setOnClickListener {

            //Cancel changed pass protect
            btnCancel.setOnClickListener {
                dialogChangePassProtect.dismiss()
            }

            //Save changed pass protect
            btnSave.setOnClickListener {
                //Log.d(TAG, "Mk Present :: ${ScoreBoard.passwordProtect}")
                if (editPassPresent.text.toString() != ScoreBoard.passwordProtect) {
                    tvError.visibility = View.VISIBLE
                } else {
                    tvError.visibility = View.GONE
                    if (editNewPass.text.toString() == editNewPass2.text.toString()) {
                        tvErrorNewPass.visibility = View.GONE
                        ScoreBoard.passwordProtect = editNewPass.text.toString()
                        dataShowPass.text = ScoreBoard.passwordProtect
                        dialogChangePassProtect.dismiss()
                        editPassPresent.text = null
                        editNewPass.text = null
                        editNewPass2.text = null
                    } else {
                        tvErrorNewPass.visibility = View.VISIBLE
                    }
                }
            }

            dialogChangePassProtect.show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun activePasswordProtection() {

        val dialogCreatePassProtect = Dialog(this@SettingScreen)
        dialogCreatePassProtect.setCanceledOnTouchOutside(false)
        dialogCreatePassProtect.setContentView(R.layout.custom_popup_create_pass_protect)
        dialogCreatePassProtect.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val editCreatePass = dialogCreatePassProtect.findViewById<EditText>(R.id.edt_create_pass)
        val editCreatePass2 = dialogCreatePassProtect.findViewById<EditText>(R.id.edt_create_pass_2)
        val btnSaveCreatePass = dialogCreatePassProtect.findViewById<Button>(R.id.btn_save_create_pass)
        val btnCancelCreatePass = dialogCreatePassProtect.findViewById<Button>(R.id.btn_cancel_create_pass)
        val tvErrorCreatePass = dialogCreatePassProtect.findViewById<TextView>(R.id.tv_error_create_pass_protect)
        val icCloseCreatePass = dialogCreatePassProtect.findViewById<ImageView>(R.id.ic_close_create_pass_protect)


        val dialog = Dialog(this@SettingScreen)
        dialog.setContentView(R.layout.custom_popup_input_pass_protect)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val edtInputPassProtect = dialog.findViewById<EditText>(R.id.edt_input_pass_protect)
        val tvErrorPassProtect = dialog.findViewById<TextView>(R.id.tv_error_input_pass_protect)
        val btnConfirmPassProtect = dialog.findViewById<Button>(R.id.btn_confirm_pass_protect)

        swPass.setOnClickListener {
            //ScoreBoard.enableProtect = ScoreBoard.enableProtect != true

            if (!ScoreBoard.enableProtect!!) {

                dialogCreatePassProtect.show()

                //Cancel create pass protect
                btnCancelCreatePass.setOnClickListener {
                    dialogCreatePassProtect.dismiss()
                }

                icCloseCreatePass.setOnClickListener {
                    dialogCreatePassProtect.dismiss()
                }

                //Save create pass protect
                btnSaveCreatePass.setOnClickListener {
                    if (editCreatePass.text.toString() == editCreatePass2.text.toString()) {
                        if(editCreatePass.text.length < 6) {
                            tvErrorCreatePass.text = "Mật khẩu phải dài hơn 6 ký tự"
                            tvErrorCreatePass.visibility = View.VISIBLE
                        } else {
                            tvErrorCreatePass.visibility = View.GONE
                            ScoreBoard.passwordProtect = editCreatePass.text.toString()
                            dataShowPass.text = ScoreBoard.passwordProtect
                            dialogCreatePassProtect.dismiss()
                            editCreatePass.text = null
                            editCreatePass2.text = null
                            swPass.setImageResource(R.drawable.toggle_on)
                            constrainShowPass.visibility = View.VISIBLE;
                            ScoreBoard.enableProtect = true
                        }
                    } else {
                        tvErrorCreatePass.text = "Xác nhận mật khẩu không trùng khớp!"
                        tvErrorCreatePass.visibility = View.VISIBLE
                    }
                }
            } else {
                dialog.show()
                btnConfirmPassProtect.setOnClickListener {
                    if(edtInputPassProtect.text.toString() == ScoreBoard.passwordProtect) {
                        swPass.setImageResource(R.drawable.toggle_off)
                        constrainShowPass.visibility = View.INVISIBLE
                        ScoreBoard.passwordProtect = ""
                        dataShowPass.text = ScoreBoard.passwordProtect
                        dialog.dismiss()
                        edtInputPassProtect.text = null
                        ScoreBoard.enableProtect = false

                    } else {
                        tvErrorPassProtect.visibility = View.VISIBLE
                    }
                }
            }
        }

        if (ScoreBoard.enableProtect!!) {
            swPass.setImageResource(R.drawable.toggle_on)
            constrainShowPass.visibility = View.VISIBLE;
        } else {
            swPass.setImageResource(R.drawable.toggle_off)
            constrainShowPass.visibility = View.INVISIBLE
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onPopupWindowOpened(spinner: Spinner?) {
        establishmentSpinner?.setBackgroundResource(R.drawable.background_spinner_bottom)
    }

    override fun onPopupWindowClosed(spinner: Spinner?) {
        establishmentSpinner?.setBackgroundResource(R.drawable.background_spinner_right)
    }

    private fun setupLogout() {
        btnLogOut.setOnClickListener {
            // delete all data
            ScoreBoard.clearAll()

            // go to login page
            val intent = Intent(application, LoginScreen::class.java)
            startActivity(intent)
            finish()
        }
    }

}