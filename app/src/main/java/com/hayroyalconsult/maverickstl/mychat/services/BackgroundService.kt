package com.hayroyalconsult.maverickstl.mychat.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.hayroyalconsult.maverickstl.mychat.http.RetrofitClient
import android.support.v4.content.LocalBroadcastManager
import android.os.Bundle
import com.google.gson.Gson
import com.hayroyalconsult.maverickstl.mychat.models.Message
import com.hayroyalconsult.maverickstl.mychat.models.User
import com.hayroyalconsult.maverickstl.mychat.response.Response
import com.hayroyalconsult.maverickstl.mychat.utility.AppPreferences
import com.hayroyalconsult.maverickstl.mychat.utility.DbHelper
import com.hayroyalconsult.maverickstl.mychat.utility.ModelConverter
import kotlinx.android.synthetic.main.activity_splash.*
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers


/**
 * Created by robot on 5/10/18.
 */
class BackgroundService : Service(){
    var context: Context? = null
    var handler: Handler? = null
    var runnable: Runnable? = null
    val TAG = "Service"
    var appPreferences : AppPreferences? = null
    var user : User? = null
    var retrofitClient : RetrofitClient? = null
    var dbHelper : DbHelper? = null
    override fun onBind(intent: Intent?): IBinder? {
        TODO("not implemented") //To
        return null
    }

    override fun onCreate() {
        appPreferences = AppPreferences(this)
        //appPreferences!!.setFriends(null)
        user  = ModelConverter.GsonToClass<User>(appPreferences!!.getUser()!!)
        context = this
        retrofitClient  = RetrofitClient(this, RetrofitClient.Defaulthost)
        dbHelper = DbHelper(this).open()
        Log.e(TAG, "Service Created + $user!!")
        try{
            if(user != null){
                handler = Handler()
                runnable = Runnable {
                    Toast.makeText(context, "Service is still running", Toast.LENGTH_LONG).show()
                    getUndeliveredMessages(user!!.id!!)
                    handler!!.postDelayed(runnable,15000)
                }
            }else{
                Log.e(TAG, "No User")
            }

            handler!!.postDelayed(runnable, 5000)
        }catch (ex : Exception){
            Log.e(TAG, "Error From Service + $ex")
        }
    }

    override fun onDestroy() {
        /* IF YOU WANT THIS SERVICE KILLED WITH THE APP THEN UNCOMMENT THE FOLLOWING LINE */
        handler!!.removeCallbacks(runnable)
        Toast.makeText(this, "Service stopped", Toast.LENGTH_LONG).show()
    }

    override fun onStart(intent: Intent?, startid: Int) {
    }
    private fun getUndeliveredMessages(id : Int){
        Log.e(TAG, "Sending API")
        retrofitClient!!.apiService!!.getUndeliveredMessage(id)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Subscriber<Response<Message>>() {
                    override fun onCompleted() {
                        Log.e(TAG, "completed")
                    }

                    override fun onError(throwable: Throwable) {
                        Log.e(TAG, "undelonError: +" + throwable.toString())
                    }

                    override fun onNext(response: Response<Message>) {
                        if(response.status != 1){
                        }else{
                            if(response.data!!.isNotEmpty()){
                                populateDatabase(response.data!!)
                            }
                        }
                        Log.e(TAG, "onNext: Post" +  response)
                    }
                })

    }

    private fun populateDatabase(list: ArrayList<Message>) {
        list.let {
            var mids = ""
            list.forEach {
                it.status = 2
                dbHelper!!.addMessage(it)
                if(mids.isNotEmpty())
                    mids += ",${it.mid}"
                else
                    mids = "${it.mid}"

            }
            if(mids != "")
                sendDeliveryNotification(mids,2)
            updateFriendList(list)
            sendMessageToActivity(1)
        }
    }

    private fun updateFriendList(ls: ArrayList<Message>) {
        ls.let {
            var list = appendLastMessage(ls)
            if(list.size > 0){
                var ids = ""
                list.forEach {
                    if(ids.length == 1)
                        ids += ",${it.from}"
                    else
                        ids = "${it.from}"
                }
                Log.e(TAG,"Ids = $ids")
                getNewFriends(ids, list)
            }
            sendMessageToActivity(1)
        }

    }
    private fun appendLastMessage(list : ArrayList<Message>) : ArrayList<Message>{
        val temp = ArrayList<Message>()
        val friends = ModelConverter.GsonToClass<ArrayList<User>>(appPreferences!!.getFriends())
        if(friends != null){
            list.forEach { lf ->
                friends.forEach fr@ {f ->
                    if(f.id == lf.from){
                        f.last_message = lf
                        temp.add(lf)
                        return@fr
                    }
                }
            }
            appPreferences!!.setFriends(friends)
        }
        Log.e(TAG, "Not Here")
        list.removeAll(temp)
        return list
    }

    private fun getNewFriends(ids: String, list: ArrayList<Message>) {
        Log.e(TAG, "Get New Friends")
        retrofitClient!!.apiService!!.getUsers(ids)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Subscriber<Response<User>>() {
                    override fun onCompleted() {
                        Log.e(TAG, "completed")
                    }

                    override fun onError(throwable: Throwable) {
                        Log.e(TAG, "new friends onError: +" + throwable.toString())
                    }

                    override fun onNext(response: Response<User>) {
                        if(response.status != 1){
                            Log.e(TAG, "could not get friends")
                        }else{
                            Log.e(TAG,response.toString())
                            var friends = ModelConverter.GsonToClass<ArrayList<User>>(appPreferences!!.getFriends())
                            if(friends == null){
                                friends = ArrayList()
                            }
                            friends.addAll(response.data!!)
                            appPreferences!!.setFriends(friends)
                            appendLastMessage(list)
                            sendMessageToActivity(1, friends)
                            Log.e(TAG, "Got New friends Updated")
                        }
                    }
                })
    }

    private fun sendDeliveryNotification(mid : String, status: Int) {
        Log.e(TAG, "Updating Delivery")
        retrofitClient!!.apiService!!.setMessageStatus(mid, status)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Subscriber<Response<Message>>() {
                    override fun onCompleted() {
                        Log.e(TAG, "completed")
                    }

                    override fun onError(throwable: Throwable) {
                        Log.e(TAG, "delivery onError: +" + throwable.toString())
                    }

                    override fun onNext(response: Response<Message>) {
                        if(response.status != 1){
                            Log.e(TAG, "Unable Updated")
                        }else{
                            response.data!!.forEach {
                                dbHelper!!.updateStatus(it.mid!!,2)
                            }
                            Log.e(TAG,response.toString())                        }
                        Log.e(TAG, "onNext: Post" +  response)
                    }
                })
    }

    private fun sendMessageToActivity(status : Int, friends : ArrayList<User>) {

        Log.e(TAG, "Sending Broadcast")
        val intent = Intent("MessageUpdate").apply {
            putExtra("status", status)
            putExtra("friends", Gson().toJson(friends))
        }
        // You can also include some extra data.
        LocalBroadcastManager.getInstance(context!!).sendBroadcast(intent)
    }

    private fun sendMessageToActivity(status : Int) {

        Log.e(TAG, "Sending Broadcast")
        val intent = Intent("MessageUpdate").apply {
            putExtra("status", status)
        }
        // You can also include some extra data.
        LocalBroadcastManager.getInstance(context!!).sendBroadcast(intent)
    }
}