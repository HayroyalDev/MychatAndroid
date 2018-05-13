package com.hayroyalconsult.maverickstl.mychat.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.hayroyalconsult.maverickstl.mychat.R
import com.hayroyalconsult.maverickstl.mychat.models.User
import java.util.zip.Inflater

/**
 * Created by robot on 5/13/18.
 */
class SearchAdapter(context: Context, var list : ArrayList<User>) : BaseAdapter(){
    var mInflater = LayoutInflater.from(context)
    override fun getView(position: Int, altView: View?, parent: ViewGroup?): View {
        val holder: ViewHolder
        var convertView = altView
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.search_row, null)
            holder = ViewHolder()
            holder.username = convertView.findViewById(R.id.search_username)
            convertView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }
        holder.username!!.text = list[position].username
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
    }

}