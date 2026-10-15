package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BotStatus
import com.example.model.CheckPointState
import com.example.model.PreparationChecklist
import com.example.ui.theme.BotStatusColors
import com.example.viewmodel.BotPanelViewModel

@Composable
fun EstadoScreen(
    viewModel: BotPanelViewModel,
    listState: LazyListState,
    onNavigateToAjustes: () -> Unit,
    modifier: Modifier = Modifier
) {
    val status by viewModel.status.collectAsState()
    val checklist by viewModel.checklist.collectAsState()
    val alerts by viewModel.alerts.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val isDark = isSystemInDarkTheme()

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // AVISO DE ERROR FATAL (PANEL ROJO CON INSTRUCCIONES)
        if (alerts.showFatalError) {
            item {
                FatalErrorCard(
                    message = alerts.fatalErrorMessage,
                    onDismiss = { viewModel.dismissFatalError() },
                    onGoToMaintenance = onNavigateToAjustes
                )
            }
        }

        // AVISO INTELIGENTE: index.js tiene cosas que pueden fallar (ÁMBAR - SOLO INFORMATIVO)
        if (alerts.showIndexSyntaxWarning) {
            item {
                IndexSyntaxWarningCard(
                    onDismiss = { viewModel.dismissIndexSyntaxWarning() }
                )
            }
        }

        // AVISO INTELIGENTE: Recursos faltantes en media
        if (alerts.showMediaWarning) {
            item {
                MediaWarningCard(
                    onDismiss = { viewModel.dismissMediaWarning() },
                    onGoToMedia = onNavigateToAjustes
                )
            }
        }

        // VINCULACIÓN PASO 1 (MORADO): Formulario para el número
        if (status is BotStatus.PairingNeedNumber) {
            item {
                PairingStepOneCard(
                    onSubmitNumber = { number -> viewModel.submitPhoneNumber(number) }
                )
            }
        }

        // VINCULACIÓN PASO 2 (MORADO): Código de 8 caracteres
        if (status is BotStatus.PairingCodeReady) {
            val code = (status as BotStatus.PairingCodeReady).code
            item {
                PairingStepTwoCard(
                    code = code,
                    onCopyCode = {
                        clipboardManager.setText(AnnotatedString(code))
                        viewModel.showSnackbar("Código copiado: $code")
                    }
                )
            }
        }

        // CHECKLIST DE PREPARACIÓN CON PUNTOS DE COLOR
        item {
            PreparationChecklistCard(checklist = checklist)
        }

        // TARJETA DE CONTROL: Iniciar, Detener, Reiniciar
        item {
            ControlCard(
                status = status,
                onStart = { viewModel.onStartBot() },
                onStop = { viewModel.requestStopBot() },
                onRestart = { viewModel.requestRestartBot() },
                onNavigateToAjustes = onNavigateToAjustes
            )
        }

        // TARJETA DE CUENTA DE WHATSAPP: Borrar sesión y vincular
        item {
            AccountCard(
                status = status,
                onResetSession = { viewModel.requestResetSession() }
            )
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun FatalErrorCard(
    message: String,
    onDismiss: () -> Unit,
    onGoToMaintenance: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) BotStatusColors.RedContainerDark else BotStatusColors.RedContainer
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "⛔ Error fatal al arrancar",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (isDark) BotStatusColors.RedTextDark else BotStatusColors.RedText
            )
            Text(
                text = message,
                fontSize = 13.sp,
                lineHeight = 17.sp,
                color = if (isDark) BotStatusColors.RedTextDark else BotStatusColors.RedText,
                modifier = Modifier.padding(vertical = 6.dp)
            )
            Text(
                text = "Instrucciones: Revisa la consola o actualiza index.js desde Mantenimiento.",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDark) BotStatusColors.RedTextDark.copy(alpha = 0.85f) else BotStatusColors.RedText.copy(alpha = 0.85f)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onGoToMaintenance) {
                    Text("Ir a Mantenimiento", color = BotStatusColors.Red)
                }
                TextButton(onClick = onDismiss) {
                    Text("Entendido", color = BotStatusColors.Red)
                }
            }
        }
    }
}

@Composable
private fun IndexSyntaxWarningCard(
    onDismiss: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) BotStatusColors.AmberContainerDark else BotStatusColors.AmberContainer
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "⚠️ Tu index.js tiene cosas que pueden fallar en este sistema",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (isDark) BotStatusColors.AmberTextDark else BotStatusColors.AmberText
            )
            Text(
                text = "Se encontraron rutas fijas o comandos nativos que podrían requerir ajustes en Android. Revisa tu archivo si experimentas reinicios.",
                fontSize = 13.sp,
                lineHeight = 17.sp,
                color = if (isDark) BotStatusColors.AmberTextDark else BotStatusColors.AmberText,
                modifier = Modifier.padding(vertical = 6.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Entendido", color = BotStatusColors.Amber)
                }
            }
        }
    }
}

@Composable
private fun MediaWarningCard(
    onDismiss: () -> Unit,
    onGoToMedia: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) BotStatusColors.AmberContainerDark else BotStatusColors.AmberContainer
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "⚠️ Faltan recursos (video, APK, imágenes) en la carpeta media",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (isDark) BotStatusColors.AmberTextDark else BotStatusColors.AmberText
            )
            Text(
                text = "Tu index.js referencia archivos multimedia que todavía no están en el almacenamiento del bot.",
                fontSize = 13.sp,
                color = if (isDark) BotStatusColors.AmberTextDark else BotStatusColors.AmberText,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onGoToMedia) {
                    Text("Agregar archivos", color = BotStatusColors.Amber)
                }
                TextButton(onClick = onDismiss) {
                    Text("Ocultar aviso", color = BotStatusColors.Amber)
                }
            }
        }
    }
}

@Composable
private fun PairingStepOneCard(
    onSubmitNumber: (String) -> Unit
) {
    var phoneNumber by remember { mutableStateOf("") }
    val isDark = isSystemInDarkTheme()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) BotStatusColors.PurpleContainerDark else BotStatusColors.PurpleContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Vincula tu WhatsApp (Paso 1 de 2)",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = if (isDark) BotStatusColors.PurpleTextDark else BotStatusColors.PurpleText
            )
            Text(
                text = "Con código de país y sin «+». Ejemplo: 5215512345678",
                fontSize = 13.sp,
                color = if (isDark) BotStatusColors.PurpleTextDark else BotStatusColors.PurpleText,
                modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
            )

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it.filter { char -> char.isDigit() } },
                placeholder = { Text("5215512345678") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { onSubmitNumber(phoneNumber) },
                enabled = phoneNumber.length >= 8,
                colors = ButtonDefaults.buttonColors(containerColor = BotStatusColors.Purple),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Enviar número")
            }
        }
    }
}

@Composable
private fun PairingStepTwoCard(
    code: String,
    onCopyCode: () -> Unit
) {
    val isDark = isSystemInDarkTheme()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) BotStatusColors.PurpleContainerDark else BotStatusColors.PurpleContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Escribe este código en tu teléfono (Paso 2)",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = if (isDark) BotStatusColors.PurpleTextDark else BotStatusColors.PurpleText
            )

            // Caja grande con el código de 8 caracteres
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(2.dp, BotStatusColors.Purple, RoundedCornerShape(12.dp))
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = code,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 6.sp,
                    color = BotStatusColors.Purple
                )
            }

            Text(
                text = "WhatsApp → Dispositivos vinculados → Vincular con número de teléfono → escribe el código.\nEl código caduca en pocos minutos.",
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = if (isDark) BotStatusColors.PurpleTextDark else BotStatusColors.PurpleText,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Button(
                onClick = onCopyCode,
                colors = ButtonDefaults.buttonColors(containerColor = BotStatusColors.Purple),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Copiar código")
            }
        }
    }
}

@Composable
private fun PreparationChecklistCard(
    checklist: PreparationChecklist
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "PREPARACIÓN",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CheckItem(name = "Node.js", state = checklist.nodeState, label = checklist.nodeLabel, modifier = Modifier.weight(1f))
                CheckItem(name = "Librerías", state = checklist.depsState, label = checklist.depsLabel, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CheckItem(name = "index.js", state = checklist.indexState, label = checklist.indexLabel, modifier = Modifier.weight(1f))
                CheckItem(name = "WhatsApp", state = checklist.sessionState, label = checklist.sessionLabel, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun CheckItem(
    name: String,
    state: CheckPointState,
    label: String,
    modifier: Modifier = Modifier
) {
    val dotColor = when (state) {
        CheckPointState.OK -> BotStatusColors.Green
        CheckPointState.WAITING -> BotStatusColors.Amber
        CheckPointState.ERROR -> BotStatusColors.Red
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = name, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ControlCard(
    status: BotStatus,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onRestart: () -> Unit,
    onNavigateToAjustes: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "CONTROL",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Iniciar
                Button(
                    onClick = onStart,
                    enabled = status.canStart,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (status.isStartHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Iniciar", fontSize = 13.sp)
                }

                // Detener
                Button(
                    onClick = onStop,
                    enabled = status.canStop,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Detener", fontSize = 13.sp)
                }

                // Reiniciar
                Button(
                    onClick = onRestart,
                    enabled = status.canRestart,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reiniciar", fontSize = 12.sp)
                }
            }

            // ACCIÓN DESTACADA SEGÚN ESTADO DE ERROR DE ARCHIVO
            if (status is BotStatus.MissingInstall) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onNavigateToAjustes,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("🛠 Instalar / reparar ahora")
                }
            } else if (status is BotStatus.MissingIndex) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onNavigateToAjustes,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("📁 Elegir index.js nuevo…")
                }
            }
        }
    }
}

@Composable
private fun AccountCard(
    status: BotStatus,
    onResetSession: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (status.isResetHighlighted) 2.dp else 1.dp,
            color = if (status.isResetHighlighted) BotStatusColors.Red else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "CUENTA DE WHATSAPP",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onResetSession,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = BotStatusColors.Red),
                border = androidx.compose.foundation.BorderStroke(1.dp, BotStatusColors.Red),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Borrar sesión y vincular", fontWeight = FontWeight.SemiBold)
            }

            Text(
                text = "Úsalo si cambiaste de número o si la sesión se cerró desde el teléfono.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}
