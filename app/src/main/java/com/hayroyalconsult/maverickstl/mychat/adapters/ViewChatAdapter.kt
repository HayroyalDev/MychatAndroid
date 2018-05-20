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
 * Created by robot on 5/11/18.
 */
class ViewChatAdapter(var context: Context, var list: ArrayList<Message>, var user : User, var friend :User) : BaseAdapter(){
    var mInflater = LayoutInflater.from(context)
    private val TYPE_SENDER = 0
    private val TYPE_RECEIVER = 1
    private val TYPE_MAX_COUNT = TYPE_RECEIVER + 1

    fun swapItem(ls : ArrayList<Message>){
        list = ls
        notifyDataSetChanged()
    }
    override fun getView(position: Int, altView: View?, parent: ViewGroup?): View {
        var holder: ViewHolder
        var convertView = altView
        val type = getItemViewType(position)
        holder = ViewHolder()
        if (convertView == null) {
            when(type){
                TYPE_SENDER ->  ts@{
                    convertView = mInflater.inflate(R.layout.row_text_sender, null)
                    holder.message = convertView.findViewById(R.id.tv_msg)
                    holder.time = convertView.findViewById(R.id.tv_time)
                }

                TYPE_RECEIVER -> tr@{
                    convertView = mInflater.inflate(R.layout.row_text_receiver,  null)
                    holder.message = convertView.findViewById(R.id.tv_msg)
                    holder.name = convertView.findViewById(R.id.tv_sender_name)
                    holder.time = convertView.findViewById(R.id.tv_time)
                }
            }
            convertView!!.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }
        if(holder.name != null){
            holder.name!!.text = friend.username
        }
        holder.time!!.text = list[position].created_at
        holder.message!!.text = list[position].message
        return convertView
    }

    override fun getItemViewType(position: Int): Int {
        return if(list[position].from == user.id) TYPE_SENDER else TYPE_RECEIVER
    }

    override fun getViewTypeCount(): Int {
        return TYPE_MAX_COUNT
    }
    override fun getItem(position: Int): Any {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return list[position].id!!.toLong()
    }

    override fun getCount(): Int {
        return list.size
    }
    inner class ViewHolder{
        var name : TextView? = null
        var message : TextView? = null
        var time : TextView? = null
    }

}