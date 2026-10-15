package com.example.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

enum class ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

class BotWebSocketClient(
    private val onMessageReceived: (String) -> Unit,
    private val onConnectionChanged: (ConnectionStatus, String?) -> Unit
) {
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .connectTimeout(5, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private val _status = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val status: StateFlow<ConnectionStatus> = _status.asStateFlow()

    fun connect(url: String) {
        disconnect()
        _status.value = ConnectionStatus.CONNECTING
        onConnectionChanged(ConnectionStatus.CONNECTING, null)

        val cleanUrl = if (!url.startsWith("ws://") && !url.startsWith("wss://")) {
            "ws://$url"
        } else url

        val request = try {
            Request.Builder().url(cleanUrl).build()
        } catch (e: Exception) {
            _status.value = ConnectionStatus.ERROR
            onConnectionChanged(ConnectionStatus.ERROR, "URL inválida: ${e.message}")
            return
        }

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _status.value = ConnectionStatus.CONNECTED
                onConnectionChanged(ConnectionStatus.CONNECTED, null)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                onMessageReceived(text)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
                _status.value = ConnectionStatus.DISCONNECTED
                onConnectionChanged(ConnectionStatus.DISCONNECTED, "Desconectado")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _status.value = ConnectionStatus.ERROR
                onConnectionChanged(ConnectionStatus.ERROR, t.message ?: "Fallo de conexión")
            }
        })
    }

    fun send(text: String): Boolean {
        return webSocket?.send(text) ?: false
    }

    fun disconnect() {
        try {
            webSocket?.close(1000, "Cerrado por el usuario")
            webSocket = null
        } catch (_: Exception) {}
        _status.value = ConnectionStatus.DISCONNECTED
        onConnectionChanged(ConnectionStatus.DISCONNECTED, null)
    }
}
