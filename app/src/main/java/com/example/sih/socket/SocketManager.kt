package com.example.sih.socket

import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject

object SocketManager {

    private lateinit var socket: Socket

    fun initializeSocket(serverUrl: String) {
        socket = IO.socket(serverUrl)
    }

    fun connect() {
        socket.connect()
    }

    fun disconnect() {
        socket.disconnect()
    }

    fun emit(event: String, data: JSONObject) {
        socket.emit(event, data)
    }

    fun on(event: String, listener: (JSONObject) -> Unit) {
        socket.on(event) { args ->
            if (args.isNotEmpty()) {
                listener(args[0] as JSONObject)
            }
        }
    }

    fun off(event: String) {
        socket.off(event)
    }
}
