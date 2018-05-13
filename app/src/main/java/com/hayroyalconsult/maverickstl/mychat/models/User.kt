package com.hayroyalconsult.maverickstl.mychat.models

/**
 * Created by robot on 5/10/18.
 */
class User{
    var id : Int? = null
    var username : String? = null
    var password : String? = null
    var created_at : String? = null
    var last_message : Message? = null
    override fun toString(): String {
        return "User(id=$id, username=$username, password=$password, created_at=$created_at, last_message=${last_message.toString()})"
    }
}