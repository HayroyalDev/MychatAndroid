package com.hayroyalconsult.maverickstl.mychat.utility

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.hayroyalconsult.maverickstl.mychat.models.User

/**
 * Created by robot on 5/10/18.
 */
class AppPreferences(var context: Context) {
    internal var gson = Gson()
    internal var spref: SharedPreferences = context.getSharedPreferences(context.packageName,Context.MODE_PRIVATE)

    fun setUser(value : User){
        val editor = spref.edit()
        editor.putString("user", Gson().toJson(value)).apply()
    }
    fun getUser() : String?{
        return spref.getString("user", null)
    }

    fun setFriends(value : ArrayList<User>?){
        if(value != null){
            val editor = spref.edit()
            editor.putString("friends", Gson().toJson(value)).apply()
        }else{
            val editor = spref.edit()
            editor.putString("friends", "").apply()
        }
    }

    fun getFriends() : String?{
        return spref.getString("friends",null)
    }

    fun getBaseUrl() : String{
        return spref.getString("ip","http://192.168.8.101:8000/api/")
    }

    fun setBaseUrl(value : String){
        if(value != null){
            val editor = spref.edit()
            editor.putString("ip", "$value:8000/api/").apply()
        }else{
            val editor = spref.edit()
            editor.putString("ip", "http://192.168.8.100:8000").apply()
        }
    }

    fun clear(){
        spref.edit().clear().apply()
    }
}