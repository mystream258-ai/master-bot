package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BotStatus
import com.example.model.HeaderColorCategory
import com.example.ui.theme.BotStatusColors

@Composable
fun StatusHeader(
    status: BotStatus,
    isCompact: Boolean,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    // Determinación del color semántico según la categoría
    val accentColor = when (status.category) {
        HeaderColorCategory.GREEN -> if (isDark) BotStatusColors.GreenDark else BotStatusColors.Green
        HeaderColorCategory.AMBER -> if (isDark) BotStatusColors.AmberDark else BotStatusColors.Amber
        HeaderColorCategory.RED -> if (isDark) BotStatusColors.RedDark else BotStatusColors.Red
        HeaderColorCategory.PURPLE -> if (isDark) BotStatusColors.PurpleDark else BotStatusColors.Purple
        HeaderColorCategory.GRAY -> if (isDark) BotStatusColors.GrayDark else BotStatusColors.Gray
    }

    val animatedAccentColor by animateColorAsState(
        targetValue = accentColor,
        animationSpec = tween(durationMillis = 250),
        label = "HeaderAccentColor"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = if (isCompact) 2.dp else 4.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 16.dp,
                        vertical = if (isCompact) 10.dp else 14.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Barra de color lateral prominente
                Box(
                    modifier = Modifier
                        .width(if (isCompact) 6.dp else 8.dp)
                        .height(if (isCompact) 24.dp else 52.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(animatedAccentColor)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 4.dp)
                ) {
                    if (!isCompact) {
                        Text(
                            text = "PANEL DEL BOT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                        )
                    }

                    Text(
                        text = status.title,
                        fontSize = if (isCompact) 16.sp else 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = animatedAccentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    AnimatedVisibility(
                        visible = !isCompact,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Text(
                            text = status.subtitleTemplate,
                            fontSize = 13.sp,
                            lineHeight = 17.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        }
    }
}
