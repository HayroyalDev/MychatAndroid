package com.hayroyalconsult.maverickstl.mychat.activity

import android.app.ProgressDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.hayroyalconsult.maverickstl.mychat.R
import com.hayroyalconsult.maverickstl.mychat.http.RetrofitClient
import com.hayroyalconsult.maverickstl.mychat.models.User
import com.hayroyalconsult.maverickstl.mychat.response.Response
import com.hayroyalconsult.maverickstl.mychat.utility.AppPreferences
import com.hayroyalconsult.maverickstl.mychat.utility.MyDialog
import kotlinx.android.synthetic.main.activity_splash.*
import okhttp3.ResponseBody
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import com.hayroyalconsult.maverickstl.mychat.services.BackgroundService
import com.hayroyalconsult.maverickstl.mychat.utility.DbHelper


class SplashActivity : AppCompatActivity() {

    var dialog : MyDialog? = null
    val TAG = "SplashActivity"
    var select = "registration"
    var pd : ProgressDialog? = null
    var retrofitClient : RetrofitClient? = null
    var appPreferences : AppPreferences? = null
    var dbHelper : DbHelper? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        pd = ProgressDialog(this)
        dialog = MyDialog(this)
        dbHelper = DbHelper(this).open()
        appPreferences = AppPreferences(this@SplashActivity)
        if(appPreferences!!.getUser() != null){
            startService(Intent(this, BackgroundService::class.java))
            goToChat()
        }
        pd!!.setMessage("Please Wait...")
        sign_up_btn.setOnClickListener{
            submit.text = "Sign Up"
            select = "registration"
            select_form.visibility = View.GONE
            loginform.visibility = View.VISIBLE
        }

        sign_in_btn.setOnClickListener {
            select = "login"
            submit.text = "Sign In"
            select_form.visibility = View.GONE
            loginform.visibility = View.VISIBLE
        }

        submit.setOnClickListener{
            when {
                username.text.isEmpty() -> username.error = "Invalid Username"
                password.text.isEmpty() -> password.error = "Invalid Password"
                else -> {
                    pd!!.show()
                    createOrLogUser()
                }
            }
        }

    }

    private fun goToChat() {
        startActivity(Intent(this@SplashActivity, ChatActivity::class.java))
        finish()
    }

    private fun createOrLogUser() {
        Log.e(TAG, appPreferences!!.getBaseUrl())
        retrofitClient  = RetrofitClient(this, appPreferences!!.getBaseUrl())
        retrofitClient!!.apiService!!.createOrLogUser(select, username.text.toString(), password.text.toString())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Subscriber<Response<User>>() {
                    override fun onCompleted() {
                       Log.e(TAG, "completed")
                    }

                    override fun onError(throwable: Throwable) {
                        Log.e(TAG, "onError: +" + throwable.toString())
                        pd!!.dismiss()
                        dialog!!.showDialog("an error occurred, Please try again")
                    }

                    override fun onNext(responseUser: Response<User>) {
                        pd!!.dismiss()
                        if(responseUser.status != 1){
                            dialog!!.showDialog(responseUser.message!!)
                        }else{
                            appPreferences!!.setUser(responseUser.data!![0])
                            startService(Intent(this@SplashActivity, BackgroundService::class.java))
                            goToChat()
                        }
                        Log.e(TAG, "onNext: Post" +  responseUser.data!![0])
                    }
                })

    }

    override fun onBackPressed() {
        if(loginform.visibility == View.VISIBLE){
            loginform.visibility = View.GONE
            select_form.visibility = View.VISIBLE
        }else
            super.onBackPressed()

    }
}
