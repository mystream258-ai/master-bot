package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.service.BotForegroundService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == "android.intent.action.QUICKBOOT_POWERON") {
            val prefs = context.getSharedPreferences("panel_bot_prefs", Context.MODE_PRIVATE)
            val autoStart = prefs.getBoolean("auto_start_boot", true)
            if (autoStart) {
                BotForegroundService.startService(
                    context,
                    title = "Atendiendo clientes",
                    sub = "Iniciado automáticamente al encender el teléfono."
                )
            }
        }
    }
}
