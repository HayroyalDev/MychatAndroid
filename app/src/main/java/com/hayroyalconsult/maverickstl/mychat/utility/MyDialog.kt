package com.hayroyalconsult.maverickstl.mychat.utility

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.hayroyalconsult.maverickstl.mychat.R


/**
 * Created by robot on 5/10/18.
 */
class MyDialog(context: Context) : AlertDialog(context) {
    fun showDialog(message : String){
        setTitle(null)
        setCancelable(true)
        setCanceledOnTouchOutside(true)
        setOnCancelListener(null)
        setMessage(message)
        show()
    }
}