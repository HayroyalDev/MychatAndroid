package com.hayroyalconsult.maverickstl.mychat.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.ListView
import com.hayroyalconsult.maverickstl.mychat.R
import com.hayroyalconsult.maverickstl.mychat.adapters.ChatListAdapter
import com.hayroyalconsult.maverickstl.mychat.adapters.ViewChatAdapter
import com.hayroyalconsult.maverickstl.mychat.models.Message
import com.hayroyalconsult.maverickstl.mychat.models.User
import com.hayroyalconsult.maverickstl.mychat.utility.AppPreferences
import com.hayroyalconsult.maverickstl.mychat.utility.DbHelper
import com.hayroyalconsult.maverickstl.mychat.utility.ModelConverter

import kotlinx.android.synthetic.main.activity_view.*
import android.database.DataSetObserver
import android.support.v4.content.LocalBroadcastManager
import android.widget.AbsListView
import com.hayroyalconsult.maverickstl.mychat.http.RetrofitClient
import com.hayroyalconsult.maverickstl.mychat.response.Response
import kotlinx.android.synthetic.main.content_view.*
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class ViewActivity : AppCompatActivity() {

    val TAG = "ViewActivity"
    var adapter : ViewChatAdapter? = null
    var appPreference : AppPreferences? = null
    var friends : ArrayList<User>? = null
    var user : User? = null
    var db : DbHelper? = null
    var retrofitClient : RetrofitClient? = null

    private val mMessageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Get extra data included in the Intent
            val message = intent.getIntExtra("status",2)
            if(message == 1 && adapter != null){
                if(intent.hasExtra("friends")){
                    friends =  ModelConverter.GsonToClass<ArrayList<User>>(intent.getStringExtra("friends"))
                    friends?.forEach {
                        if(it.id == friend!!.id){
                            messages!!.add(it.last_message!!)
                        }
                    }
                }
                refresh()
                //adapter!!.swapItem(friends!!)
            }
            Log.e(TAG, message.toString())
            Log.e(TAG, friends.toString())
        }
    }
    var friend : User? = null
    var messages : ArrayList<Message>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        appPreference = AppPreferences(this)
        db = DbHelper(this).open()
        user = ModelConverter.GsonToClass<User>(appPreference!!.getUser())
        friend = ModelConverter.GsonToClass<User>(intent.getStringExtra("friend"))
        toolbar.title = friend!!.username
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, IntentFilter("MessageUpdate"))
        messages = Message.messageList(db!!.getChatHistory(friend!!.id!!, user!!.id!!))
        if(messages == null)
            messages = ArrayList()
        adapter = ViewChatAdapter(this,messages!!,user!!, friend!!)
        val lv = findViewById<ListView>(R.id.rv_chat)
        lv.transcriptMode = AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL
        lv.isStackFromBottom = true

        lv.adapter = adapter
        adapter!!.registerDataSetObserver(object : DataSetObserver() {
            override fun onChanged() {
                super.onChanged()
                lv.setSelection(adapter!!.count - 1)
            }
        })
        Log.e(TAG, messages.toString())
        btn_send.setOnClickListener {
            if(et_chat.text.isNotEmpty()){
                var msg = Message()
                msg.from = user!!.id
                msg.to = friend!!.id
                msg.message = et_chat.text.toString()
                msg.mid = Random().nextInt(99999999)
                msg.status = 0
                msg.created_at = getDate()
                db!!.addMessage(msg)
                msg = Message.messageSingle( db!!.getMessageByMid(msg.mid!!))!!
                messages!!.add(msg)
                sendMessage(msg)
                adapter!!.notifyDataSetChanged()
                et_chat.text.clear()
                Log.e(TAG, msg.toString())
            }
        }

        Thread(Runnable {
            updateStatus()
        }).start()

    }

    fun refresh(){
        messages = Message.messageList(db!!.getChatHistory(friend!!.id!!, user!!.id!!))
        runOnUiThread {
            adapter!!.swapItem(messages!!)
        }
    }


    private fun updateStatus() {
        var mids = ""
        if(messages != null){
            messages?.forEach {
                if((it.from != user!!.id) && (it.status!! < 3))
                    if(it.status!! == 0){
                        sendMessage(it)
                    }else{
                        if(mids.isNotEmpty())
                            mids += ",${it.mid}"
                        else
                            mids = "${it.mid}"
                    }

            }

            if(mids != ""){
                Log.e(TAG, "mids=$mids")
                updateStatusApi(mids)
            }
        }
    }

    fun getDate() : String{
        val date = Date()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm")
        sdf.timeZone = TimeZone.getTimeZone("GMT+1")
        return sdf.format(date)
    }

    private fun sendMessage(msg : Message) {
        Log.e(TAG, "Send Message")
        retrofitClient  = RetrofitClient(this, appPreference!!.getBaseUrl())
        retrofitClient!!.apiService!!.sendMessage(msg.mid!!,msg.from!!, msg.to!!, msg.message!!)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Subscriber<Response<Message>>() {
                    override fun onCompleted() {
                        Log.e(TAG, "completed")
                    }

                    override fun onError(throwable: Throwable) {
                        Log.e(TAG, "new friends onError: +" + throwable.toString())
                    }

                    override fun onNext(response: Response<Message>) {
                        if(response.status != 1){
                            Log.e(TAG, "Message Could Not Be Sent")
                        }else{
                            db!!.updateStatus(msg.mid!!, 1)
                            for (i in messages!!.size-1..0){
                                if(messages!![i].mid == msg.mid){
                                    messages!![i] = msg
                                    //db!!.addMessage(msg)
                                    Log.e(TAG, "replaced message after send")
                                    break
                                }
                            }
                            adapter!!.notifyDataSetChanged()
                            Log.e(TAG,response.toString())
                        }
                    }
                })
    }

    private fun updateStatusApi(mid : String){
        Log.e(TAG, "Get New Friends")
        retrofitClient  = RetrofitClient(this, appPreference!!.getBaseUrl())
        retrofitClient!!.apiService!!.setMessageStatus(mid, 3)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Subscriber<Response<Message>>() {
                    override fun onCompleted() {
                        Log.e(TAG, "completed")
                    }

                    override fun onError(throwable: Throwable) {
                        Log.e(TAG, "Status onError: +" + throwable.toString())
                    }

                    override fun onNext(response: Response<Message>) {
                        if(response.status != 1){
                            Log.e(TAG, "Message Not Found")
                        }else{
                            response.data!!.forEach {
                                db!!.updateStatus(it.mid!!,3)
                            }
                            Log.e(TAG,response.toString())
                        }
                    }
                })
    }
}
