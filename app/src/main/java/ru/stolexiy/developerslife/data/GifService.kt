package ru.stolexiy.developerslife.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

private const val BASE_URL: String = "http://developerslife.ru/"

interface GifService {
    @GET("random?json=true")
    suspend fun getRandomGif(): Gif

    @GET("latest/{page}?json=true")
    suspend fun getLatestGifPage(@Path("page") page: Int): GifPage

    @GET("hot/{page}?json=true")
    suspend fun getHotGifPage(@Path("page") page: Int): GifPage

    @GET("top/{page}?json=true")
    suspend fun getTopGifPage(@Path("page") page: Int): GifPage

    companion object {

        fun create() : GifService {

            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build()
            return retrofit.create(GifService::class.java)

        }
    }
}