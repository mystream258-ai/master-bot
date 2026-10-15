package com.example.model

/**
 * Categorías de color y semántica del encabezado de estado
 */
enum class HeaderColorCategory {
    GREEN,
    AMBER,
    RED,
    PURPLE,
    GRAY
}

/**
 * Modelo exhaustivo de estados del Panel del Bot
 */
sealed class BotStatus(
    val category: HeaderColorCategory,
    val title: String,
    val subtitleTemplate: String
) {
    // 🟢 VERDE
    data class Online(
        val connectedSince: String = "16:15",
        val clientMovementsToday: Int = 14
    ) : BotStatus(
        category = HeaderColorCategory.GREEN,
        title = "Atendiendo clientes",
        subtitleTemplate = "Conectado a WhatsApp desde las $connectedSince. Movimientos de clientes hoy: $clientMovementsToday."
    )

    // 🟠 ÁMBAR
    object Starting : BotStatus(
        category = HeaderColorCategory.AMBER,
        title = "Iniciando…",
        subtitleTemplate = "El bot se está conectando a WhatsApp."
    )

    object Reconnecting : BotStatus(
        category = HeaderColorCategory.AMBER,
        title = "Reconectando…",
        subtitleTemplate = "Se perdió la conexión con WhatsApp; el bot lo intenta solo."
    )

    object Restarting : BotStatus(
        category = HeaderColorCategory.AMBER,
        title = "Reiniciando…",
        subtitleTemplate = "El bot se reinicia solo en unos segundos."
    )

    object Installing : BotStatus(
        category = HeaderColorCategory.AMBER,
        title = "Instalando…",
        subtitleTemplate = "Puede tardar unos minutos. Mientras tanto puedes seguir usando el teléfono."
    )

    object Updating : BotStatus(
        category = HeaderColorCategory.AMBER,
        title = "Actualizando index.js…",
        subtitleTemplate = "Se valida el archivo nuevo antes de reemplazar el anterior."
    )

    // 🔴 ROJO
    object SessionClosed : BotStatus(
        category = HeaderColorCategory.RED,
        title = "La sesión de WhatsApp se cerró",
        subtitleTemplate = "Pulsa «Borrar sesión y vincular» para conectar el número otra vez."
    )

    object MissingIndex : BotStatus(
        category = HeaderColorCategory.RED,
        title = "Falta index.js",
        subtitleTemplate = "Copia el archivo index.js del bot en la carpeta o pulsa «Elegir index.js nuevo…»."
    )

    object MissingInstall : BotStatus(
        category = HeaderColorCategory.RED,
        title = "Falta instalar",
        subtitleTemplate = "Pulsa «Instalar» para preparar todo. Solo se hace una vez."
    )

    // 🟣 MORADO
    object PairingNeedNumber : BotStatus(
        category = HeaderColorCategory.PURPLE,
        title = "Falta vincular WhatsApp",
        subtitleTemplate = "Escribe tu número abajo para recibir el código de vinculación."
    )

    data class PairingCodeReady(
        val code: String = "ABCD-1234"
    ) : BotStatus(
        category = HeaderColorCategory.PURPLE,
        title = "Escribe el código en tu teléfono",
        subtitleTemplate = "El código caduca en pocos minutos."
    )

    // ⚪ GRIS
    object Stopped : BotStatus(
        category = HeaderColorCategory.GRAY,
        title = "Bot detenido",
        subtitleTemplate = "Pulsa Iniciar para ponerlo a trabajar."
    )

    // Propiedades derivadas de control de botones
    val canStart: Boolean
        get() = when (this) {
            is Stopped -> true
            is SessionClosed -> true
            else -> false // En MissingIndex y MissingInstall está estrictamente DESHABILITADO
        }

    val canStop: Boolean
        get() = when (this) {
            is Online, is Starting, is Reconnecting, is Restarting,
            is PairingNeedNumber, is PairingCodeReady -> true
            else -> false
        }

    val canRestart: Boolean
        get() = canStop

    val isResetHighlighted: Boolean
        get() = this is SessionClosed

    val isStartHighlighted: Boolean
        get() = this is Stopped
}

/**
 * Estado de cada punto del checklist de preparación
 */
enum class CheckPointState {
    OK,
    WAITING,
    ERROR
}

data class PreparationChecklist(
    val nodeState: CheckPointState = CheckPointState.OK,
    val nodeLabel: String = "v22",
    val depsState: CheckPointState = CheckPointState.OK,
    val depsLabel: String = "OK",
    val indexState: CheckPointState = CheckPointState.OK,
    val indexLabel: String = "Listo",
    val sessionState: CheckPointState = CheckPointState.WAITING,
    val sessionLabel: String = "Pendiente"
)

/**
 * Etiquetas para la clasificación y coloreado de eventos
 */
enum class EventTag(val displayName: String) {
    SYS("Sistema"),
    OK("Correcto"),
    ONLINE("En línea"),
    WARN("Aviso"),
    ERR("Error"),
    CLI("Cliente"),
    PAIR("Vincular"),
    CODE("Código"),
    INFO("Detalle")
}

data class BotEvent(
    val id: String,
    val time: String,
    val tag: EventTag,
    val message: String
)

/**
 * Avisos inteligentes que aparecen condicionalmente como tarjetas descartables
 */
data class SmartAlertsState(
    val showMediaWarning: Boolean = false,
    val showIndexSyntaxWarning: Boolean = false,
    val showFatalError: Boolean = false,
    val fatalErrorMessage: String = "El bot se cerró inesperadamente (código de salida 1). Revisa la sintaxis en index.js o reinstala las librerías."
)

/**
 * Recursos multimedia esperados en la carpeta media
 */
data class MediaResourceItem(
    val filename: String,
    val isPresent: Boolean,
    val description: String
)

/**
 * Comandos seguros de diagnóstico para el teléfono
 */
data class DiagnosticItem(
    val id: String,
    val name: String,
    val command: String,
    val description: String
)

/**
 * Comandos guardados para la consola del bot
 */
data class SavedBotCommand(
    val name: String,
    val text: String,
    val isDestructive: Boolean = false
)
