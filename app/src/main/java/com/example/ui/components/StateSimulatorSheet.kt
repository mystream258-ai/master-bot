package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.model.BotStatus
import com.example.ui.theme.BotStatusColors
import com.example.viewmodel.BotPanelViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StateSimulatorFloatingControl(
    viewModel: BotPanelViewModel,
    onNavigateToConsola: () -> Unit,
    modifier: Modifier = Modifier
) {
    // REGLA: Existe SOLO en builds DEBUG
    if (!BuildConfig.DEBUG) return

    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    BoxSimulatorButton(
        onClick = { showSheet = true },
        modifier = modifier
    )

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState
        ) {
            SimulatorSheetContent(
                viewModel = viewModel,
                onNavigateToConsola = onNavigateToConsola,
                onDismiss = { showSheet = false }
            )
        }
    }
}

@Composable
private fun BoxSimulatorButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(imageVector = Icons.Default.Tune, contentDescription = "Simular estado")
            Text(text = "Simular estado", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SimulatorSheetContent(
    viewModel: BotPanelViewModel,
    onNavigateToConsola: () -> Unit,
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Selector de Estados & Pruebas (Debug)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Fuerza cualquier estado del bot y ejecuta pruebas de diagnóstico.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // PRUEBA DEL MOTOR NODEJS-MOBILE
        Text(
            text = "MOTOR NODEJS-MOBILE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Button(
            onClick = {
                viewModel.runNodeJsMobileTest(context)
                onNavigateToConsola()
                onDismiss()
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
            Text(text = " Probar motor (nodejs-project/main.js → Consola)", fontSize = 12.sp)
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        Text(text = "ESTADOS PRINCIPALES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

        // Verde
        Button(
            onClick = {
                viewModel.forceState(BotStatus.Online("16:15", 14))
                onDismiss()
            },
            colors = ButtonDefaults.buttonColors(containerColor = BotStatusColors.Green),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🟢 Verde: Atendiendo clientes")
        }

        // Ámbar
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    viewModel.forceState(BotStatus.Starting)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = BotStatusColors.Amber),
                modifier = Modifier.weight(1f)
            ) {
                Text("🟠 Iniciando…", fontSize = 12.sp)
            }
            Button(
                onClick = {
                    viewModel.forceState(BotStatus.Reconnecting)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = BotStatusColors.Amber),
                modifier = Modifier.weight(1f)
            ) {
                Text("🟠 Reconectando…", fontSize = 12.sp)
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    viewModel.forceState(BotStatus.Restarting)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = BotStatusColors.Amber),
                modifier = Modifier.weight(1f)
            ) {
                Text("🟠 Reiniciando…", fontSize = 12.sp)
            }
            Button(
                onClick = {
                    viewModel.forceState(BotStatus.Installing)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = BotStatusColors.Amber),
                modifier = Modifier.weight(1f)
            ) {
                Text("🟠 Instalando…", fontSize = 12.sp)
            }
        }

        // Rojo
        Text(text = "ESTADOS ROJOS (BOTÓN INICIAR DESHABILITADO EN ERRORES DE ARCHIVO)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BotStatusColors.Red)

        Button(
            onClick = {
                viewModel.forceState(BotStatus.MissingInstall)
                onDismiss()
            },
            colors = ButtonDefaults.buttonColors(containerColor = BotStatusColors.Red),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🔴 Falta instalar (Iniciar OFF / Resalta Instalar)")
        }

        Button(
            onClick = {
                viewModel.forceState(BotStatus.MissingIndex)
                onDismiss()
            },
            colors = ButtonDefaults.buttonColors(containerColor = BotStatusColors.Red),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🔴 Falta index.js (Iniciar OFF / Resalta Elegir)")
        }

        Button(
            onClick = {
                viewModel.forceState(BotStatus.SessionClosed)
                onDismiss()
            },
            colors = ButtonDefaults.buttonColors(containerColor = BotStatusColors.Red),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🔴 Sesión cerrada (Iniciar ON / Resalta Borrar sesión)")
        }

        // Morado
        Text(text = "VINCULACIÓN WHATSAPP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BotStatusColors.Purple)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    viewModel.forceState(BotStatus.PairingNeedNumber)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = BotStatusColors.Purple),
                modifier = Modifier.weight(1f)
            ) {
                Text("🟣 Paso 1: Número", fontSize = 12.sp)
            }
            Button(
                onClick = {
                    viewModel.forceState(BotStatus.PairingCodeReady("ABCD-1234"))
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = BotStatusColors.Purple),
                modifier = Modifier.weight(1f)
            ) {
                Text("🟣 Paso 2: Código", fontSize = 12.sp)
            }
        }

        // Gris
        Button(
            onClick = {
                viewModel.forceState(BotStatus.Stopped)
                onDismiss()
            },
            colors = ButtonDefaults.buttonColors(containerColor = BotStatusColors.Gray),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("⚪ Gris: Bot detenido (Iniciar ON)")
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        Text(text = "TARJETAS DE AVISO INTELIGENTE", fontSize = 11.sp, fontWeight = FontWeight.Bold)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { viewModel.toggleIndexSyntaxWarningAlert() },
                modifier = Modifier.weight(1f)
            ) {
                Text("⚠️ index.js (Ámbar)", fontSize = 11.sp)
            }
            OutlinedButton(
                onClick = { viewModel.toggleMediaWarningAlert() },
                modifier = Modifier.weight(1f)
            ) {
                Text("📁 Media faltante", fontSize = 11.sp)
            }
        }

        OutlinedButton(
            onClick = { viewModel.toggleFatalErrorAlert() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("⛔ Error Fatal al arrancar (Rojo)", fontSize = 11.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
