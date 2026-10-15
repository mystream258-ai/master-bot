package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BotEvent
import com.example.model.EventTag
import com.example.ui.theme.BotStatusColors
import com.example.viewmodel.BotPanelViewModel

@Composable
fun ActividadScreen(
    viewModel: BotPanelViewModel,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    val events by viewModel.events.collectAsState()
    val activeTab by viewModel.activeActivityTab.collectAsState()

    val clientCount = events.count { it.tag == EventTag.CLI }
    val alertCount = events.count { it.tag == EventTag.WARN || it.tag == EventTag.ERR }

    // Filtrado según la pestaña seleccionada
    // Resumen: eventos destacados (excluye INFO / detalle ruidoso, igual que en el panel HTA original)
    val filteredEvents = when (activeTab) {
        "clientes" -> events.filter { it.tag == EventTag.CLI }
        "alertas" -> events.filter { it.tag == EventTag.WARN || it.tag == EventTag.ERR }
        "detalle" -> events // Muestra todo
        else -> events.filter { it.tag != EventTag.INFO } // "resumen" por defecto
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // PESTAÑAS TIPO CHIPS M3
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = activeTab == "resumen",
                onClick = { viewModel.setActiveActivityTab("resumen") },
                label = { Text("Resumen", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors()
            )
            FilterChip(
                selected = activeTab == "clientes",
                onClick = { viewModel.setActiveActivityTab("clientes") },
                label = { Text("Clientes ($clientCount)", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors()
            )
            FilterChip(
                selected = activeTab == "alertas",
                onClick = { viewModel.setActiveActivityTab("alertas") },
                label = { Text("Alertas ($alertCount)", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors()
            )
            FilterChip(
                selected = activeTab == "detalle",
                onClick = { viewModel.setActiveActivityTab("detalle") },
                label = { Text("Detalle", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors()
            )
        }

        // LISTA DE EVENTOS O ESTADO VACÍO
        if (filteredEvents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "📭", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Todavía no hay actividad.",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Cuando el bot atienda a un cliente o avise de un problema, lo verás aquí.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredEvents, key = { it.id }) { event ->
                    EventCardItem(event = event)
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun EventCardItem(event: BotEvent) {
    val isDark = isSystemInDarkTheme()

    // Configuración visual según etiqueta
    val (stripeColor, badgeBg, badgeTextColor) = when (event.tag) {
        EventTag.ERR -> Triple(BotStatusColors.Red, if (isDark) BotStatusColors.RedContainerDark else BotStatusColors.RedContainer, if (isDark) BotStatusColors.RedTextDark else BotStatusColors.RedText)
        EventTag.WARN -> Triple(BotStatusColors.Amber, if (isDark) BotStatusColors.AmberContainerDark else BotStatusColors.AmberContainer, if (isDark) BotStatusColors.AmberTextDark else BotStatusColors.AmberText)
        EventTag.CLI -> Triple(BotStatusColors.ClientBlue, if (isDark) BotStatusColors.ClientBlueContainerDark else BotStatusColors.ClientBlueContainer, if (isDark) BotStatusColors.ClientBlueDark else BotStatusColors.ClientBlue)
        EventTag.ONLINE, EventTag.OK -> Triple(BotStatusColors.Green, if (isDark) BotStatusColors.GreenContainerDark else BotStatusColors.GreenContainer, if (isDark) BotStatusColors.GreenTextDark else BotStatusColors.GreenText)
        EventTag.PAIR, EventTag.CODE -> Triple(BotStatusColors.Purple, if (isDark) BotStatusColors.PurpleContainerDark else BotStatusColors.PurpleContainer, if (isDark) BotStatusColors.PurpleTextDark else BotStatusColors.PurpleText)
        EventTag.SYS, EventTag.INFO -> Triple(BotStatusColors.Gray, MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Barra de color lateral del evento
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(38.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(stripeColor)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = event.time,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(badgeBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = event.tag.displayName,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeTextColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = event.message,
                    fontSize = 13.sp,
                    lineHeight = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
