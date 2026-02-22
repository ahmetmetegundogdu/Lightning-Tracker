package com.gnd.lightningtracker

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
import org.json.JSONObject

var newCon = true
var firstCon: Boolean = true
var previousLastItem = ""

class WebSocketService : Service() {
    private var socket: WebSocket? = null
    private var currentTime : Long = 0
    private var lastItem = ""

    override fun onBind(intent: Intent?): IBinder {
        TODO("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()
        connect()
        Log.d("zamazingo", "dkjldfjfgjlşgklhkifşlfi")
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
                webSocket.send("""{"a":111}""")
                currentTime = System.currentTimeMillis()*1000000
                newCon = true
                response.close()
            }

            @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("websocket", "mesage received")
                val data = decode(text)
                val jsonObject = JSONObject(data)
                onMessageReceived(jsonObject)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocket", "Kapanıyor: $code")

                previousLastItem = lastItem

                Handler(Looper.getMainLooper()).postDelayed({
                    connect()
                }, 500)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocket", "Bağlantı Hatası: ${t.message}")
                response?.close()
                Handler(Looper.getMainLooper()).postDelayed({
                    connect()
                }, 500)
            }
        }

        socket = client.newWebSocket(request, listener)
    }

    private fun onMessageReceived(message: JSONObject) {
        val intent = Intent("WS_MESSAGE")

        intent.putExtra("lat", message.optDouble("lat"))
        intent.putExtra("lon", message.optDouble("lon"))

        lastItem = "${message.optInt("mds")}${message.getInt("mcg")}"

        val uniqueLightning = (lastItem == previousLastItem)
        val timeThreshold = message.optDouble("delay") > 10

        intent.putExtra("uniqueLightning",uniqueLightning)
        intent.putExtra("timeThreshold",timeThreshold)

        intent.setPackage(packageName)
        sendBroadcast(intent)
    }

    private fun decode(input: String): String {
        try {
            if (input.isEmpty()) return ""

            val dictionary = mutableMapOf<Int, String>()
            val chars = input.toCharArray()
            var c = chars[0].toString()
            var f = c
            val result = mutableListOf<String>()
            result.add(c)

            val h = 256
            var o = h

            var i = 1
            while (i < chars.size) {
                val a = chars[i].code
                val entry = if (a < h) {
                    chars[i].toString()
                } else {
                    dictionary[a] ?: (f + c)
                }

                result.add(entry)
                c = entry[0].toString()
                dictionary[o] = f + c
                o++
                f = entry
                i++
            }
            return result.joinToString("")
        } catch (e: Exception) {
            Log.e("Decode", "Hata: ${e.message}")
            return "{}"
        }
    }
}

