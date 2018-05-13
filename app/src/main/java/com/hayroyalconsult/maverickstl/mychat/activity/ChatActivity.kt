package com.hayroyalconsult.maverickstl.mychat.activity

import android.app.ProgressDialog
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import com.hayroyalconsult.maverickstl.mychat.R

import kotlinx.android.synthetic.main.activity_chat.*
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.google.gson.Gson
import com.hayroyalconsult.maverickstl.mychat.adapters.ChatListAdapter
import com.hayroyalconsult.maverickstl.mychat.adapters.SearchAdapter
import com.hayroyalconsult.maverickstl.mychat.http.RetrofitClient
import com.hayroyalconsult.maverickstl.mychat.models.Message
import com.hayroyalconsult.maverickstl.mychat.models.User
import com.hayroyalconsult.maverickstl.mychat.response.Response
import com.hayroyalconsult.maverickstl.mychat.services.BackgroundService
import com.hayroyalconsult.maverickstl.mychat.utility.AppPreferences
import com.hayroyalconsult.maverickstl.mychat.utility.DbHelper
import com.hayroyalconsult.maverickstl.mychat.utility.ModelConverter
import kotlinx.android.synthetic.main.content_chat.*
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.lang.System.out
import java.text.SimpleDateFormat
import java.util.*


class ChatActivity : AppCompatActivity() {

    val TAG = "ChatActivity"
    var adapter : ChatListAdapter? = null
    var appPreference : AppPreferences? = null
    var friends : ArrayList<User>? = null
    var user : User? = null
    var db : DbHelper? = null
    var appPreferences : AppPreferences? = null
    private val mMessageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Get extra data included in the Intent
            val message = intent.getIntExtra("status",2)
            if(message == 1 && adapter != null){
                if(intent.hasExtra("friends")){
                    friends =  ModelConverter.GsonToClass<ArrayList<User>>(intent.getStringExtra("friends"))
                }
                adapterWork()
            }
            //Log.e(TAG, message.toString())
            //Log.e(TAG, friends.toString())
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        setSupportActionBar(toolbar)
        db = DbHelper(this).open()
        appPreference = AppPreferences(this)
        toolbar.title = "Chat List"
        val lv = findViewById<ListView>(R.id.chat_list__view)
        appPreference = AppPreferences(this)
        user = ModelConverter.GsonToClass<User>(appPreference!!.getUser()!!)
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, IntentFilter("MessageUpdate"))

        friends = ModelConverter.GsonToClass<ArrayList<User>>(appPreference?.getFriends())
        if(friends == null) {
            friends = ArrayList()
        }
        getLastMessage()
        adapter = ChatListAdapter(this, friends!!, user!!)
        updateView()
        lv.adapter = adapter
        lv.setOnItemClickListener { _, _, position, _ ->
            var selected = lv.getItemAtPosition(position) as User
            startActivity(Intent(this, ViewActivity::class.java).apply {
                putExtra("friend", Gson().toJson(selected))
            })
        }
    }

    fun adapterWork(){
        runOnUiThread {
            getLastMessage()
            updateView()
            adapter!!.swapItem(friends!!)
        }
    }

    override fun onResume() {
        adapterWork()
        super.onResume()
    }
    private fun getLastMessage() {
        friends?.let {
            friends!!.forEach  { fr ->
                fr.last_message = Message.messageSingle(db!!.getLastMessage(fr.id!!, user!!.id!!))
            }

            sortByTime(it)
            Log.e(TAG, it.toString())
        }
    }

    private fun sortByTime(list: ArrayList<User>) {
       if(list.size > 1){
           Collections.sort(list, object : Comparator<User> {
               internal var df = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH)
               override fun compare(lhs: User, rhs: User): Int {
                   try {
                       df.timeZone = TimeZone.getDefault()
                       if (lhs.last_message!!.created_at == null && rhs.last_message!!.created_at == null) {
                           return 1
                       }
                       if (lhs.last_message!!.created_at == null && rhs.last_message!!.created_at != null) {
                           return 1
                       }

                       if (rhs.last_message!!.created_at == null && lhs.last_message!!.created_at != null) {
                           return -1
                       }
                       val a = df.parse(lhs.last_message!!.created_at)
                       val b = df.parse(rhs.last_message!!.created_at)

                       Log.e(TAG, "Result Of Comparison.....sortChatList, Line 328")
                       out.println(-a.compareTo(b))
                       return -a.compareTo(b)
                   } catch (e: Exception) {
                       e.printStackTrace()
                       return 0
                   }

               }
           })
           friends = list
           Log.e(TAG, list.toString())
       }
    }

    private fun updateView(){
        if(friends!!.size == 0){
            chat_list__view.visibility = View.GONE
            chat_frag_tv.visibility = View.VISIBLE
        }else{
            chat_list__view.visibility = View.VISIBLE
            chat_frag_tv.visibility = View.GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.chat_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    fun convertTime(time: Long) :String{
        val date = Date(time)
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm")
        sdf.timeZone = TimeZone.getTimeZone("GMT+1")
        return sdf.format(date)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item!!.itemId){
            R.id.search -> {
                showSearchDialog()
            }
            R.id.sign_out -> {
                db!!.truncateMessage()
                appPreference!!.clear()
                stopService(Intent(this, BackgroundService::class.java))
                startActivity(Intent(this,SplashActivity::class.java))
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showSearchDialog() {
        val dialog  = AlertDialog.Builder(this).create()
        val v = layoutInflater.inflate(R.layout.layout_search, null)
        val search_text = v.findViewById<EditText>(R.id.search_text)
        val search_btn = v.findViewById<Button>(R.id.search_btn)
        val search_no = v.findViewById<TextView>(R.id.search_no)
        val search_list = v.findViewById<ListView>(R.id.search_lv)
        search_list.setOnItemClickListener { _, _, position, _ ->
            val temp = search_list.getItemAtPosition(position) as User
            friends!!.add(temp)
            friends?.let ll@{
                friends?.forEach {fd->
                    if(fd.id == temp.id){
                        return@ll
                    }
                }
                appPreference!!.setFriends(friends!!)
            }

            dialog.dismiss()
            adapterWork()
            startActivity(Intent(this, ViewActivity::class.java).apply {
                putExtra("friend", Gson().toJson(temp))
            })

        }
        search_btn.setOnClickListener {
            if(search_text.text.isNotBlank()){
                var pd = ProgressDialog(this)
                pd.setMessage("Searching...")
                pd.setCanceledOnTouchOutside(false)
                pd.show()
                val retrofit = RetrofitClient(this, RetrofitClient.Defaulthost)
                retrofit.apiService!!.searchResult(search_text.text.toString())
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(object : Subscriber<Response<User>>() {
                            override fun onCompleted() {
                                Log.e(TAG, "completed")
                            }

                            override fun onError(throwable: Throwable) {
                                pd.dismiss()
                                search_text.error ="An Error Occurred."
                                Log.e(TAG, "new friends onError: +" + throwable.toString())
                            }

                            override fun onNext(response: Response<User>) {
                                if(response.status != 1){
                                    search_text.error = response.message
                                    search_no.text = response.message
                                    Log.e(TAG, "could not get friends")
                                }else{
                                    val temp = response.data
                                    if(temp!!.size == 0){
                                        search_text.error = "User not found"
                                    }else{
                                        temp.remove(user)
                                        for(i in 0 until temp.size-1){
                                            if(temp[i].id == user!!.id){
                                                temp.removeAt(i)
                                            }
                                        }
                                        val adapter = SearchAdapter(this@ChatActivity, temp)
                                        search_list.adapter = adapter
                                    }

                                }
                                pd.dismiss()
                            }
                        })
            }else{
                search_text.error = "Field Can not be empty"
            }
        }
        dialog.setView(v)
        dialog.setTitle("User Search")
        dialog.setCancelable(true)
        dialog.show()
    }

    override fun onBackPressed() {
        stopService(Intent(this, BackgroundService::class.java))
        super.onBackPressed()
    }
}
