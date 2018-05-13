package com.hayroyalconsult.maverickstl.mychat.utility

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.hayroyalconsult.maverickstl.mychat.models.Message
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by robot on 5/10/18.
 */
class DbHelper(val context: Context) {
    private var ourContext: Context? = null
    private var helper: SqlHelper? = null
    private var db: SQLiteDatabase? = null
    internal var TAG = "DBHELPER"
    internal val DATABASE_NAME = "mychat.db"
    private val DB_PATH = "/data/data/com.hayroyalconsult.maverickstl.mychat/databases/"

    init {
        ourContext = context
    }

    @Throws(SQLException::class)
    fun open(): DbHelper {
        helper = SqlHelper(context)
        db = helper!!.writableDatabase
        return this
    }

    fun close() {
        db!!.close()
    }

    inner class SqlHelper(context: Context, private var dbFile: File = File(DB_PATH + DATABASE_NAME)) : SQLiteOpenHelper(context, DATABASE_NAME, null, 1) {

        @Synchronized
        override fun getWritableDatabase(): SQLiteDatabase {
            //Log.e(TAG, DB_PATH)
            if (!dbFile.exists()) {
                val db = super.getWritableDatabase()
                copyDataBase(db.path)
            }
            return super.getWritableDatabase()
        }

        @Synchronized
        override fun getReadableDatabase(): SQLiteDatabase {
            if (!dbFile.exists()) {
                val db = super.getReadableDatabase()
                copyDataBase(db.path)
            }
            return super.getReadableDatabase()
        }

        private fun copyDataBase(dbPath: String) {
            try {
                val assestDB = context.assets.open(DATABASE_NAME)
                val appDB = FileOutputStream(dbPath, false)

                val buffer = ByteArray(1024)
                var length: Int = assestDB.read(buffer)
                while (length > 0) {
                    appDB.write(buffer, 0, length)
                    length = assestDB.read(buffer)
                }

                appDB.flush()
                appDB.close()
                assestDB.close()
                Log.e(TAG, " Database Copied")
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e(TAG, e.toString())
            }

        }

        override fun onCreate(db: SQLiteDatabase) {

        }

        override fun onUpgrade(db: SQLiteDatabase, i: Int, i1: Int) {

        }

        override fun close() {
            db!!.close()
        }

    }

    //GET Data From Database
    fun truncateMessage(){
        try{
            db!!.rawQuery("DELETE from messages", null)
            Log.e(TAG, "Truncated")
        }catch (ex : Exception){
            Log.e(TAG, ex.toString())
        }
    }
    fun addMessage(list : Message){
        try{
            val cv = ContentValues()
            cv.put("mid", list.mid)
            cv.put("_from", list.from)
            cv.put("_to", list.to)
            cv.put("message", list.message)
            cv.put("status", list.status)
            cv.put("created_at", list.created_at)
            db!!.insert("messages", null, cv)
            Log.e(TAG," Message Added")
        }catch (ex: Exception){
            Log.e(TAG, ex.toString())

        }
    }

    fun updateStatus(mid: Int, status : Int){
        val query = "UPDATE messages set status=$status where mid=$mid"
        db!!.execSQL(query)
    }

    fun getMessageByMid(mid : Int) : Cursor{
        val query = "SELECT  * FROM $TABLE_OF_MESSAGE  WHERE mid LIKE $mid limit 1"
        return db!!.rawQuery(query,null)
    }

    fun getLastMessage(from : Int, to :Int) : Cursor{
        val query = "SELECT  * FROM $TABLE_OF_MESSAGE  WHERE (_from LIKE '$from' AND _to LIKE '$to') OR (_from LIKE '$to' AND _to LIKE '$from') ORDER BY id DESC limit 1"
        return db!!.rawQuery(query,null)
    }

    fun getChatHistory(from : Int, to : Int) : Cursor{
        val query = "SELECT  * FROM $TABLE_OF_MESSAGE  WHERE (_from LIKE '$from' AND _to LIKE '$to') OR (_from LIKE '$to' AND _to LIKE '$from') ORDER BY id ASC"
        return db!!.rawQuery(query,null)
    }
    val TABLE_OF_MESSAGE = "messages"
}
