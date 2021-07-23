package com.example.githubsearch.service

import com.google.gson.GsonBuilder
import okhttp3.*
import java.util.concurrent.TimeUnit

object GithubService {

    private var gson = GsonBuilder().create()

    fun getGson() = gson

     private fun makeRequest(url: String): Request {
        return Request
            .Builder()
            .header("Accept", "application/json")
            .url(url)
            .build()
    }

    private val client = OkHttpClient()
        .newBuilder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    fun search(url: String, cb: Callback) {
        client.newCall(makeRequest(url)).enqueue(cb)
    }
}