package com.example.nodejs

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object NodeRunner {
    private const val TAG = "NodeRunner"
    private var isNativeLibraryLoaded = false

    init {
        try {
            System.loadLibrary("node")
            System.loadLibrary("nodejs-mobile-android")
            isNativeLibraryLoaded = true
            Log.i(TAG, "Bibliotecas nativas de nodejs-mobile cargadas con éxito.")
        } catch (e: UnsatisfiedLinkError) {
            isNativeLibraryLoaded = false
            Log.w(TAG, "Bibliotecas nativas (libnode.so / libnodejs-mobile-android.so) no encontradas en APK: ${e.message}")
        }
    }

    fun isEngineInstalled(): Boolean = isNativeLibraryLoaded

    /**
     * Copia la carpeta assets/nodejs-project al almacenamiento interno de la app
     */
    fun prepareNodeProject(context: Context): File {
        val targetDir = File(context.filesDir, "nodejs-project")
        if (!targetDir.exists()) {
            targetDir.mkdirs()
        }

        try {
            val assetList = context.assets.list("nodejs-project") ?: emptyArray()
            for (filename in assetList) {
                val destFile = File(targetDir, filename)
                context.assets.open("nodejs-project/$filename").use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error al copiar nodejs-project desde assets: ${e.message}")
        }

        return targetDir
    }

    /**
     * Inicia el motor Node con los argumentos especificados
     */
    fun startNode(context: Context, scriptName: String = "main.js"): Int {
        if (!isNativeLibraryLoaded) {
            Log.e(TAG, "No se puede iniciar Node: Motor no instalado.")
            return -1
        }

        val projectDir = prepareNodeProject(context)
        val scriptPath = File(projectDir, scriptName).absolutePath

        return try {
            nativeStartNodeWithArguments(arrayOf("node", scriptPath))
        } catch (e: Exception) {
            Log.e(TAG, "Fallo al llamar startNodeWithArguments: ${e.message}")
            -1
        }
    }

    // Firma JNI de nodejs-mobile
    private external fun nativeStartNodeWithArguments(arguments: Array<String>): Int
}
