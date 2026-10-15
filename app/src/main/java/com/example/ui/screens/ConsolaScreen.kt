package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DiagnosticItem
import com.example.model.SavedBotCommand
import com.example.ui.theme.BotStatusColors
import com.example.viewmodel.BotPanelViewModel

@Composable
fun ConsolaScreen(
    viewModel: BotPanelViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf("consola") } // "consola" o "diagnosticos"

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Selector superior entre Consola del bot y Diagnósticos del teléfono
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedTab == "consola",
                onClick = { selectedTab = "consola" },
                label = { Text("Consola del bot", fontSize = 12.sp) }
            )
            FilterChip(
                selected = selectedTab == "diagnosticos",
                onClick = { selectedTab = "diagnosticos" },
                label = { Text("Diagnósticos del teléfono", fontSize = 12.sp) }
            )
        }

        if (selectedTab == "consola") {
            BotConsoleSection(viewModel = viewModel)
        } else {
            PhoneDiagnosticsSection(viewModel = viewModel)
        }
    }
}

@Composable
private fun BotConsoleSection(viewModel: BotPanelViewModel) {
    val consoleOutput by viewModel.consoleOutput.collectAsState()
    val customCommands by viewModel.userCustomCommands.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    var inputCommandText by remember { mutableStateOf("") }
    var saveNameText by remember { mutableStateOf("") }
    var showCommandsDropdown by remember { mutableStateOf(false) }
    var selectedSavedCommand by remember { mutableStateOf<SavedBotCommand?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Caja de salida estilo terminal
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0F172A))
                .padding(12.dp)
        ) {
            Text(
                text = consoleOutput,
                color = Color(0xFFE2E8F0),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 16.sp,
                modifier = Modifier.verticalScroll(rememberScrollState())
            )
        }

        // Selector desplegable de comandos guardados
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "COMANDOS GUARDADOS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { showCommandsDropdown = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = selectedSavedCommand?.let { "${it.name} - ${it.text}" } ?: "-- Elige un comando --",
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                    }

                    DropdownMenu(
                        expanded = showCommandsDropdown,
                        onDismissRequest = { showCommandsDropdown = false }
                    ) {
                        Text(
                            text = "  Comandos de WhatsApp",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        viewModel.defaultBotCommands.forEach { cmd ->
                            DropdownMenuItem(
                                text = { Text("${cmd.name}${if (cmd.isDestructive) " ⚠️" else ""}") },
                                onClick = {
                                    selectedSavedCommand = cmd
                                    inputCommandText = cmd.text
                                    showCommandsDropdown = false
                                }
                            )
                        }

                        if (customCommands.isNotEmpty()) {
                            HorizontalDivider()
                            Text(
                                text = "  Mi lista",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            customCommands.forEach { cmd ->
                                DropdownMenuItem(
                                    text = { Text("${cmd.name} - ${cmd.text}") },
                                    onClick = {
                                        selectedSavedCommand = cmd
                                        inputCommandText = cmd.text
                                        showCommandsDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = inputCommandText,
                    onValueChange = { inputCommandText = it },
                    placeholder = { Text("Escribe comando o texto para el bot…") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.sendCommandToBot(inputCommandText)
                            inputCommandText = ""
                        },
                        enabled = inputCommandText.isNotBlank(),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Enviar al bot", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(inputCommandText))
                            viewModel.showSnackbar("Comando copiado.")
                        },
                        enabled = inputCommandText.isNotBlank(),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Copiar comando", fontSize = 12.sp)
                    }
                }

                OutlinedButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(consoleOutput))
                        viewModel.showSnackbar("Salida de consola copiada.")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copiar toda la salida")
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // SECCIÓN "MI LISTA"
                Text(
                    text = "Mi lista:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = saveNameText,
                        onValueChange = { saveNameText = it },
                        placeholder = { Text("Nombre (ej. SALUDO)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    Button(
                        onClick = {
                            viewModel.saveUserCustomCommand(saveNameText, inputCommandText)
                            saveNameText = ""
                        },
                        enabled = saveNameText.isNotBlank() && inputCommandText.isNotBlank(),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("Guardar texto", fontSize = 11.sp)
                    }
                }

                if (selectedSavedCommand != null && customCommands.contains(selectedSavedCommand)) {
                    OutlinedButton(
                        onClick = {
                            selectedSavedCommand?.let { viewModel.deleteUserCustomCommand(it) }
                            selectedSavedCommand = null
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BotStatusColors.Red),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BotStatusColors.Red),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Quitar el elegido de Mi lista", fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun PhoneDiagnosticsSection(viewModel: BotPanelViewModel) {
    val terminalOutput by viewModel.terminalOutput.collectAsState()
    val isRunning by viewModel.isTerminalRunning.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    var showDiagDropdown by remember { mutableStateOf(false) }
    var selectedDiag by remember { mutableStateOf(viewModel.diagnosticItems.first()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Caja de salida estilo terminal segura
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0A0F1D))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Text(
                text = terminalOutput,
                color = Color(0xFF38BDF8),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 16.sp,
                modifier = Modifier.verticalScroll(rememberScrollState())
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "DIAGNÓSTICOS DEL TELÉFONO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Selector de diagnóstico cerrado y seguro
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { showDiagDropdown = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = "${selectedDiag.name} (${selectedDiag.command})",
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                    }

                    DropdownMenu(
                        expanded = showDiagDropdown,
                        onDismissRequest = { showDiagDropdown = false }
                    ) {
                        viewModel.diagnosticItems.forEach { diag ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(diag.name, fontWeight = FontWeight.SemiBold)
                                        Text(diag.command, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.primary)
                                    }
                                },
                                onClick = {
                                    selectedDiag = diag
                                    showDiagDropdown = false
                                }
                            )
                        }
                    }
                }

                // REGLA OBLIGATORIA: El campo es de SOLO LECTURA (muestra qué se va a ejecutar)
                OutlinedTextField(
                    value = selectedDiag.command,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Comando seguro (solo lectura)") },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                    modifier = Modifier.fillMaxWidth()
                )

                // Botones requeridos: Ejecutar, Detener, Copiar salida, Limpiar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.runSafeDiagnostic(selectedDiag) },
                        enabled = !isRunning,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ejecutar")
                    }

                    OutlinedButton(
                        onClick = { viewModel.stopSafeDiagnostic() },
                        enabled = isRunning,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BotStatusColors.Red),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BotStatusColors.Red),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Detener")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(terminalOutput))
                            viewModel.showSnackbar("Salida de la terminal copiada.")
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Text("Copiar salida", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { viewModel.clearTerminalOutput() },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Text("Limpiar", fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}
