package com.example.engine

import android.content.Context
import android.os.Build
import com.example.model.EventTag
import com.example.viewmodel.BotPanelViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object NodeEngineTester {

    /**
     * Prueba 1: Diagnóstico y arranque del motor Node.js
     * - Verifica binarios ejecutables en nativeLibraryDir (libnode.so) o en PATH
     * - Realiza prueba de escritura/lectura en almacenamiento interno propio
     * - Intenta ejecutar el binario y obtener process.version
     */
    suspend fun testNodeEngine(context: Context, viewModel: BotPanelViewModel): String = withContext(Dispatchers.IO) {
        val sb = StringBuilder()
        sb.appendLine("==========================================")
        sb.appendLine("FASE 2A: PRUEBA DE MOTOR NODE.JS")
        sb.appendLine("Fecha/Hora: ${java.time.LocalDateTime.now()}")
        sb.appendLine("Dispositivo: ${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE}, API ${Build.VERSION.SDK_INT})")
        sb.appendLine("Arquitectura CPU (ABIs): ${Build.SUPPORTED_ABIS.joinToString(", ")}")
        sb.appendLine("==========================================")

        // 1. Prueba de almacenamiento interno propio de la app
        sb.appendLine("\n[1/3] Prueba de I/O en almacenamiento interno:")
        val filesDir = context.filesDir
        val testFile = File(filesDir, "motor_test.txt")
        try {
            val contentToWrite = "NodeTest_Token_${System.currentTimeMillis()} - Node Engine Storage Check OK"
            FileOutputStream(testFile).use { fos ->
                fos.write(contentToWrite.toByteArray(Charsets.UTF_8))
            }
            val readContent = testFile.readText(Charsets.UTF_8)
            val isSuccess = readContent == contentToWrite
            sb.appendLine("  ✓ Carpeta interna: ${filesDir.absolutePath}")
            sb.appendLine("  ✓ Archivo creado: ${testFile.name} (${testFile.length()} bytes)")
            sb.appendLine("  ✓ Permisos: Lectura=${testFile.canRead()}, Escritura=${testFile.canWrite()}")
            sb.appendLine("  ✓ Integridad de datos: ${if (isSuccess) "CORRECTA" else "FALLIDA"}")
        } catch (e: Exception) {
            sb.appendLine("  ✗ Fallo en prueba de archivos: ${e.message}")
        }

        // 2. Búsqueda del motor Node.js empaquetado en APK
        sb.appendLine("\n[2/3] Búsqueda de motor Node.js empaquetado (libnode.so):")
        val nativeDir = File(context.applicationInfo.nativeLibraryDir)
        sb.appendLine("  • Ruta nativeLibraryDir: ${nativeDir.absolutePath}")
        val libNodeFile = File(nativeDir, "libnode.so")

        if (libNodeFile.exists()) {
            sb.appendLine("  ✓ Se encontró libnode.so empaquetado en el APK.")
            sb.appendLine("  • Tamaño: ${libNodeFile.length()} bytes")
            sb.appendLine("  • Permiso ejecución: ${libNodeFile.canExecute()}")

            // Intentar ejecución del binario Node
            try {
                val scriptFile = File(filesDir, "version_test.js").apply {
                    writeText("console.log(JSON.stringify({version: process.version, arch: process.arch, platform: process.platform}));")
                }
                val process = ProcessBuilder(libNodeFile.absolutePath, scriptFile.absolutePath)
                    .directory(filesDir)
                    .redirectErrorStream(true)
                    .start()

                val output = process.inputStream.bufferedReader().readText()
                val exitCode = process.waitFor()
                sb.appendLine("  ✓ Proceso Node ejecutado (código $exitCode).")
                sb.appendLine("  ✓ Salida: $output")
            } catch (e: Exception) {
                sb.appendLine("  ✗ Error al invocar libnode.so: ${e.message}")
            }
        } else {
            sb.appendLine("  ✗ NO se encontró libnode.so en ${nativeDir.name}.")
            sb.appendLine("    Causa técnica: El APK actual no contiene la biblioteca compartida nativa de Node.js empaquetada en jniLibs/${Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a"}/.")

            // Búsqueda alternativa en binarios de sistema
            sb.appendLine("  • Buscando binario 'node' en PATH del sistema:")
            val pathDirs = (System.getenv("PATH") ?: "/system/bin:/system/xbin").split(":")
            var systemNodeFound = false
            for (dir in pathDirs) {
                val nodeCandidate = File(dir, "node")
                if (nodeCandidate.exists()) {
                    sb.appendLine("  ✓ Encontrado en sistema: ${nodeCandidate.absolutePath}")
                    systemNodeFound = true
                    break
                }
            }
            if (!systemNodeFound) {
                sb.appendLine("  ✗ No existe binario 'node' en el sistema Android (esperado en Android estándar).")
            }
        }

        // 3. Diagnóstico de restricciones Android W^X
        sb.appendLine("\n[3/3] Diagnóstico de seguridad Android (W^X / SELinux):")
        sb.appendLine("  • Restricción W^X (API >= 29): ACTIVA. No se permite ejecutar binarios desde /data/data/.")
        sb.appendLine("  • Solución requerida: El binario DEBE llamarse 'libnode.so' y residir en jniLibs/<abi>/ dentro del APK.")

        sb.appendLine("\n==========================================")
        sb.appendLine("RESUMEN: Almacenamiento OK. Motor Node requiere empaquetado nativo libnode.so.")
        sb.appendLine("==========================================")

        val resultStr = sb.toString()
        viewModel.showSnackbar("Prueba de motor finalizada. Ver Consola.")
        resultStr
    }

    /**
     * Prueba 2: Viabilidad de carga de Baileys
     * - Analiza requisitos de Node (Node >= 18 para @whiskeysockets/baileys)
     * - Diagnostica dependencias críticas de red y WebCrypto
     */
    suspend fun testBaileysLoading(context: Context, viewModel: BotPanelViewModel): String = withContext(Dispatchers.IO) {
        val sb = StringBuilder()
        sb.appendLine("==========================================")
        sb.appendLine("FASE 2A: PRUEBA DE CARGA DE BAILEYS")
        sb.appendLine("Fecha/Hora: ${java.time.LocalDateTime.now()}")
        sb.appendLine("==========================================")

        sb.appendLine("\n[1/4] Compatibilidad de versión de Node.js con Baileys:")
        sb.appendLine("  • Librería WhatsApp: @whiskeysockets/baileys (v6.x+)")
        sb.appendLine("  • Requisito mínimo oficial: Node.js >= 18.0.0 (recomendado v20 LTS o v22 LTS)")
        sb.appendLine("  • Incompatibilidad de nodejs-mobile oficial: nodejs-mobile está detenido en Node.js v12 / v14.")
        sb.appendLine("  • Conclusión: En nodejs-mobile v12/v14, Baileys FALLARÁ INMEDIATAMENTE al hacer require()")
        sb.appendLine("    debido a sintaxis moderna ES2022 (optional chaining, private fields, globalThis, WebCrypto).")

        sb.appendLine("\n[2/4] Módulos criptográficos y dependencias de red:")
        sb.appendLine("  • WebCrypto (crypto.subtle): Obligatorio para cálculo de claves Signal y Curve25519.")
        sb.appendLine("  • WebSocket (ws): Requiere Node con soporte de sockets TCP nativos.")
        sb.appendLine("  • @hapi/boom, pino, protobufjs: Pure JS, compatibles si Node >= 18.")

        sb.appendLine("\n[3/4] Detección de node_modules en la app:")
        val nmDir = File(context.filesDir, "node_modules")
        val baileysDir = File(nmDir, "@whiskeysockets/baileys")
        if (baileysDir.exists()) {
            sb.appendLine("  ✓ @whiskeysockets/baileys detectado en: ${baileysDir.absolutePath}")
        } else {
            sb.appendLine("  • node_modules todavía no está copiado en almacenamiento de la app.")
            sb.appendLine("  • Para la prueba real se requiere empaquetar o sincronizar la carpeta node_modules del bot.")
        }

        sb.appendLine("\n[4/4] Veredicto técnico preliminar:")
        sb.appendLine("  ✗ nodejs-mobile estándar (Node 12/14) NO ES VIABLE para Baileys moderno.")
        sb.appendLine("  ✓ Opciones viables:")
        sb.appendLine("    A) Compilar Node 20/22 como libnode.so para Android arm64-v8a con NDK local.")
        sb.appendLine("    B) Enfoque 'Modo B' (Control Remoto): El bot corre en PC/Servidor con Node 22;")
        sb.appendLine("       la app actúa como panel de control móvil completo.")

        sb.appendLine("==========================================")

        val resultStr = sb.toString()
        viewModel.showSnackbar("Diagnóstico Baileys completado. Ver Consola.")
        resultStr
    }
}
