package com.hayroyalconsult.maverickstl.mychat.http

import com.hayroyalconsult.maverickstl.mychat.models.Message
import com.hayroyalconsult.maverickstl.mychat.models.User
import com.hayroyalconsult.maverickstl.mychat.response.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import rx.Observable

/**
 * Created by robot on 5/10/18.
 */
interface Api{
    @FormUrlEncoded
    @POST("user/installations")
    fun postInstallations(@Field("UUID") UUID: String, @Field("phoneType") PhoneType: String, @Field("androidID") AndroidID: String): Observable<String>

    @FormUrlEncoded
    @POST("user/create")
    fun createOrLogUser(@Field("type") type: String, @Field("username") username : String, @Field("password") password: String): Observable<Response<User>>

    @FormUrlEncoded
    @POST("user/get")
    fun getUsers(@Field("ids") ids: String): Observable<Response<User>>

    @FormUrlEncoded
    @POST("message/undelivered")
    fun getUndeliveredMessage(@Field("to") type: Int): Observable<Response<Message>>

    @FormUrlEncoded
    @POST("message/status")
    fun setMessageStatus(@Field("mid") mid: String, @Field("status") status : Int): Observable<Response<Message>>

    @FormUrlEncoded
    @POST("message/create")
    fun sendMessage(@Field("mid") mid: Int, @Field("from") from : Int, @Field("to") to : Int, @Field("message") message : String): Observable<Response<Message>>

    @FormUrlEncoded
    @POST("user/search")
    fun searchResult(@Field("value") value: String) : Observable<Response<User>>

}
