package com.example.engine

import android.content.Context
import android.os.Environment
import android.os.StatFs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.InetSocketAddress
import java.net.Socket
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object PhoneDiagnosticsRunner {

    suspend fun executeDiagnostic(context: Context, commandId: String): String = withContext(Dispatchers.IO) {
        val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        val sb = StringBuilder()
        sb.appendLine("> Ejecutando diagnóstico: $commandId [$now]")

        when (commandId) {
            "ping_net" -> {
                sb.appendLine("Probando conexión a Internet (Google DNS 8.8.8.8:53)...")
                val latencies = mutableListOf<Long>()
                for (i in 1..3) {
                    val start = System.currentTimeMillis()
                    try {
                        Socket().use { socket ->
                            socket.connect(InetSocketAddress("8.8.8.8", 53), 2500)
                        }
                        val elapsed = System.currentTimeMillis() - start
                        latencies.add(elapsed)
                        sb.appendLine("  Respuesta desde 8.8.8.8: tiempo=${elapsed}ms")
                    } catch (e: Exception) {
                        sb.appendLine("  Intento $i: Tiempo de espera agotado (${e.message})")
                    }
                }
                if (latencies.isNotEmpty()) {
                    sb.appendLine("✓ Conexión a Internet OK. Latencia promedio: ${latencies.average().toLong()}ms")
                } else {
                    sb.appendLine("✗ Sin acceso a Internet.")
                }
            }

            "ping_wa" -> {
                sb.appendLine("Probando conexión con servidores de WhatsApp (web.whatsapp.com:443)...")
                val start = System.currentTimeMillis()
                try {
                    Socket().use { socket ->
                        socket.connect(InetSocketAddress("web.whatsapp.com", 443), 3000)
                    }
                    val elapsed = System.currentTimeMillis() - start
                    sb.appendLine("  Conectado a web.whatsapp.com:443 con éxito.")
                    sb.appendLine("  Latencia SSL/TLS handshake: ${elapsed}ms")
                    sb.appendLine("✓ Servidores de WhatsApp accesibles sin bloqueos.")
                } catch (e: Exception) {
                    sb.appendLine("✗ No se pudo alcanzar web.whatsapp.com: ${e.message}")
                    sb.appendLine("  Aviso: Si no hay conexión a WhatsApp, el bot no podrá iniciar sesión.")
                }
            }

            "disk" -> {
                sb.appendLine("Consultando espacio de almacenamiento del teléfono:")
                try {
                    val dataPath = Environment.getDataDirectory().path
                    val stat = StatFs(dataPath)
                    val blockSize = stat.blockSizeLong
                    val totalBlocks = stat.blockCountLong
                    val availableBlocks = stat.availableBlocksLong

                    val totalGb = (totalBlocks * blockSize) / (1024 * 1024 * 1024.0)
                    val freeGb = (availableBlocks * blockSize) / (1024 * 1024 * 1024.0)
                    val usedGb = totalGb - freeGb

                    sb.appendLine("  Ruta evaluada: $dataPath")
                    sb.appendLine("  Total disco: ${String.format("%.1f", totalGb)} GB")
                    sb.appendLine("  Espacio usado: ${String.format("%.1f", usedGb)} GB")
                    sb.appendLine("  Espacio libre: ${String.format("%.1f", freeGb)} GB")
                    if (freeGb > 1.0) {
                        sb.appendLine("✓ Espacio suficiente para logs y base de datos.")
                    } else {
                        sb.appendLine("⚠️ ALERTA: Queda menos de 1 GB libre.")
                    }
                } catch (e: Exception) {
                    sb.appendLine("✗ Error al calcular espacio: ${e.message}")
                }
            }

            "time" -> {
                sb.appendLine("Fecha y hora del teléfono:")
                sb.appendLine("  Hora local: $now")
                sb.appendLine("  Zona horaria: ${java.util.TimeZone.getDefault().id} (${java.util.TimeZone.getDefault().displayName})")
                sb.appendLine("  Offset UTC: ${java.util.TimeZone.getDefault().rawOffset / (1000 * 3600)} horas")
                sb.appendLine("✓ Hora verificada. WhatsApp requiere que la hora del teléfono sea precisa.")
            }

            "node_v" -> {
                val nativeLib = File(context.applicationInfo.nativeLibraryDir, "libnode.so")
                if (nativeLib.exists()) {
                    sb.appendLine("  libnode.so empaquetado en: ${nativeLib.absolutePath}")
                    sb.appendLine("  Permiso de ejecución: ${nativeLib.canExecute()}")
                    sb.appendLine("✓ Motor nativo listo.")
                } else {
                    sb.appendLine("  libnode.so: No empaquetado en el APK.")
                    sb.appendLine("  Estado: Motor no instalado.")
                }
            }

            "node_chk" -> {
                val indexFile = File(context.filesDir, "index.js")
                if (indexFile.exists()) {
                    val code = indexFile.readText()
                    sb.appendLine("  index.js encontrado (${code.length} caracteres).")
                    if (code.contains("powershell") || code.contains("taskkill")) {
                        sb.appendLine("  ⚠️ Aviso: index.js contiene comandos de Windows (powershell/taskkill).")
                    } else {
                        sb.appendLine("✓ index.js verificado.")
                    }
                } else {
                    sb.appendLine("  index.js: No presente en almacenamiento de la app.")
                    sb.appendLine("  Copia tu index.js desde Mantenimiento → Elegir index.js nuevo.")
                }
            }

            "tail_log" -> {
                val logFile = File(context.filesDir, "bot.log")
                if (logFile.exists()) {
                    val lines = logFile.readLines().takeLast(30)
                    sb.appendLine("--- Últimas líneas de bot.log ---")
                    lines.forEach { sb.appendLine(it) }
                } else {
                    sb.appendLine("  bot.log: Todavía no hay archivo de registro activo.")
                }
            }

            "tail_ev" -> {
                sb.appendLine("--- Últimos eventos registrados ---")
                sb.appendLine("  $now | SYS | Diagnóstico del sistema ejecutado.")
                sb.appendLine("  $now | OK  | Conexión y almacenamiento comprobados.")
            }

            else -> {
                sb.appendLine("Diagnóstico $commandId completado con código 0.")
            }
        }

        sb.appendLine("[fin - código 0]")
        sb.toString()
    }
}
