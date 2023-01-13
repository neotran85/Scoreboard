package com.bidaappscoreboard.service

import android.content.Context
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.http.HttpRequest
import com.apollographql.apollo3.api.http.HttpResponse
import com.apollographql.apollo3.network.http.HttpInterceptor
import com.apollographql.apollo3.network.http.HttpInterceptorChain
import com.apollographql.apollo3.network.okHttpClient
import com.bidaappscoreboard.config.AppConfig
import com.bidaappscoreboard.store.HttpHeaders
import com.bidaappscoreboard.store.ScoreBoard
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.OkHttpClient

//Note: The singleton is handled here manually for sample purposes. In a real-life application,
// you would want to use a dependency injection framework such as Dagger or Koin.
var apolloClient: ApolloClient? = null

fun initApolloClient(context: Context): ApolloClient {
    val okHttpClient = OkHttpClient.Builder().build()

    apolloClient = ApolloClient.Builder()
        .serverUrl(AppConfig.API_URL)
        .webSocketServerUrl(AppConfig.SOCKET_URL)
        .okHttpClient(okHttpClient)
        .addHttpInterceptor(AuthorizationInterceptor(context = context))
        .build()

    return apolloClient!!
}

private class AuthorizationInterceptor(val context: Context): HttpInterceptor {
    private val mutex = Mutex()

    override suspend fun intercept(
        request: HttpRequest,
        chain: HttpInterceptorChain
    ) : HttpResponse {

        val response = prepareRequest(context, request, chain)

        return if (response.statusCode == 401) {
            val token = mutex.withLock {
                ScoreBoard.apiRenewToken()
            }

            prepareRequest(context, request, chain)
        } else {
            response
        }
    }
}

private suspend fun prepareRequest(context: Context, request: HttpRequest, chain: HttpInterceptorChain) : HttpResponse {
    val token = ScoreBoard.token
    val refreshToken = ScoreBoard.renewToken
    val headers = HttpHeaders.toJson()
    val newRequest = request.newBuilder()

    // if token -> set auth header
    if (token != null) {
        newRequest.addHeader("Authorization","Bearer $token")
    }

    // if refreshToken -> set cookie header
    if (refreshToken != null) {
        newRequest.addHeader("Cookie", refreshToken ?: "")
    }

    // set device headers
    for ((key, value) in headers) {
        newRequest.addHeader(key, value ?: "")
    }

    return chain.proceed(newRequest.build())
}