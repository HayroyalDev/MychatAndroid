package com.hayroyalconsult.maverickstl.mychat.models

import android.database.Cursor

/**
 * Created by robot on 5/10/18.
 */
class Message{
    var id : Int? = null
    var from : Int? = null
    var to : Int? = null
    var message : String? = null
    var mid : Int? = null
    var status : Int? = null
    var created_at : String? = null
    var sender_name : String? = null


    companion object {
        fun messageList(res : Cursor) : ArrayList<Message>{
            var list = ArrayList<Message>()
            while (res.moveToNext()){
                var mes = Message()
                mes.id = res.getInt(res.getColumnIndex("id"))
                mes.mid = res.getInt(res.getColumnIndex("mid"))
                mes.message = res.getString(res.getColumnIndex("message"))
                mes.from = res.getInt(res.getColumnIndex("_from"))
                mes.to = res.getInt(res.getColumnIndex("_to"))
                mes.status = res.getInt(res.getColumnIndex("status"))
                mes.created_at = res.getString(res.getColumnIndex("created_at"))
                list.add(mes)
            }
            return list
        }

        fun messageSingle(res : Cursor) : Message?{
            return if(res.count != 0){
                res.moveToFirst()
                var mes = Message()
                mes.id = res.getInt(res.getColumnIndex("id"))
                mes.mid = res.getInt(res.getColumnIndex("mid"))
                mes.message = res.getString(res.getColumnIndex("message"))
                mes.from = res.getInt(res.getColumnIndex("_from"))
                mes.to = res.getInt(res.getColumnIndex("_to"))
                mes.status = res.getInt(res.getColumnIndex("status"))
                mes.created_at = res.getString(res.getColumnIndex("created_at"))
                mes
            } else{
                null
            }
        }
    }

    override fun toString(): String {
        return "Message(id=$id, from=$from, " +
                "to=$to, message=$message, mid=$mid, " +
                "status=$status, created_at=$created_at, " +
                "sender_name=$sender_name)"
    }


}