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
import androidx.compose.runtime.Composable
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
import com.vital.health.theme.*

@Composable
fun HomeScreen(
    userProfile: UserProfile,
    liveData: LiveHealthData,
    isSyncing: Boolean = false,
    onSync: () -> Unit = {},
    onNavigateToExercise: () -> Unit = {},
    onTriggerHealthTip: () -> Unit = {}
) {
    val currentSteps = liveData.steps
    val targetSteps = userProfile.goals.dailySteps
    val currentBpm = liveData.heartRateBpm
    val activeCalories = liveData.activeCaloriesKcal
    val workoutDaysDone = liveData.weeklyExerciseDays
    val workoutDaysTarget = userProfile.goals.weeklyWorkoutDays
    val weightKg = liveData.weightKg ?: userProfile.weightKg
    val bmi = userProfile.bmi

    // Generate real motivational insight with real quotes
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
            .background(DeepBlack)
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
                        color = CrispWhite,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (liveData.isRealHealthConnectData) Color(0xFF66BB6A) else MidGray)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (liveData.isRealHealthConnectData) "Health Connect Sincronizado" else "Health Connect Conectado",
                            color = MidGray,
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
                            .background(Graphite)
                            .border(1.dp, GlassBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sincronizar",
                            tint = if (isSyncing) Color(0xFF66BB6A) else CrispWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Motivational quote trigger
                    IconButton(
                        onClick = onTriggerHealthTip,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Graphite)
                            .border(1.dp, GlassBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Citação",
                            tint = CrispWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Widget 1: Batimentos Cardíacos
        item {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Graphite),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GlassBorder, RoundedCornerShape(28.dp))
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
                                tint = CrispWhite,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Batimentos Cardíacos",
                                color = CrispWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = if (currentBpm != null) "Health Connect" else "Sem leitura recente",
                            color = MidGray,
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
                                    color = CrispWhite,
                                    fontSize = 34.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "bpm",
                                    color = MidGray,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }
                            Text(
                                text = if (currentBpm != null) "Normal • 52–100 bpm" else "Sincronize a pulseira Zepp / Amazfit",
                                color = MidGray,
                                fontSize = 12.sp
                            )
                        }

                        // Sparkline minimalist Canvas
                        Canvas(modifier = Modifier.size(width = 130.dp, height = 40.dp)) {
                            val path = Path()
                            val points = listOf(
                                Offset(0f, 26f),
                                Offset(22f, 24f),
                                Offset(44f, 12f),
                                Offset(66f, 30f),
                                Offset(88f, 6f),
                                Offset(110f, 18f),
                                Offset(130f, 14f)
                            )
                            points.forEachIndexed { i, p ->
                                if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
                                drawCircle(
                                    color = CrispWhite,
                                    radius = 2.5f,
                                    center = p
                                )
                            }
                            drawPath(
                                path = path,
                                color = CrispWhite,
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
                // Calorias
                Card(
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = Graphite),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, GlassBorder, RoundedCornerShape(26.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = MidGray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Calorias Ativas",
                                color = MidGray,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "$activeCalories",
                                color = CrispWhite,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "kcal",
                                color = MidGray,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(bottom = 3.dp)
                            )
                        }

                        Text(
                            text = "Meta: ${userProfile.goals.dailyCalories} kcal",
                            color = MidGray,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Mini Bar graph
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(22.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            listOf(0.35f, 0.55f, 0.45f, 0.70f, 0.85f, 0.95f).forEach { h ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(h)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(DarkGray)
                                )
                            }
                        }
                    }
                }

                // Passos
                Card(
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = Graphite),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, GlassBorder, RoundedCornerShape(26.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DirectionsWalk,
                                contentDescription = null,
                                tint = MidGray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Passos Hoje",
                                color = MidGray,
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
                                    color = CrispWhite,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Meta: $targetSteps",
                                    color = MidGray,
                                    fontSize = 11.sp
                                )
                            }

                            // Circular Progress Ring
                            Box(
                                modifier = Modifier.size(42.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                val progress = (currentSteps.toFloat() / targetSteps.toFloat()).coerceIn(0f, 1f)
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    drawCircle(
                                        color = DarkGray,
                                        style = Stroke(width = 4.dp.toPx())
                                    )
                                    drawArc(
                                        color = CrispWhite,
                                        startAngle = -90f,
                                        sweepAngle = 360f * progress,
                                        useCenter = false,
                                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.DirectionsRun,
                                    contentDescription = null,
                                    tint = CrispWhite,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Widget 4: Exercícios da semana
        item {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Graphite),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GlassBorder, RoundedCornerShape(26.dp))
                    .clickable { onNavigateToExercise() }
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
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = null,
                                tint = MidGray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Exercícios da semana",
                                color = MidGray,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "$workoutDaysDone",
                                color = CrispWhite,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = " / $workoutDaysTarget dias",
                                color = MidGray,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }

                    // Mini Workout bars
                    Row(
                        modifier = Modifier.height(38.dp),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        listOf(0.4f, 0.7f, 0.9f, 0.3f, 0.6f).forEachIndexed { idx, barH ->
                            val active = idx < workoutDaysDone
                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .fillMaxHeight(barH)
                                    .clip(CircleShape)
                                    .background(if (active) CrispWhite else DarkGray)
                            )
                        }
                    }
                }
            }
        }

        // Widget 5: Composição Corporal
        item {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Graphite),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GlassBorder, RoundedCornerShape(26.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Seu corpo em resumo",
                        color = CrispWhite,
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
                                Text(text = "Peso: ", color = MidGray, fontSize = 13.sp)
                                Text(text = "$weightKg kg", color = CrispWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Row {
                                Text(text = "IMC: ", color = MidGray, fontSize = 13.sp)
                                Text(text = "$bmi", color = CrispWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Row {
                                Text(text = "Gordura: ", color = MidGray, fontSize = 13.sp)
                                Text(
                                    text = if (liveData.bodyFatPct != null) "${liveData.bodyFatPct}%" else "Bioimpedância",
                                    color = CrispWhite,
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
                                drawCircle(color = CrispWhite, radius = 3f, center = p)
                            }
                            drawPath(
                                path = path,
                                color = CrispWhite,
                                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    }
                }
            }
        }

        // Widget 6: Citação Real Inspiradora & Insight do Dia
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Graphite),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(DarkGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatQuote,
                                contentDescription = null,
                                tint = CrispWhite,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = primaryInsight?.title ?: "Inspiração do Dia",
                                color = CrispWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (primaryInsight?.author != null) {
                                Text(
                                    text = primaryInsight.author,
                                    color = MidGray,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    if (primaryInsight?.quote != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "\"${primaryInsight.quote}\"",
                            color = CrispWhite,
                            fontSize = 13.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            lineHeight = 19.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = primaryInsight?.message ?: "Mantenha o foco e a consistência no seu ritmo de atividade hoje.",
                        color = MidGray,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}
