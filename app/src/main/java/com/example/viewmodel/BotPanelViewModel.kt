package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.engine.NodeEngineTester
import com.example.engine.PhoneDiagnosticsRunner
import com.example.model.BotEvent
import com.example.model.BotStatus
import com.example.model.CheckPointState
import com.example.model.DiagnosticItem
import com.example.model.EventTag
import com.example.model.MediaResourceItem
import com.example.model.PreparationChecklist
import com.example.model.SavedBotCommand
import com.example.model.SmartAlertsState
import com.example.network.BotWebSocketClient
import com.example.network.ConnectionStatus
import com.example.service.BotForegroundService
import com.example.storage.MediaStorageManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

sealed class ConfirmationDialogType {
    data class DestructiveCommand(val command: String, val message: String) : ConfirmationDialogType()
    object StopBotWhileServing : ConfirmationDialogType()
    object RestartBotWhileServing : ConfirmationDialogType()
    object ResetSession : ConfirmationDialogType()
}

class BotPanelViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context get() = getApplication<Application>().applicationContext
    private val prefs = context.getSharedPreferences("panel_bot_prefs", Context.MODE_PRIVATE)

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    private fun currentTime(): String = LocalTime.now().format(timeFormatter)

    // Estado principal del bot
    private val _status = MutableStateFlow<BotStatus>(BotStatus.Stopped)
    val status: StateFlow<BotStatus> = _status.asStateFlow()

    // Checklist de preparación
    private val _checklist = MutableStateFlow(
        PreparationChecklist(
            nodeState = CheckPointState.OK,
            nodeLabel = "v22",
            depsState = CheckPointState.OK,
            depsLabel = "OK",
            indexState = CheckPointState.OK,
            indexLabel = "Listo",
            sessionState = CheckPointState.WAITING,
            sessionLabel = "Inactivo"
        )
    )
    val checklist: StateFlow<PreparationChecklist> = _checklist.asStateFlow()

    // Avisos inteligentes
    private val _alerts = MutableStateFlow(SmartAlertsState())
    val alerts: StateFlow<SmartAlertsState> = _alerts.asStateFlow()

    // Pestaña activa en pantalla Actividad (resumen, clientes, alertas, detalle)
    private val _activeActivityTab = MutableStateFlow("resumen")
    val activeActivityTab: StateFlow<String> = _activeActivityTab.asStateFlow()

    // Eventos de muestra iniciales (incluye todos los tags requeridos)
    private val _events = MutableStateFlow<List<BotEvent>>(
        listOf(
            BotEvent("1", "16:15:02", EventTag.ONLINE, "Conectado a WhatsApp. El bot está atendiendo clientes."),
            BotEvent("2", "16:14:50", EventTag.OK, "Librerías verificadas e inicializadas correctamente."),
            BotEvent("3", "16:12:44", EventTag.CLI, "Cliente atendido (+52155492...): «Información de precios y planes»."),
            BotEvent("4", "16:08:10", EventTag.CLI, "Cliente VIP detectado (+521998...): Catálogo y video enviados."),
            BotEvent("5", "16:05:00", EventTag.CODE, "ABCD-1234 (Código de vinculación generado y listo)."),
            BotEvent("6", "16:04:15", EventTag.PAIR, "Solicitud de vinculación enviada para el número 5215512345678."),
            BotEvent("7", "15:58:30", EventTag.WARN, "Aviso: la ruta de almacenamiento se redirigió a la carpeta propia de la app."),
            BotEvent("8", "15:56:12", EventTag.ERR, "Error previo resuelto: reconexión automática ejecutada con éxito."),
            BotEvent("9", "15:55:00", EventTag.SYS, "Supervisor iniciado (PID 4920). Verificando entorno Android."),
            BotEvent("10", "15:54:10", EventTag.INFO, "Detalle interno: Socket handshake finalizado con latencia 48ms.")
        )
    )
    val events: StateFlow<List<BotEvent>> = _events.asStateFlow()

    // Consola del bot
    private val _consoleOutput = MutableStateFlow(
        "[16:15:02] Bot iniciado correctamente.\n" +
        "[16:15:05] Baileys: conectado y listo.\n" +
        "[16:15:06] Esperando interacciones de clientes...\n" +
        "[16:17:20] Cliente atendido: +52155492..."
    )
    val consoleOutput: StateFlow<String> = _consoleOutput.asStateFlow()

    // Comandos de WhatsApp oficiales
    val defaultBotCommands = listOf(
        SavedBotCommand("ADMIN", "ADMIN", isDestructive = false),
        SavedBotCommand("ACTUALIZAR", "ACTUALIZAR", isDestructive = true),
        SavedBotCommand("EDITAR DISPOSITIVO", "EDITAR DISPOSITIVO", isDestructive = false),
        SavedBotCommand("EDITAR MENSAJES", "EDITAR MENSAJES", isDestructive = false),
        SavedBotCommand("CLIENTES", "CLIENTES", isDestructive = false),
        SavedBotCommand("RESET", "RESET", isDestructive = true),
        SavedBotCommand("DOWN", "DOWN", isDestructive = true),
        SavedBotCommand("RUN", "RUN", isDestructive = false),
        SavedBotCommand("DOOMSDAY", "DOOMSDAY", isDestructive = true),
        SavedBotCommand("PATH", "PATH", isDestructive = true)
    )

    // "Mi lista" personalizada en la consola
    private val _userCustomCommands = MutableStateFlow(
        listOf(
            SavedBotCommand("SALUDO", "Hola, bot funcionando", isDestructive = false)
        )
    )
    val userCustomCommands: StateFlow<List<SavedBotCommand>> = _userCustomCommands.asStateFlow()

    // Diagnósticos seguros del teléfono
    val diagnosticItems = listOf(
        DiagnosticItem("node_v", "Versión de Node.js", "node -v", "Confirma la presencia del motor Node.js"),
        DiagnosticItem("node_chk", "Revisar sintaxis de index.js", "node --check index.js", "Valida que el archivo no contenga errores de sintaxis"),
        DiagnosticItem("tail_log", "Ver últimas líneas del log", "tail -n 40 logs/bot.log", "Muestra la actividad cruda reciente del proceso"),
        DiagnosticItem("tail_ev", "Ver últimos eventos", "tail -n 30 logs/eventos.log", "Muestra el registro de eventos clasificados"),
        DiagnosticItem("ping_net", "Probar internet (Google DNS)", "ping -c 3 8.8.8.8", "Comprueba conectividad global"),
        DiagnosticItem("ping_wa", "Probar conexión a WhatsApp", "ping -c 3 web.whatsapp.com", "Comprueba acceso a los servidores de WhatsApp"),
        DiagnosticItem("disk", "Espacio libre en almacenamiento", "df -h /data", "Espacio disponible en la memoria del teléfono"),
        DiagnosticItem("time", "Hora y fecha del sistema", "date", "Comprueba sincronización horaria precisa")
    )

    private val _terminalOutput = MutableStateFlow(
        "\$ term-diag --safe\n" +
        "Diagnósticos seguros predefinidos para Android.\n" +
        "Listo para ejecutar."
    )
    val terminalOutput: StateFlow<String> = _terminalOutput.asStateFlow()

    private val _isTerminalRunning = MutableStateFlow(false)
    val isTerminalRunning: StateFlow<Boolean> = _isTerminalRunning.asStateFlow()

    // Recursos multimedia (sincronizados con almacenamiento real)
    private val _mediaResources = MutableStateFlow<List<MediaResourceItem>>(emptyList())
    val mediaResources: StateFlow<List<MediaResourceItem>> = _mediaResources.asStateFlow()

    // Ajustes persistidos
    private val _autoStartOnBoot = MutableStateFlow(prefs.getBoolean("auto_start_boot", true))
    val autoStartOnBoot: StateFlow<Boolean> = _autoStartOnBoot.asStateFlow()

    private val _driveId = MutableStateFlow(prefs.getString("drive_id", "1AbC_xYz9876543210-sample") ?: "")
    val driveId: StateFlow<String> = _driveId.asStateFlow()

    private val _engineMode = MutableStateFlow(prefs.getString("engine_mode", "local") ?: "local")
    val engineMode: StateFlow<String> = _engineMode.asStateFlow()

    private val _remoteWsUrl = MutableStateFlow(prefs.getString("remote_ws_url", "ws://192.168.1.50:3000") ?: "ws://192.168.1.50:3000")
    val remoteWsUrl: StateFlow<String> = _remoteWsUrl.asStateFlow()

    // Cliente WebSocket para Modo B
    private val wsClient = BotWebSocketClient(
        onMessageReceived = { message -> handleRemoteMessage(message) },
        onConnectionChanged = { status, error ->
            if (status == ConnectionStatus.CONNECTED) {
                showSnackbar("Conectado con éxito a la PC remota.")
                addEvent(EventTag.ONLINE, "Conexión establecida con el bot en PC.")
            } else if (status == ConnectionStatus.ERROR) {
                showSnackbar("Error de conexión remota: ${error ?: "Desconocido"}")
            }
        }
    )

    // Diálogos de confirmación
    private val _activeConfirmationDialog = MutableStateFlow<ConfirmationDialogType?>(null)
    val activeConfirmationDialog: StateFlow<ConfirmationDialogType?> = _activeConfirmationDialog.asStateFlow()

    // Mensaje para Snackbar
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    init {
        refreshMediaResources()
    }

    private fun refreshMediaResources() {
        viewModelScope.launch {
            val list = MediaStorageManager.getResourceItems(context)
            _mediaResources.value = list
            val missing = list.count { !it.isPresent }
            _alerts.update { it.copy(showMediaWarning = missing > 0) }
        }
    }

    // Acciones de control de estado
    fun onStartBot() {
        if (_engineMode.value == "remote") {
            wsClient.send("{\"action\": \"start\"}")
        }
        viewModelScope.launch {
            _status.value = BotStatus.Starting
            BotForegroundService.startService(context, "Iniciando…", "El bot se está conectando a WhatsApp.")
            addEvent(EventTag.SYS, "Iniciando el bot…")
            delay(1200)
            _status.value = BotStatus.Online(connectedSince = currentTime(), clientMovementsToday = 14)
            BotForegroundService.startService(context, "Atendiendo clientes", "Conectado a WhatsApp en segundo plano.")
            _checklist.update { it.copy(sessionState = CheckPointState.OK, sessionLabel = "Vinculado") }
            addEvent(EventTag.ONLINE, "Conectado a WhatsApp. El bot está atendiendo clientes.")
            showSnackbar("Bot conectado y atendiendo")
        }
    }

    fun requestStopBot() {
        if (_status.value is BotStatus.Online) {
            _activeConfirmationDialog.value = ConfirmationDialogType.StopBotWhileServing
        } else {
            confirmStopBot()
        }
    }

    fun confirmStopBot() {
        if (_engineMode.value == "remote") {
            wsClient.send("{\"action\": \"stop\"}")
        }
        _status.value = BotStatus.Stopped
        BotForegroundService.stopService(context)
        _checklist.update { it.copy(sessionState = CheckPointState.WAITING, sessionLabel = "Inactivo") }
        addEvent(EventTag.SYS, "Bot detenido desde el panel.")
        showSnackbar("Bot detenido.")
    }

    fun requestRestartBot() {
        if (_status.value is BotStatus.Online) {
            _activeConfirmationDialog.value = ConfirmationDialogType.RestartBotWhileServing
        } else {
            confirmRestartBot()
        }
    }

    fun confirmRestartBot() {
        viewModelScope.launch {
            _status.value = BotStatus.Restarting
            BotForegroundService.startService(context, "Reiniciando…", "El bot se reinicia solo en unos segundos.")
            addEvent(EventTag.SYS, "Reiniciando el bot…")
            delay(1300)
            _status.value = BotStatus.Online(connectedSince = currentTime(), clientMovementsToday = 14)
            BotForegroundService.startService(context, "Atendiendo clientes", "Conectado a WhatsApp.")
            addEvent(EventTag.ONLINE, "Conectado a WhatsApp tras reinicio.")
            showSnackbar("Bot reiniciado con éxito")
        }
    }

    fun requestResetSession() {
        _activeConfirmationDialog.value = ConfirmationDialogType.ResetSession
    }

    fun confirmResetSession() {
        _status.value = BotStatus.PairingNeedNumber
        _checklist.update { it.copy(sessionState = CheckPointState.WAITING, sessionLabel = "Sin vincular") }
        addEvent(EventTag.WARN, "Sesión borrada. Se requiere vincular el número de WhatsApp.")
        showSnackbar("Sesión borrada. Escribe tu número para vincular.")
    }

    fun dismissConfirmationDialog() {
        _activeConfirmationDialog.value = null
    }

    // Flujo de vinculación
    fun submitPhoneNumber(number: String) {
        if (number.length < 8) {
            showSnackbar("Escribe el número completo con código de país, sin «+».")
            return
        }
        if (_engineMode.value == "remote") {
            wsClient.send("{\"action\": \"command\", \"text\": \"$number\"}")
        }
        viewModelScope.launch {
            addEvent(EventTag.PAIR, "Número de WhatsApp enviado: $number")
            showSnackbar("Número enviado al bot. Generando código…")
            delay(900)
            val newCode = "ABCD-1234"
            _status.value = BotStatus.PairingCodeReady(newCode)
            addEvent(EventTag.CODE, "$newCode (Escribe este código en tu teléfono).")
        }
    }

    // Consola del bot
    fun sendCommandToBot(commandText: String) {
        val trimmed = commandText.trim()
        if (trimmed.isEmpty()) return

        val matchedCmd = defaultBotCommands.find { it.text.equals(trimmed, ignoreCase = true) }
        if (matchedCmd != null && matchedCmd.isDestructive) {
            _activeConfirmationDialog.value = ConfirmationDialogType.DestructiveCommand(
                command = trimmed,
                message = "«$trimmed» es un comando crítico que alterará el estado o los archivos del bot. ¿Deseas enviarlo de todos modos?"
            )
            return
        }

        executeCommandSend(trimmed)
    }

    fun confirmDestructiveCommand(commandText: String) {
        executeCommandSend(commandText)
    }

    private fun executeCommandSend(commandText: String) {
        if (_engineMode.value == "remote") {
            wsClient.send("{\"action\": \"command\", \"text\": \"$commandText\"}")
        }
        _consoleOutput.update { current ->
            "$current\n> $commandText\n[Sistema] Dato enviado al bot."
        }
        addEvent(EventTag.SYS, "Comando enviado al bot: $commandText")
        showSnackbar("Comando enviado al bot.")
    }

    fun saveUserCustomCommand(name: String, text: String) {
        if (name.isBlank() || text.isBlank()) {
            showSnackbar("Escribe un nombre y el texto a guardar.")
            return
        }
        _userCustomCommands.update { current ->
            current.filterNot { it.name.equals(name, ignoreCase = true) } +
                    SavedBotCommand(name, text, isDestructive = false)
        }
        showSnackbar("Guardado en tu lista: $name")
    }

    fun deleteUserCustomCommand(command: SavedBotCommand) {
        _userCustomCommands.update { current -> current - command }
        showSnackbar("Quitado de tu lista: ${command.name}")
    }

    // Diagnósticos del teléfono (ejecución real de telemetría y red)
    fun runSafeDiagnostic(item: DiagnosticItem) {
        if (_isTerminalRunning.value) {
            showSnackbar("Ya hay un diagnóstico en ejecución.")
            return
        }
        viewModelScope.launch {
            _isTerminalRunning.value = true
            _terminalOutput.update { current ->
                "$current\n\n$ ${item.command}\n[Ejecutando diagnóstico...]"
            }
            val result = PhoneDiagnosticsRunner.executeDiagnostic(context, item.id)
            _terminalOutput.update { current ->
                "$current\n$result"
            }
            _isTerminalRunning.value = false
        }
    }

    fun stopSafeDiagnostic() {
        if (_isTerminalRunning.value) {
            _isTerminalRunning.value = false
            _terminalOutput.update { current ->
                "$current\n[fin - código -1] Detenido por el usuario."
            }
            showSnackbar("Diagnóstico detenido.")
        }
    }

    fun clearTerminalOutput() {
        _terminalOutput.value = "$ term-diag --safe\nListo."
        showSnackbar("Terminal limpia.")
    }

    // Diagnósticos Fase 2A: Pruebas de viabilidad de Node.js y Baileys
    fun runEngineTest(ctx: Context) {
        viewModelScope.launch {
            _consoleOutput.value = "[Prueba de Motor] Iniciando diagnóstico de viabilidad...\nEspere unos segundos..."
            val result = NodeEngineTester.testNodeEngine(ctx, this@BotPanelViewModel)
            _consoleOutput.value = result
            addEvent(EventTag.SYS, "Prueba de viabilidad de motor Node.js ejecutada.")
        }
    }

    fun runBaileysTest(ctx: Context) {
        viewModelScope.launch {
            _consoleOutput.value = "[Prueba de Baileys] Analizando compatibilidad de Baileys...\nEspere unos segundos..."
            val result = NodeEngineTester.testBaileysLoading(ctx, this@BotPanelViewModel)
            _consoleOutput.value = result
            addEvent(EventTag.SYS, "Prueba de compatibilidad de Baileys ejecutada.")
        }
    }

    // Gestión real de archivos Media con Storage Access Framework (SAF)
    fun addMediaFilesFromUris(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            var savedCount = 0
            for (uri in uris) {
                val savedName = MediaStorageManager.saveUriToMedia(context, uri)
                if (savedName != null) {
                    savedCount++
                }
            }
            refreshMediaResources()
            showSnackbar("Se copiaron $savedCount archivo(s) a la carpeta de recursos.")
        }
    }

    fun addMediaFiles(filenames: List<String>) {
        if (filenames.isEmpty()) return
        refreshMediaResources()
        showSnackbar("Se registraron ${filenames.size} archivo(s) de recursos.")
    }

    // Ajustes
    fun setAutoStartOnBoot(enabled: Boolean) {
        _autoStartOnBoot.value = enabled
        prefs.edit().putBoolean("auto_start_boot", enabled).apply()
        showSnackbar(if (enabled) "El bot se iniciará al encender el teléfono." else "Inicio automático desactivado.")
    }

    fun saveDriveId(id: String) {
        _driveId.value = id
        prefs.edit().putString("drive_id", id).apply()
        showSnackbar("ID de Google Drive guardado.")
    }

    fun setEngineMode(mode: String) {
        _engineMode.value = mode
        prefs.edit().putString("engine_mode", mode).apply()
        if (mode == "remote") {
            wsClient.connect(_remoteWsUrl.value)
        } else {
            wsClient.disconnect()
        }
        showSnackbar(if (mode == "remote") "Modo PC remota seleccionado." else "Modo Local (Móvil) activo.")
    }

    fun setRemoteWsUrl(url: String) {
        _remoteWsUrl.value = url
        prefs.edit().putString("remote_ws_url", url).apply()
        if (_engineMode.value == "remote") {
            wsClient.connect(url)
        }
    }

    private fun handleRemoteMessage(jsonText: String) {
        // En una implementación completa procesa los mensajes del servidor remoto
        _consoleOutput.update { "$it\n[Remoto] $jsonText" }
    }

    fun setActiveActivityTab(tab: String) {
        _activeActivityTab.value = tab
    }

    // Avisos descartables
    fun dismissMediaWarning() {
        _alerts.update { it.copy(showMediaWarning = false) }
    }

    fun dismissIndexSyntaxWarning() {
        _alerts.update { it.copy(showIndexSyntaxWarning = false) }
    }

    fun dismissFatalError() {
        _alerts.update { it.copy(showFatalError = false) }
    }

    fun showSnackbar(message: String) {
        _snackbarMessage.value = message
    }

    fun clearSnackbarMessage() {
        _snackbarMessage.value = null
    }

    // Simulación directa para debug
    fun forceState(newStatus: BotStatus) {
        _status.value = newStatus
        when (newStatus) {
            is BotStatus.Online -> {
                _checklist.update {
                    it.copy(
                        nodeState = CheckPointState.OK,
                        depsState = CheckPointState.OK,
                        indexState = CheckPointState.OK,
                        sessionState = CheckPointState.OK,
                        sessionLabel = "Vinculado"
                    )
                }
            }
            is BotStatus.MissingInstall -> {
                _checklist.update { it.copy(depsState = CheckPointState.ERROR, depsLabel = "Faltan") }
            }
            is BotStatus.MissingIndex -> {
                _checklist.update { it.copy(indexState = CheckPointState.ERROR, indexLabel = "Falta") }
            }
            is BotStatus.SessionClosed -> {
                _checklist.update { it.copy(sessionState = CheckPointState.ERROR, sessionLabel = "Cerrada") }
            }
            else -> {}
        }
        showSnackbar("Estado forzado: ${newStatus.title}")
    }

    fun toggleMediaWarningAlert() {
        _alerts.update { it.copy(showMediaWarning = !it.showMediaWarning) }
    }

    fun toggleIndexSyntaxWarningAlert() {
        _alerts.update { it.copy(showIndexSyntaxWarning = !it.showIndexSyntaxWarning) }
    }

    fun toggleFatalErrorAlert() {
        _alerts.update { it.copy(showFatalError = !it.showFatalError) }
    }

    private fun addEvent(tag: EventTag, message: String) {
        val newEvent = BotEvent(
            id = System.currentTimeMillis().toString(),
            time = currentTime(),
            tag = tag,
            message = message
        )
        _events.update { listOf(newEvent) + it.take(150) }
    }

    override fun onCleared() {
        super.onCleared()
        wsClient.disconnect()
    }
}
