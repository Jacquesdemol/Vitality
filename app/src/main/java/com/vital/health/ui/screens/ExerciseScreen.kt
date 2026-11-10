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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vital.health.data.UserProfile
import com.vital.health.healthconnect.HealthConnectLogger
import com.vital.health.healthconnect.LiveHealthData
import com.vital.health.model.DayActivitySummary
import com.vital.health.model.ExerciseSessionData
import com.vital.health.model.StreakData
import com.vital.health.theme.VitalTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ExerciseScreen(
    liveData: LiveHealthData,
    userProfile: UserProfile,
    weeklyActivity: List<DayActivitySummary> = emptyList(),
    streakData: StreakData? = null,
    currentWeekOffset: Long = 0L,
    onPreviousWeek: () -> Unit = {},
    onNextWeek: () -> Unit = {}
) {
    val colors = VitalTheme.colors
    val today = LocalDate.now()

    // Seleção vinculada estritamente à DATA real (LocalDate), com recálculo chaveado na semana exibida
    var selectedDate by remember(currentWeekOffset, weeklyActivity.firstOrNull()?.date) {
        val todayInWeek = weeklyActivity.find { it.date == today }?.date
        val initial = todayInWeek ?: weeklyActivity.lastOrNull()?.date ?: today.plusWeeks(currentWeekOffset)
        mutableStateOf(initial)
    }

    // Busca os dados do dia EXATAMENTE pela data selecionada, evitando retenção de dados de dias anteriores
    val selectedDay = weeklyActivity.find { it.date == selectedDate }

    val totalWeekMinutes = weeklyActivity.sumOf { it.totalActiveMinutes }
    val totalWeekCalories = weeklyActivity.sumOf { it.totalCaloriesKcal }
    val totalWeekDistance = weeklyActivity.sumOf { it.totalDistanceKm }

    val activeHours = totalWeekMinutes / 60
    val activeMins = totalWeekMinutes % 60

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
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = colors.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Exercícios",
                        color = colors.textPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Movimente-se. Seu corpo agradece!",
                        color = colors.textSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Streak & Consistency Banner
        if (streakData != null) {
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, colors.cardBorder, RoundedCornerShape(22.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = colors.streakFlame,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${streakData.currentStreakDays} dias de ofensiva",
                                color = colors.textPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "${streakData.activeDaysThisWeek}/${streakData.weeklyGoalDays} dias cumpridos",
                            color = colors.textSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Card 1: Interactive 7-Day Activity Chart with Week Navigation
        item {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(28.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Week navigation row
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
                            if (weeklyActivity.isNotEmpty()) {
                                val startD = weeklyActivity.first().date.format(DateTimeFormatter.ofPattern("dd/MM"))
                                val endD = weeklyActivity.last().date.format(DateTimeFormatter.ofPattern("dd/MM"))
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

                    // Weekly Totals
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "Tempo total ativo",
                                color = colors.textSecondary,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${activeHours}h ${activeMins}min",
                                color = colors.textPrimary,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${totalWeekCalories.toInt()} kcal",
                                color = colors.textPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${String.format(Locale.getDefault(), "%.1f", totalWeekDistance)} km total",
                                color = colors.textSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 7 Interactive Day Bars (Seg a Dom)
                    if (weeklyActivity.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            weeklyActivity.forEach { day ->
                                val isSelected = day.date == selectedDate
                                val barHeightRatio = day.intensityScore.coerceIn(0.12f, 1f)

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            selectedDate = day.date
                                            HealthConnectLogger.logDaySelection(
                                                "ExerciseScreen",
                                                day.date.toString(),
                                                "${day.date}T00:00 - ${day.date}T23:59",
                                                day.exerciseSessions.size
                                            )
                                        }
                                        .padding(vertical = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom
                                ) {
                                    if (day.totalActiveMinutes > 0) {
                                        Text(
                                            text = "${day.totalActiveMinutes}m",
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
                                            .height((130 * barHeightRatio).dp.coerceAtMost(80.dp))
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                if (isSelected) colors.primary
                                                else if (day.hasActivity) colors.chartBarInactive
                                                else colors.surfaceVariant
                                            )
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = day.dayOfWeekLabel,
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
                                text = "Nenhuma sessão de treino no Health Connect para esta semana",
                                color = colors.textSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Details Section for the Selected Day (Vinculado estritamente à data selecionada)
        item {
            val formattedDate = selectedDate.format(
                DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("pt", "BR"))
            )
            val hasActivity = selectedDay?.hasActivity == true

            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(22.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = formattedDate.replaceFirstChar { it.uppercase() },
                                color = colors.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (hasActivity)
                                    "${selectedDay?.exerciseSessions?.size ?: 0} sessão(ões) registrada(s)"
                                else
                                    "Nenhum treino registrado neste dia",
                                color = colors.textSecondary,
                                fontSize = 12.sp
                            )
                        }

                        if (hasActivity && selectedDay != null) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${selectedDay.totalActiveMinutes} min",
                                    color = colors.textPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${selectedDay.totalCaloriesKcal.toInt()} kcal",
                                    color = colors.textSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Real Exercise Sessions List for Selected Day
        if (selectedDay != null && selectedDay.exerciseSessions.isNotEmpty()) {
            item {
                Text(
                    text = "Treinos do dia",
                    color = colors.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }

            items(selectedDay.exerciseSessions.size) { sessionIdx ->
                val session = selectedDay.exerciseSessions[sessionIdx]
                ExerciseSessionCard(session = session)
            }
        } else {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, colors.cardBorder, RoundedCornerShape(20.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = colors.textSecondary.copy(alpha = 0.5f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Nenhum treino neste dia",
                            color = colors.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Os treinos registrados no Health Connect aparecerão automaticamente aqui quando houver atividade nesta data.",
                            color = colors.textSecondary,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }

        // Inspirational Real Athlete Quote
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
                                text = "Eliud Kipchoge",
                                color = colors.textPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Bicampeão Olímpico & Recordista de Maratona",
                                color = colors.textSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "\"Apenas os disciplinados são livres na vida. Se você é indisciplinado, você é escravo de seus humores e paixões.\"",
                        color = colors.textPrimary,
                        fontSize = 13.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        lineHeight = 19.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "A consistência diária nos pequenos treinos gera a verdadeira transformação na sua capacidade cardiorrespiratória.",
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ExerciseSessionCard(session: ExerciseSessionData) {
    val colors = VitalTheme.colors

    val icon: ImageVector = when {
        session.typeName.contains("corrida", ignoreCase = true) -> Icons.Default.DirectionsRun
        session.typeName.contains("ciclismo", ignoreCase = true) || session.typeName.contains("bicicleta", ignoreCase = true) -> Icons.Default.DirectionsBike
        session.typeName.contains("caminhada", ignoreCase = true) -> Icons.Default.DirectionsWalk
        session.typeName.contains("natação", ignoreCase = true) -> Icons.Default.Pool
        else -> Icons.Default.FitnessCenter
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colors.cardBorder, RoundedCornerShape(22.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = colors.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = session.title,
                            color = colors.textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${session.durationMinutes} min • ${session.typeName}",
                            color = colors.textSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                if (session.caloriesKcal != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${session.caloriesKcal.toInt()} kcal",
                            color = colors.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (session.distanceKm != null && session.distanceKm > 0.05) {
                            Text(
                                text = "${String.format(Locale.getDefault(), "%.2f", session.distanceKm)} km",
                                color = colors.textSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Detalhes de Frequência Cardíaca e origem
            if (session.avgHeartRateBpm != null || session.sourceApp.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = colors.cardBorder)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (session.avgHeartRateBpm != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = colors.textSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Média: ${session.avgHeartRateBpm} bpm" +
                                        (if (session.maxHeartRateBpm != null) " (Máx: ${session.maxHeartRateBpm})" else ""),
                                color = colors.textSecondary,
                                fontSize = 11.sp
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Text(
                        text = "Origem: ${session.sourceApp}",
                        color = colors.textSecondary,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
