package com.gnd.flashtracker

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

public var newCon = true


class WebSocketService : Service() {
    private var socket: WebSocket? = null
    private var currentTime : Long = 0

    override fun onBind(intent: Intent?): IBinder {
        TODO("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()
        connect()
        Log.d("zamazingo", "dkjldfjfgjlşgklhkifşlfi")
    }
    override fun onDestroy() {
        super.onDestroy()
    }

    fun connect(){
        val client = OkHttpClient()

        currentTime = 0

        val request = Request.Builder()
            .url("wss://ws1.blitzortung.org/") // URL
            .build()

        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocket", "Bağlandı!")
                response.close()
                webSocket.send("""{"a":111}""")
                currentTime = System.currentTimeMillis()*1000000
                newCon = true
            }

            @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("websocket", "mesage received")
                onMessageReceived(text)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocket", "Kapanıyor: $code")
                Handler(Looper.getMainLooper()).postDelayed({
                    connect()
                }, 1000)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocket", "Bağlantı Hatası: ${t.message}")
                response?.close()
                Handler(Looper.getMainLooper()).postDelayed({
                    connect()
                }, 1000)
            }
        }

        socket = client.newWebSocket(request, listener)
    }

    fun sendMessage(msg: String) {
        socket?.send(msg)
    }

    private fun onMessageReceived(message: String) {
        val intent = Intent("WS_MESSAGE")
        intent.putExtra("text", message)
        intent.putExtra("currentTime", currentTime)
        intent.setPackage(packageName)
        sendBroadcast(intent)
    }

}
