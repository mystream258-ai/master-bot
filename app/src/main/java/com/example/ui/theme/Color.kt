package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Paleta base Material 3 (Teal/Cyan moderno para aplicaciones de servicio/utilidad)
val PrimaryLight = Color(0xFF006874)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFF9EEFFD)
val OnPrimaryContainerLight = Color(0xFF001F24)

val SecondaryLight = Color(0xFF4A6267)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFCCE8ED)
val OnSecondaryContainerLight = Color(0xFF051F23)

val TertiaryLight = Color(0xFF525E7D)
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFDAE2FF)
val OnTertiaryContainerLight = Color(0xFF0E1A37)

val BackgroundLight = Color(0xFFFBFDFD)
val OnBackgroundLight = Color(0xFF191C1D)
val SurfaceLight = Color(0xFFFFFFFF)
val OnSurfaceLight = Color(0xFF191C1D)
val SurfaceVariantLight = Color(0xFFF0F4F6)
val OnSurfaceVariantLight = Color(0xFF40484A)
val OutlineLight = Color(0xFFCBD5E1)
val OutlineVariantLight = Color(0xFFE2E8F0)

val PrimaryDark = Color(0xFF4DD8EC)
val OnPrimaryDark = Color(0xFF00363D)
val PrimaryContainerDark = Color(0xFF004F58)
val OnPrimaryContainerDark = Color(0xFF9EEFFD)

val SecondaryDark = Color(0xFFB1CBD0)
val OnSecondaryDark = Color(0xFF1C3438)
val SecondaryContainerDark = Color(0xFF334B4F)
val OnSecondaryContainerDark = Color(0xFFCCE8ED)

val TertiaryDark = Color(0xFFBAC6EA)
val OnTertiaryDark = Color(0xFF24304D)
val TertiaryContainerDark = Color(0xFF3B4664)
val OnTertiaryContainerDark = Color(0xFFDAE2FF)

val BackgroundDark = Color(0xFF0F1416)
val OnBackgroundDark = Color(0xFFE1E3E4)
val SurfaceDark = Color(0xFF161C1E)
val OnSurfaceDark = Color(0xFFE1E3E4)
val SurfaceVariantDark = Color(0xFF222B2E)
val OnSurfaceVariantDark = Color(0xFFBFC8CA)
val OutlineDark = Color(0xFF475569)
val OutlineVariantDark = Color(0xFF334155)

// Colores semánticos exactos del Panel del Bot (Claro y Oscuro)
object BotStatusColors {
    // Verde: Atendiendo clientes
    val Green = Color(0xFF1F8A5B)
    val GreenDark = Color(0xFF2EBD7B)
    val GreenContainer = Color(0xFFE8F5E9)
    val GreenContainerDark = Color(0xFF0D2C1D)
    val GreenText = Color(0xFF135C3C)
    val GreenTextDark = Color(0xFF69F0AE)

    // Ámbar: Iniciando, Reconectando, Reiniciando, Instalando
    val Amber = Color(0xFFB26A00)
    val AmberDark = Color(0xFFE59218)
    val AmberContainer = Color(0xFFFFF8E1)
    val AmberContainerDark = Color(0xFF331F00)
    val AmberText = Color(0xFF7C4800)
    val AmberTextDark = Color(0xFFFFCC80)

    // Rojo: Falta index.js, Falta instalar, Sesión cerrada
    val Red = Color(0xFFC23B31)
    val RedDark = Color(0xFFEF5350)
    val RedContainer = Color(0xFFFDEBEE)
    val RedContainerDark = Color(0xFF3B1111)
    val RedText = Color(0xFF8C1E17)
    val RedTextDark = Color(0xFFFFCDD2)

    // Morado: Falta vincular WhatsApp, Código de emparejamiento
    val Purple = Color(0xFF6B3FA0)
    val PurpleDark = Color(0xFF9C67DC)
    val PurpleContainer = Color(0xFFF3E8FD)
    val PurpleContainerDark = Color(0xFF29153F)
    val PurpleText = Color(0xFF451E70)
    val PurpleTextDark = Color(0xFFE1BEE7)

    // Gris: Bot detenido
    val Gray = Color(0xFF74838D)
    val GrayDark = Color(0xFF8DA0AC)
    val GrayContainer = Color(0xFFEDF2F5)
    val GrayContainerDark = Color(0xFF242C31)
    val GrayText = Color(0xFF37474F)
    val GrayTextDark = Color(0xFFCFD8DC)

    // Azul específico para eventos de "Cliente"
    val ClientBlue = Color(0xFF0284C7)
    val ClientBlueDark = Color(0xFF38BDF8)
    val ClientBlueContainer = Color(0xFFF0F9FF)
    val ClientBlueContainerDark = Color(0xFF0C2333)
}
