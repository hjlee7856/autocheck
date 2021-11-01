package com.example.genshin_autocheck

import retrofit2.Call
import retrofit2.http.Header
import retrofit2.http.POST

interface CheckAPI {
    @POST("event/sol/sign?act_id=e202102251931481&lang=ko-kr")
    fun putCookie(
        @Header("Cookie") cookies: String
    ): Call<Message>
}