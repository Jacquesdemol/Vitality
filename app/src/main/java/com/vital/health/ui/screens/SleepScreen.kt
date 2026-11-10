package com.vital.health.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vital.health.data.UserProfile
import com.vital.health.healthconnect.HealthConnectLogger
import com.vital.health.healthconnect.LiveHealthData
import com.vital.health.model.NightSleepSummary
import com.vital.health.model.SleepStageType
import com.vital.health.theme.VitalTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun SleepScreen(
    liveData: LiveHealthData,
    userProfile: UserProfile,
    weeklySleep: List<NightSleepSummary> = emptyList(),
    currentWeekOffset: Long = 0L,
    onPreviousWeek: () -> Unit = {},
    onNextWeek: () -> Unit = {}
) {
    val colors = VitalTheme.colors
    val today = LocalDate.now()

    // Seleção vinculada estritamente à DATA real (LocalDate), chaveada na semana e dados carregados
    var selectedDate by remember(currentWeekOffset, weeklySleep.firstOrNull()?.date) {
        val todayInWeek = weeklySleep.find { it.date == today }?.date
        val initial = todayInWeek ?: weeklySleep.lastOrNull()?.date ?: today.plusWeeks(currentWeekOffset)
        mutableStateOf(initial)
    }

    // Busca o resumo da noite EXATAMENTE pela data selecionada
    val selectedNight = weeklySleep.find { it.date == selectedDate }

    val totalWeekSleepMinutes = weeklySleep.sumOf { it.totalMinutes }
    val daysWithSleep = weeklySleep.count { it.totalMinutes > 0 }
    val avgSleepMinutes = if (daysWithSleep > 0) totalWeekSleepMinutes / daysWithSleep else 0L

    val avgHours = avgSleepMinutes / 60
    val avgMins = avgSleepMinutes % 60

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(colors.surface)
                        .border(1.dp, colors.cardBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bedtime,
                        contentDescription = null,
                        tint = colors.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Sono & Recuperação",
                        color = colors.textPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Durma bem. Viva melhor.",
                        color = colors.textSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Card 1: Interactive 7-Day Sleep Chart & Week Navigator
        item {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(28.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Week navigation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onPreviousWeek,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(colors.surfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "Semana anterior",
                                tint = colors.textPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val weekLabel = if (currentWeekOffset == 0L) "Esta Semana" else "${Math.abs(currentWeekOffset)} sem atrás"
                            Text(
                                text = weekLabel,
                                color = colors.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (weeklySleep.isNotEmpty()) {
                                val startD = weeklySleep.first().date.format(DateTimeFormatter.ofPattern("dd/MM"))
                                val endD = weeklySleep.last().date.format(DateTimeFormatter.ofPattern("dd/MM"))
                                Text(
                                    text = "$startD - $endD",
                                    color = colors.textSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = onNextWeek,
                            enabled = currentWeekOffset < 0L,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (currentWeekOffset < 0L) colors.surfaceVariant else colors.surfaceVariant.copy(alpha = 0.4f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Próxima semana",
                                tint = if (currentWeekOffset < 0L) colors.textPrimary else colors.textSecondary.copy(alpha = 0.4f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Weekly Average Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "Média de sono na semana",
                                color = colors.textSecondary,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (daysWithSleep > 0) "${avgHours}h ${avgMins}min" else "--",
                                color = colors.textPrimary,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "Meta: ${userProfile.goals.sleepHours.toInt()}h por noite",
                            color = colors.textSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 7-day Bar chart
                    if (weeklySleep.isNotEmpty()) {
                        val maxMins = (weeklySleep.maxOfOrNull { it.totalMinutes } ?: 480L).coerceAtLeast(480L).toFloat()

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            weeklySleep.forEach { night ->
                                val isSelected = night.date == selectedDate
                                val ratio = (night.totalMinutes.toFloat() / maxMins).coerceIn(0.08f, 1f)
                                val dayLabel = night.date.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale("pt", "BR"))
                                    .replaceFirstChar { it.uppercase() }
                                    .take(3)

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            selectedDate = night.date
                                            HealthConnectLogger.logDaySelection(
                                                "SleepScreen",
                                                night.date.toString(),
                                                "${night.date.minusDays(1)} 18:00 - ${night.date} 16:00",
                                                if (night.session != null) 1 else 0
                                            )
                                        }
                                        .padding(vertical = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom
                                ) {
                                    if (night.totalMinutes > 0) {
                                        Text(
                                            text = "${night.totalMinutes / 60}h",
                                            color = if (isSelected) colors.primary else colors.textSecondary,
                                            fontSize = 9.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.height(13.dp))
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Box(
                                        modifier = Modifier
                                            .width(if (isSelected) 18.dp else 12.dp)
                                            .height((130 * ratio).dp.coerceAtMost(80.dp))
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                if (isSelected) colors.primary
                                                else if (night.totalMinutes > 0) colors.chartBarInactive
                                                else colors.surfaceVariant
                                            )
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = dayLabel,
                                        color = if (isSelected) colors.primary else colors.textSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Nenhum dado de sono sincronizado pelo Health Connect nesta semana",
                                color = colors.textSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Details Section for the Selected Night
        val session = selectedNight?.session
        val formattedDate = selectedDate.format(
            DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("pt", "BR"))
        )
        val hasSleepData = selectedNight != null && selectedNight.totalMinutes > 0L

        item {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(26.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = formattedDate.replaceFirstChar { it.uppercase() },
                                color = colors.textPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (session != null)
                                    "Dormiu às ${selectedNight.formattedBedtime} • Acordou às ${selectedNight.formattedWakeTime}"
                                else
                                    "Sem registro de sono para esta noite",
                                color = colors.textSecondary,
                                fontSize = 12.sp
                            )
                        }

                        if (hasSleepData && selectedNight != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.surfaceVariant)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = selectedNight.qualityAssessment,
                                    color = colors.textPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    if (hasSleepData && selectedNight != null) {
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${selectedNight.totalMinutes / 60}",
                                    color = colors.textPrimary,
                                    fontSize = 34.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "h",
                                    color = colors.textSecondary,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(bottom = 5.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${selectedNight.totalMinutes % 60}",
                                    color = colors.textPrimary,
                                    fontSize = 34.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "min",
                                    color = colors.textSecondary,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(bottom = 5.dp)
                                )
                            }

                            if (selectedNight.comparisonWithWeeklyAvgMinutes != 0L) {
                                val isAbove = selectedNight.comparisonWithWeeklyAvgMinutes > 0
                                Text(
                                    text = (if (isAbove) "▲ +" else "▼ ") +
                                            "${Math.abs(selectedNight.comparisonWithWeeklyAvgMinutes)} min vs média",
                                    color = if (isAbove) Color(0xFF66BB6A) else Color(0xFFFFB74D),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Hypnogram / Sleep Stages Visualization
                        if (session != null && session.hasStages && session.stages.isNotEmpty()) {
                            Text(
                                text = "Hipnograma de Estágios Reais",
                                color = colors.textPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Chronological stages bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colors.surfaceVariant),
                                horizontalArrangement = Arrangement.spacedBy(1.dp)
                            ) {
                                val totalStageMinutes = session.stages.sumOf { it.durationMinutes }.coerceAtLeast(1L)
                                session.stages.forEach { interval ->
                                    val weight = (interval.durationMinutes.toFloat() / totalStageMinutes).coerceAtLeast(0.02f)
                                    val stageColor = when (interval.stageType) {
                                        SleepStageType.DEEP -> colors.primary
                                        SleepStageType.REM -> Color(0xFFBA68C8)
                                        SleepStageType.LIGHT -> colors.textSecondary
                                        SleepStageType.AWAKE, SleepStageType.OUT_OF_BED -> Color(0xFFFF8A65)
                                        SleepStageType.UNKNOWN -> colors.surfaceVariant
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(weight)
                                            .fillMaxHeight()
                                            .background(stageColor)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Real stages legend with minutes
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = "• Profundo", color = colors.textPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "${session.deepMinutes} min", color = colors.textSecondary, fontSize = 11.sp)
                                }
                                Column {
                                    Text(text = "• REM", color = Color(0xFFBA68C8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "${session.remMinutes} min", color = colors.textSecondary, fontSize = 11.sp)
                                }
                                Column {
                                    Text(text = "• Leve", color = colors.textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "${session.lightMinutes} min", color = colors.textSecondary, fontSize = 11.sp)
                                }
                                Column {
                                    Text(text = "• Acordado", color = Color(0xFFFF8A65), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "${session.awakeMinutes} min", color = colors.textSecondary, fontSize = 11.sp)
                                }
                            }
                        } else {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = colors.textSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "A pulseira ou app de origem registrou a duração total do sono, mas não forneceu os estágios detalhados (REM, Leve, Profundo) no Health Connect para esta noite.",
                                        color = colors.textSecondary,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Nenhuma sessão de sono encontrada no Health Connect para esta data. Os dados aparecem automaticamente assim que a sincronização for realizada pelo app de origem.",
                            color = colors.textSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }

        // Card 3: Citação Real sobre Descanso e Disciplina
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(colors.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatQuote,
                                contentDescription = null,
                                tint = colors.textPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "Marco Aurélio",
                                color = colors.textPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Imperador Romano & Filósofo Estoico",
                                color = colors.textSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "\"Ao acordar pela manhã, pense no privilégio precioso que é estar vivo: respirar, pensar, desfrutar, amar.\"",
                        color = colors.textPrimary,
                        fontSize = 13.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        lineHeight = 19.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "O sono de qualidade é a base biológica de toda a regeneração celular, foco cognitivo e desempenho físico.",
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}
