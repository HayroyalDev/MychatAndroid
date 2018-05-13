package com.hayroyalconsult.maverickstl.mychat.response

import com.google.gson.Gson
import com.hayroyalconsult.maverickstl.mychat.models.User
import com.hayroyalconsult.maverickstl.mychat.utility.ModelConverter

/**
 * Created by robot on 5/10/18.
 */
class Response<T>{
    var status : Int? = null
    var message : String? = null
    var data: ArrayList<T>? = null
    //var user : User? = convertToUser()
    override fun toString(): String {
        return "ResponseUser(status=$status, message=$message, data=${data})"
    }
}