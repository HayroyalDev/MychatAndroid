package com.hayroyalconsult.maverickstl.mychat.activity

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast

import com.hayroyalconsult.maverickstl.mychat.R
import com.hayroyalconsult.maverickstl.mychat.services.BackgroundService
import com.hayroyalconsult.maverickstl.mychat.utility.AppPreferences
import kotlinx.android.synthetic.main.activity_ip.*

class IpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ip)
        setSupportActionBar(toolbar)
        val appPreferences = AppPreferences(this)
        val old_add = appPreferences.getBaseUrl().replace(":8000/api/","")
        server_address.text = old_add
        new_server_address.setText(old_add)
        btn_server_change.setOnClickListener {
            if(new_server_address.text.toString().isNotEmpty()){
                appPreferences.setBaseUrl(new_server_address.text.toString())
                stopService(Intent(this, BackgroundService::class.java))
                startService(Intent(this, BackgroundService::class.java))
                Toast.makeText(this,"Server Address Updated", Toast.LENGTH_SHORT).show()
                val new = appPreferences.getBaseUrl().replace(":8000/api/","")
                server_address.text = new
                new_server_address.setText(new)
            }else{
                Toast.makeText(this,"Invalid Entry, Please Try Again", Toast.LENGTH_SHORT).show()
            }
        }

    }

}
