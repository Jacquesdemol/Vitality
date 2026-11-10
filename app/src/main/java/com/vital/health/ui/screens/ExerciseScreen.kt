package com.vital.health.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vital.health.data.UserProfile
import com.vital.health.healthconnect.LiveHealthData
import com.vital.health.theme.*

data class WorkoutItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector
)

@Composable
fun ExerciseScreen(
    liveData: LiveHealthData,
    userProfile: UserProfile
) {
    val workouts = listOf(
        WorkoutItem("Musculação & Hipertrofia", "Hoje • 1 h 20 min • 412 kcal", Icons.Default.FitnessCenter),
        WorkoutItem("Corrida ao ar livre", "Ontem • 45 min • 380 kcal", Icons.Default.DirectionsRun),
        WorkoutItem("Ciclismo", "3 dias atrás • 50 min • 310 kcal", Icons.Default.DirectionsBike)
    )

    val activeHours = liveData.weeklyExerciseMinutes / 60
    val activeMins = liveData.weeklyExerciseMinutes % 60

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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Graphite)
                        .border(1.dp, GlassBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = CrispWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Exercícios",
                        color = CrispWhite,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Movimente-se. Seu corpo agradece!",
                        color = MidGray,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Card 1: Atividade da semana
        item {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Graphite),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GlassBorder, RoundedCornerShape(28.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(DarkGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsRun,
                                contentDescription = null,
                                tint = CrispWhite,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "Atividade da semana",
                                color = MidGray,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (liveData.weeklyExerciseMinutes > 0) "${activeHours} h ${activeMins} min" else "4 h 20 min",
                                    color = CrispWhite,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "▲ +12%",
                                    color = Color(0xFF66BB6A),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Bar Chart
                    Row(
                        modifier = Modifier.height(44.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        listOf(0.3f, 0.5f, 0.8f, 0.45f, 0.95f, 0.7f, 0.6f).forEach { h ->
                            Box(
                                modifier = Modifier
                                    .width(5.dp)
                                    .fillMaxHeight(h)
                                    .clip(CircleShape)
                                    .background(CrispWhite)
                            )
                        }
                    }
                }
            }
        }

        // Card 2 & 3: Calorias ativas & Distância
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Calorias Ativas
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
                                text = "Calorias ativas",
                                color = MidGray,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = if (liveData.activeCaloriesKcal > 0) "${liveData.activeCaloriesKcal}" else "1.820",
                                color = CrispWhite,
                                fontSize = 22.sp,
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

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Meta: ${userProfile.goals.dailyCalories} kcal",
                            color = MidGray,
                            fontSize = 11.sp
                        )
                    }
                }

                // Distância
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
                                imageVector = Icons.Default.Navigation,
                                contentDescription = null,
                                tint = MidGray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Distância",
                                color = MidGray,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = if (liveData.distanceKm > 0.0) String.format("%.1f", liveData.distanceKm) else "14,6",
                                color = CrispWhite,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "km",
                                color = MidGray,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(bottom = 3.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "▲ +10% nesta semana",
                            color = Color(0xFF66BB6A),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Card 4: Citação Real Motivacional sobre Treino
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
                                text = "Usain Bolt",
                                color = CrispWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Recordista Mundial e 8x Campeão Olímpico",
                                color = MidGray,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "\"Eu treinei 4 anos para correr apenas 9 segundos. Não desista se os resultados levarem algum tempo.\"",
                        color = CrispWhite,
                        fontSize = 13.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        lineHeight = 19.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "A consistência nos dias de esforço moderado é o que constrói resistência cardiovascular sólida.",
                        color = MidGray,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        // Section Title: Suas Atividades
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Suas atividades",
                    color = CrispWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Health Connect",
                    color = MidGray,
                    fontSize = 12.sp
                )
            }
        }

        // Activity List
        items(workouts.size) { idx ->
            val w = workouts[idx]
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Graphite),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GlassBorder, RoundedCornerShape(22.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(DarkGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = w.icon,
                                contentDescription = null,
                                tint = CrispWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = w.title,
                                color = CrispWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = w.subtitle,
                                color = MidGray,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MidGray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
