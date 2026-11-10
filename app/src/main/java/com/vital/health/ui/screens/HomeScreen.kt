package com.vital.health.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vital.health.data.UserProfile
import com.vital.health.healthconnect.LiveHealthData
import com.vital.health.insights.DailyInsightEngine
import com.vital.health.model.HealthSyncStatus
import com.vital.health.model.StreakData
import com.vital.health.model.WeeklyDistanceSummary
import com.vital.health.theme.VitalTheme
import java.time.LocalDate

@Composable
fun HomeScreen(
    userProfile: UserProfile,
    liveData: LiveHealthData,
    streakData: StreakData,
    weeklyDistance: WeeklyDistanceSummary?,
    syncStatus: HealthSyncStatus = HealthSyncStatus.CONNECTED,
    isSyncing: Boolean = false,
    onSync: () -> Unit = {},
    onNavigateToExercise: () -> Unit = {},
    onNavigateToSleep: () -> Unit = {},
    onTriggerHealthTip: () -> Unit = {}
) {
    val colors = VitalTheme.colors

    val currentSteps = liveData.steps
    val targetSteps = userProfile.goals.dailySteps
    val currentBpm = liveData.heartRateBpm
    val restingBpm = liveData.restingHeartRateBpm
    val activeCalories = liveData.activeCaloriesKcal
    val workoutDaysDone = liveData.weeklyExerciseDays
    val workoutDaysTarget = userProfile.goals.weeklyWorkoutDays
    val weightKg = liveData.weightKg ?: userProfile.weightKg
    val bmi = userProfile.bmi

    var showDistanceDialog by remember { mutableStateOf(false) }

    val insights = DailyInsightEngine.generate(
        currentSteps = currentSteps,
        stepGoal = targetSteps,
        sleepMinutes = liveData.sleepDurationMinutes,
        weeklyAvgSleepMinutes = (userProfile.goals.sleepHours * 60).toLong(),
        workoutCount = workoutDaysDone,
        restingHrDelta = 0
    )
    val primaryInsight = insights.firstOrNull()

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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Olá, ${userProfile.name}!",
                        color = colors.textPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val statusDotColor = when (syncStatus) {
                            HealthSyncStatus.CONNECTED -> Color(0xFF4CAF50)
                            HealthSyncStatus.SYNCING -> Color(0xFFFFB300)
                            HealthSyncStatus.NO_DATA -> Color(0xFF29B6F6)
                            HealthSyncStatus.PERMISSIONS_PENDING -> Color(0xFFFF9800)
                            HealthSyncStatus.UNAVAILABLE, HealthSyncStatus.UPDATE_REQUIRED, HealthSyncStatus.ERROR -> Color(0xFFE57373)
                        }
                        val statusText = when (syncStatus) {
                            HealthSyncStatus.CONNECTED -> if (liveData.isRealHealthConnectData) "Health Connect Sincronizado" else "Health Connect Conectado"
                            HealthSyncStatus.SYNCING -> "Sincronizando dados..."
                            HealthSyncStatus.NO_DATA -> "Sem registros recentes"
                            HealthSyncStatus.PERMISSIONS_PENDING -> "Permissões pendentes"
                            HealthSyncStatus.UNAVAILABLE -> "Health Connect não disponível"
                            HealthSyncStatus.UPDATE_REQUIRED -> "Atualização necessária"
                            HealthSyncStatus.ERROR -> "Aguardando sincronização"
                        }

                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(statusDotColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = statusText,
                            color = colors.textSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Sync button
                    IconButton(
                        onClick = onSync,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(colors.surface)
                            .border(1.dp, colors.cardBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sincronizar",
                            tint = if (isSyncing) colors.primary else colors.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Motivational quote trigger
                    IconButton(
                        onClick = onTriggerHealthTip,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(colors.surface)
                            .border(1.dp, colors.cardBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Citação",
                            tint = colors.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Duolingo-Style Activity Streak Card ("Ofensiva")
        item {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(28.dp))
                    .clickable { onNavigateToExercise() }
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(colors.streakFlame.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = "Ofensiva",
                                    tint = colors.streakFlame,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Ofensiva de Atividade",
                                    color = colors.textPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Dias consecutivos em movimento",
                                    color = colors.textSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Best streak pill badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.surfaceVariant)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Recorde: ${streakData.bestStreakDays} d",
                                color = colors.textSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${streakData.currentStreakDays}",
                                color = colors.textPrimary,
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (streakData.currentStreakDays == 1) "dia seguido" else "dias seguidos",
                                color = colors.streakFlame,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }

                        Text(
                            text = "${streakData.activeDaysThisWeek} de ${streakData.weeklyGoalDays} dias na semana",
                            color = colors.textSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Progress bar for the weekly goal
                    val progressRatio = (streakData.activeDaysThisWeek.toFloat() / streakData.weeklyGoalDays.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progressRatio)
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .background(colors.streakFlame)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = streakData.streakMessage,
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        // Widget 1: Batimentos Cardíacos (com repouso diferenciado)
        item {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(28.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = colors.textPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Batimentos Cardíacos",
                                color = colors.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = if (currentBpm != null) "Health Connect" else "Sem leitura recente",
                            color = colors.textSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = if (currentBpm != null) "$currentBpm" else "--",
                                    color = colors.textPrimary,
                                    fontSize = 34.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "bpm",
                                    color = colors.textSecondary,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }
                            Text(
                                text = if (restingBpm != null) "Repouso: $restingBpm bpm • Leitura oficial"
                                       else if (currentBpm != null) "Normal • 52–100 bpm"
                                       else "Sincronize a pulseira Zepp / Amazfit",
                                color = colors.textSecondary,
                                fontSize = 12.sp
                            )
                        }

                        // Sparkline minimalist Canvas
                        Canvas(modifier = Modifier.size(width = 120.dp, height = 36.dp)) {
                            val path = Path()
                            val points = listOf(
                                Offset(0f, 24f),
                                Offset(20f, 22f),
                                Offset(40f, 10f),
                                Offset(60f, 28f),
                                Offset(80f, 6f),
                                Offset(100f, 16f),
                                Offset(120f, 12f)
                            )
                            points.forEachIndexed { i, p ->
                                if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
                                drawCircle(
                                    color = colors.textPrimary,
                                    radius = 2.5f,
                                    center = p
                                )
                            }
                            drawPath(
                                path = path,
                                color = colors.textPrimary,
                                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    }
                }
            }
        }

        // Widget 2 & 3: Calorias & Passos
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Calorias Ativas
                Card(
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, colors.cardBorder, RoundedCornerShape(26.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = colors.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Calorias Ativas",
                                color = colors.textSecondary,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = if (activeCalories > 0) "$activeCalories" else "0",
                                color = colors.textPrimary,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "kcal",
                                color = colors.textSecondary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(bottom = 3.dp)
                            )
                        }

                        Text(
                            text = "Meta: ${userProfile.goals.dailyCalories} kcal",
                            color = colors.textSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        val calRatio = (activeCalories.toFloat() / userProfile.goals.dailyCalories.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape)
                                .background(colors.surfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(calRatio)
                                    .fillMaxHeight()
                                    .clip(CircleShape)
                                    .background(colors.primary)
                            )
                        }
                    }
                }

                // Passos Hoje
                Card(
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, colors.cardBorder, RoundedCornerShape(26.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DirectionsWalk,
                                contentDescription = null,
                                tint = colors.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Passos Hoje",
                                color = colors.textSecondary,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "$currentSteps",
                                    color = colors.textPrimary,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Meta: $targetSteps",
                                    color = colors.textSecondary,
                                    fontSize = 11.sp
                                )
                            }

                            // Circular Progress Ring
                            Box(
                                modifier = Modifier.size(40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                val progress = (currentSteps.toFloat() / targetSteps.coerceAtLeast(1L).toFloat()).coerceIn(0f, 1f)
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    drawCircle(
                                        color = colors.surfaceVariant,
                                        style = Stroke(width = 3.5.dp.toPx())
                                    )
                                    drawArc(
                                        color = colors.primary,
                                        startAngle = -90f,
                                        sweepAngle = 360f * progress,
                                        useCenter = false,
                                        style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.DirectionsRun,
                                    contentDescription = null,
                                    tint = colors.textPrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Widget 4: Distância & Painel Interativo de Km
        item {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(26.dp))
                    .clickable { showDistanceDialog = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = null,
                                tint = colors.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Distância percorrida",
                                color = colors.textSecondary,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = if (liveData.distanceKm > 0.0) String.format("%.2f", liveData.distanceKm) else "0.0",
                                color = colors.textPrimary,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "km hoje",
                                color = colors.textSecondary,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }

                        if (weeklyDistance != null) {
                            Text(
                                text = "${String.format("%.1f", weeklyDistance.totalWeeklyKm)} km na semana (Média ${String.format("%.1f", weeklyDistance.dailyAverageKm)} km/dia)",
                                color = colors.textSecondary,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    // Mini 7-day distance bars
                    if (weeklyDistance != null && weeklyDistance.days.isNotEmpty()) {
                        val maxDist = weeklyDistance.days.maxOfOrNull { it.distanceKm }?.coerceAtLeast(1.0) ?: 1.0
                        Row(
                            modifier = Modifier.height(36.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            weeklyDistance.days.forEach { dayDist ->
                                val ratio = (dayDist.distanceKm / maxDist).toFloat().coerceIn(0.12f, 1f)
                                Box(
                                    modifier = Modifier
                                        .width(5.dp)
                                        .fillMaxHeight(ratio)
                                        .clip(CircleShape)
                                        .background(if (dayDist.distanceKm > 0.1) colors.primary else colors.surfaceVariant)
                                )
                            }
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Ver distância",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Widget 5: Sono da Noite Passada
        item {
            val sleepMins = liveData.sleepDurationMinutes
            val sleepHours = sleepMins / 60
            val sleepRemainder = sleepMins % 60

            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(26.dp))
                    .clickable { onNavigateToSleep() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bedtime,
                                contentDescription = null,
                                tint = colors.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sono recente",
                                color = colors.textSecondary,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = if (sleepMins > 0) "${sleepHours}h ${sleepRemainder}min" else "--",
                                color = colors.textPrimary,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = if (sleepMins > 0) "Dormiu ${liveData.sleepStartTime} • Acordou ${liveData.sleepEndTime}"
                                   else "Nenhum sono sincronizado pelo Health Connect",
                            color = colors.textSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Ver Sono",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Widget 6: Composição Corporal & IMC
        item {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(26.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Seu corpo em resumo",
                        color = colors.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row {
                                Text(text = "Peso: ", color = colors.textSecondary, fontSize = 13.sp)
                                Text(text = "$weightKg kg", color = colors.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Row {
                                Text(text = "IMC: ", color = colors.textSecondary, fontSize = 13.sp)
                                Text(text = "$bmi", color = colors.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Row {
                                Text(text = "Gordura: ", color = colors.textSecondary, fontSize = 13.sp)
                                Text(
                                    text = if (liveData.bodyFatPct != null) "${liveData.bodyFatPct}%" else "Bioimpedância",
                                    color = colors.textPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Sparkline de peso
                        Canvas(modifier = Modifier.size(width = 110.dp, height = 40.dp)) {
                            val path = Path()
                            val points = listOf(
                                Offset(0f, 32f),
                                Offset(35f, 25f),
                                Offset(70f, 20f),
                                Offset(110f, 12f)
                            )
                            points.forEachIndexed { i, p ->
                                if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
                                drawCircle(color = colors.textPrimary, radius = 3f, center = p)
                            }
                            drawPath(
                                path = path,
                                color = colors.textPrimary,
                                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    }
                }
            }
        }

        // Widget 7: Citação Real Inspiradora & Insight do Dia
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
                                text = primaryInsight?.title ?: "Inspiração do Dia",
                                color = colors.textPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (primaryInsight?.author != null) {
                                Text(
                                    text = primaryInsight.author,
                                    color = colors.textSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    if (primaryInsight?.quote != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "\"${primaryInsight.quote}\"",
                            color = colors.textPrimary,
                            fontSize = 13.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            lineHeight = 19.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = primaryInsight?.message ?: "Mantenha o foco e a consistência no seu ritmo de atividade hoje.",
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }

    // Modal / Dialog for Detailed 7-day Distance Chart
    if (showDistanceDialog) {
        AlertDialog(
            onDismissRequest = { showDistanceDialog = false },
            containerColor = colors.surface,
            title = {
                Text(
                    text = "Distância percorrida (7 dias)",
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (weeklyDistance != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total na semana", color = colors.textSecondary, fontSize = 11.sp)
                                Text(
                                    "${String.format("%.2f", weeklyDistance.totalWeeklyKm)} km",
                                    color = colors.textPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Média diária", color = colors.textSecondary, fontSize = 11.sp)
                                Text(
                                    "${String.format("%.2f", weeklyDistance.dailyAverageKm)} km/dia",
                                    color = colors.textPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Interactive 7-day bar chart
                        val maxD = weeklyDistance.days.maxOfOrNull { it.distanceKm }?.coerceAtLeast(1.0) ?: 1.0
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            weeklyDistance.days.forEach { dayD ->
                                val ratio = (dayD.distanceKm / maxD).toFloat().coerceIn(0.08f, 1f)
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = if (dayD.distanceKm > 0) String.format("%.1f", dayD.distanceKm) else "0",
                                        color = colors.textSecondary,
                                        fontSize = 10.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(18.dp)
                                            .fillMaxHeight(ratio)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (dayD.distanceKm > 0.1) colors.primary else colors.surfaceVariant)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = dayD.dayOfWeekLabel,
                                        color = colors.textPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Origem dos dados: Android Health Connect (leitura agregada de sensores e pulseiras conectadas).",
                            color = colors.textSecondary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    } else {
                        Text(
                            text = "Nenhum dado de distância registrado pelo Health Connect para o período.",
                            color = colors.textSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDistanceDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary)
                ) {
                    Text("Fechar", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
