package com.hayroyalconsult.maverickstl.mychat.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.hayroyalconsult.maverickstl.mychat.R
import com.hayroyalconsult.maverickstl.mychat.models.Message
import com.hayroyalconsult.maverickstl.mychat.models.User

/**
 * Created by robot on 5/10/18.
 */
class ChatListAdapter(var context: Context, var list : ArrayList<User>, var user : User) : BaseAdapter(){
    var mInflater = LayoutInflater.from(context)


    override fun getView(position: Int, altView: View?, parent: ViewGroup?): View {
        val holder: ViewHolder
        var convertView = altView
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.chat_list_row, null)
            holder = ViewHolder()
            holder.username = convertView.findViewById(R.id.username)
            holder.time = convertView.findViewById(R.id.time)
            holder.message = convertView.findViewById(R.id.message)
            holder.unread = convertView.findViewById(R.id.count)
            convertView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }
        if(list[position].last_message == null){
            val mes = Message()
            mes.message =  ""
            mes.created_at = ""
            mes.status = 0
            list[position].last_message = mes
        }else{

        }
        holder.username!!.text = list[position].username
        holder.time!!.text = list[position].last_message?.created_at!!
        holder.message!!.text = list[position].last_message!!.message!!
        if(list[position].last_message!!.from != user.id){
            if(list[position].last_message!!.status == 2)
                holder.unread!!.text = "N"
            else
                holder.unread!!.text = ""
        }else{
            holder.unread!!.text = ""
        }
        return convertView!!
    }

    fun swapItem(temp : ArrayList<User>){
        list = temp
        notifyDataSetChanged()
    }
    override fun getItem(position: Int): Any {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return list.size.toLong()
    }

    override fun getCount(): Int {
        return list.size
    }

    internal class ViewHolder {
        var username : TextView? = null
        var message : TextView? = null
        var time : TextView? = null
        var unread : TextView? = null
    }

}