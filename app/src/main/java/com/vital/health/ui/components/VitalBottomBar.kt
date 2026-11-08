package com.vital.health.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vital.health.theme.*

enum class VitalTab(val title: String, val icon: ImageVector) {
    SLEEP("Sono", Icons.Default.Bedtime),
    EXERCISE("Exercícios", Icons.Default.FitnessCenter),
    HOME("Início", Icons.Default.Home),
    SETTINGS("Configurações", Icons.Default.Settings)
}

/**
 * Ilha flutuante em formato de pílula inspirada na navegação inferior da Galaxy Store
 */
@Composable
fun VitalBottomBar(
    currentTab: VitalTab,
    onTabSelected: (VitalTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .widthIn(max = 380.dp)
                .fillMaxWidth()
                .clip(CircleShape)
                .background(GlassBackground)
                .border(1.dp, GlassBorder, CircleShape)
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            VitalTab.values().forEach { tab ->
                val isSelected = tab == currentTab
                val tabBg by animateColorAsState(
                    targetValue = if (isSelected) PillHighlight else androidx.compose.ui.graphics.Color.Transparent,
                    animationSpec = tween(durationMillis = 200),
                    label = "tab_highlight"
                )
                val contentColor = if (isSelected) CrispWhite else MidGray

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .background(tabBg)
                        .clickable { onTabSelected(tab) }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title,
                        tint = contentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = tab.title,
                        color = contentColor,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
