package com.bidaappscoreboard.common

import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.view.View

object HideKeyboard {
    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
}