package com.hayroyalconsult.maverickstl.mychat.utility

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Created by robot on 5/10/18.
 */
class ModelConverter{
    companion object {
        val TAG = "ModelConverter"
        //.....................................................Method to convert gson to class
        inline fun <reified T> GsonToClass(value: String?): T? {
            return if(value != null){
                val type = object : TypeToken<T>() {}.type
                Gson().fromJson(value, type)
            }else{
                null
            }

        }
    }
}