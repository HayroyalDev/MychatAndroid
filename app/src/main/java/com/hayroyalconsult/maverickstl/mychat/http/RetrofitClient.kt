package com.hayroyalconsult.maverickstl.mychat.http

import android.content.Context
import com.hayroyalconsult.maverickstl.mychat.BuildConfig
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Created by robot on 5/10/18.
 */
    class RetrofitClient(private val context: Context, url: String) {
        var apiService: Api? = null

        init {

            val interceptor = HttpLoggingInterceptor()
            if (BuildConfig.DEBUG)
                interceptor.level = HttpLoggingInterceptor.Level.BODY
            else
                interceptor.level = HttpLoggingInterceptor.Level.NONE

            val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(url)
                    .build()

            apiService = retrofit.create(Api::class.java)
        }

        companion object {
            private var retrofitClient: RetrofitClient? = null
            //public static  String Defaulthost = "http://gacserver.000webhostapp.com/api/";
            //public static String Defaulthost = "http://gacpedro.com.ng/api/";
            var Defaulthost = "http://192.168.8.100:8000/api/"

            fun getInstance(ctx: Context, url: String): RetrofitClient {
                if (retrofitClient == null) {
                    retrofitClient = RetrofitClient(ctx, url)

                }
                return retrofitClient!!
            }
        }
    }
