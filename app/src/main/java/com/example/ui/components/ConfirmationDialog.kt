package com.example.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.example.ui.theme.BotStatusColors
import com.example.viewmodel.ConfirmationDialogType

@Composable
fun PanelConfirmationDialog(
    dialogType: ConfirmationDialogType,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val title: String
    val message: String
    val confirmButtonText: String
    val isDanger: Boolean

    when (dialogType) {
        is ConfirmationDialogType.DestructiveCommand -> {
            title = "Confirmar comando: ${dialogType.command}"
            message = dialogType.message
            confirmButtonText = "Enviar comando"
            isDanger = true
        }
        is ConfirmationDialogType.StopBotWhileServing -> {
            title = "¿Detener el bot?"
            message = "El bot está atendiendo clientes en este momento. Si lo detienes, dejará de responder mensajes de inmediato."
            confirmButtonText = "Sí, detener"
            isDanger = true
        }
        is ConfirmationDialogType.RestartBotWhileServing -> {
            title = "¿Reiniciar el bot?"
            message = "El bot se reiniciará en unos segundos. Se cerrará temporalmente la conexión con WhatsApp."
            confirmButtonText = "Reiniciar ahora"
            isDanger = false
        }
        is ConfirmationDialogType.ResetSession -> {
            title = "¿Borrar sesión y vincular?"
            message = "Se eliminará la sesión de WhatsApp guardada en este teléfono. Deberás volver a vincular el número mediante código para reanudar el servicio."
            confirmButtonText = "Sí, borrar sesión"
            isDanger = true
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
        },
        text = {
            Text(text = message, style = MaterialTheme.typography.bodyMedium)
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = if (isDanger) {
                    ButtonDefaults.buttonColors(
                        containerColor = BotStatusColors.Red,
                        contentColor = MaterialTheme.colorScheme.surface
                    )
                } else {
                    ButtonDefaults.buttonColors()
                }
            ) {
                Text(text = confirmButtonText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancelar")
            }
        }
    )
}
