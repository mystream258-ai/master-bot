package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.BotStatus
import com.example.ui.components.PanelConfirmationDialog
import com.example.ui.components.StateSimulatorFloatingControl
import com.example.ui.components.StatusHeader
import com.example.ui.screens.ActividadScreen
import com.example.ui.screens.AjustesScreen
import com.example.ui.screens.ConsolaScreen
import com.example.ui.screens.EstadoScreen
import com.example.viewmodel.BotPanelViewModel
import com.example.viewmodel.ConfirmationDialogType

enum class PanelScreen(val label: String) {
    ESTADO("Estado"),
    ACTIVIDAD("Actividad"),
    CONSOLA("Consola"),
    AJUSTES("Ajustes")
}

@Composable
fun PanelBotApp(
    viewModel: BotPanelViewModel = viewModel()
) {
    val status by viewModel.status.collectAsState()
    val activeDialog by viewModel.activeConfirmationDialog.collectAsState()
    val snackbarMsg by viewModel.snackbarMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var currentScreen by remember { mutableStateOf(PanelScreen.ESTADO) }

    // Estados de scroll para aplicar histéresis sin parpadeo
    val estadoListState = rememberLazyListState()
    val actividadListState = rememberLazyListState()

    val isHeaderCompact by remember {
        derivedStateOf {
            when (currentScreen) {
                PanelScreen.CONSOLA, PanelScreen.AJUSTES -> true
                PanelScreen.ESTADO -> estadoListState.firstVisibleItemIndex > 0 || estadoListState.firstVisibleItemScrollOffset > 80
                PanelScreen.ACTIVIDAD -> actividadListState.firstVisibleItemIndex > 0 || actividadListState.firstVisibleItemScrollOffset > 80
            }
        }
    }

    // Manejo de mensajes Snackbar
    LaunchedEffect(snackbarMsg) {
        snackbarMsg?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbarMessage()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.navigationBarsPadding(),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = currentScreen == PanelScreen.ESTADO,
                    onClick = { currentScreen = PanelScreen.ESTADO },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Estado") },
                    label = { Text(PanelScreen.ESTADO.label, fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = currentScreen == PanelScreen.ACTIVIDAD,
                    onClick = { currentScreen = PanelScreen.ACTIVIDAD },
                    icon = { Icon(Icons.Default.Article, contentDescription = "Actividad") },
                    label = { Text(PanelScreen.ACTIVIDAD.label, fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = currentScreen == PanelScreen.CONSOLA,
                    onClick = { currentScreen = PanelScreen.CONSOLA },
                    icon = { Icon(Icons.Default.Terminal, contentDescription = "Consola") },
                    label = { Text(PanelScreen.CONSOLA.label, fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = currentScreen == PanelScreen.AJUSTES,
                    onClick = { currentScreen = PanelScreen.AJUSTES },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Ajustes") },
                    label = { Text(PanelScreen.AJUSTES.label, fontSize = 11.sp) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // ENCABEZADO DE ESTADO PERMANENTE
                StatusHeader(
                    status = status,
                    isCompact = isHeaderCompact
                )

                // CONTENIDO DE LA PANTALLA SELECCIONADA
                Box(modifier = Modifier.weight(1f)) {
                    when (currentScreen) {
                        PanelScreen.ESTADO -> {
                            EstadoScreen(
                                viewModel = viewModel,
                                listState = estadoListState,
                                onNavigateToAjustes = { currentScreen = PanelScreen.AJUSTES }
                            )
                        }
                        PanelScreen.ACTIVIDAD -> {
                            ActividadScreen(
                                viewModel = viewModel,
                                listState = actividadListState
                            )
                        }
                        PanelScreen.CONSOLA -> {
                            ConsolaScreen(viewModel = viewModel)
                        }
                        PanelScreen.AJUSTES -> {
                            AjustesScreen(viewModel = viewModel)
                        }
                    }
                }
            }

            // BOTÓN FLOTANTE "Simular estado" (SOLO EN BUILDS DEBUG, ABAJO A LA DERECHA)
            StateSimulatorFloatingControl(
                viewModel = viewModel,
                onNavigateToConsola = { currentScreen = PanelScreen.CONSOLA },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp)
            )

            // DIÁLOGO DE CONFIRMACIÓN MODAL
            activeDialog?.let { dialogType ->
                PanelConfirmationDialog(
                    dialogType = dialogType,
                    onConfirm = {
                        when (dialogType) {
                            is ConfirmationDialogType.DestructiveCommand -> viewModel.confirmDestructiveCommand(dialogType.command)
                            is ConfirmationDialogType.StopBotWhileServing -> viewModel.confirmStopBot()
                            is ConfirmationDialogType.RestartBotWhileServing -> viewModel.confirmRestartBot()
                            is ConfirmationDialogType.ResetSession -> viewModel.confirmResetSession()
                        }
                    },
                    onDismiss = { viewModel.dismissConfirmationDialog() }
                )
            }
        }
    }
}
