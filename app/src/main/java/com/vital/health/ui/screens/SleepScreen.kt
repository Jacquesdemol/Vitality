package com.vital.health.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.TrendingUp
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
import com.vital.health.theme.*

@Composable
fun SleepScreen() {
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
                        imageVector = Icons.Default.Bedtime,
                        contentDescription = null,
                        tint = CrispWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Sono",
                        color = CrispWhite,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Durma bem. Viva melhor.",
                        color = MidGray,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Card 1: Seu sono na noite passada
        item {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Graphite),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GlassBorder, RoundedCornerShape(28.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Seu sono na noite passada",
                        color = MidGray,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "7",
                                color = CrispWhite,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "h",
                                color = MidGray,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "48",
                                color = CrispWhite,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "min",
                                color = MidGray,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }

                        // Badge "Bom"
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(DarkGray)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = CrispWhite,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Bom",
                                    color = CrispWhite,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Text(
                        text = "Meta: 7–9 h • Dormiu às 23:20, acordou às 07:08",
                        color = MidGray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Hypnogram Bars
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val stages = listOf(
                            0.2f, 0.45f, 0.7f, 0.9f, 0.85f, 0.5f, 0.75f, 0.95f, 0.6f, 0.8f, 0.4f, 0.85f, 0.7f, 0.3f, 0.9f, 0.6f, 0.2f
                        )
                        stages.forEach { heightRatio ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(heightRatio)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(
                                        when {
                                            heightRatio < 0.35f -> Color(0xFF45454C) // Acordado
                                            heightRatio < 0.6f -> Color(0xFF6E6E78)  // Leve
                                            heightRatio < 0.85f -> Color(0xFFA6A6B2) // REM
                                            else -> CrispWhite                       // Profundo
                                        }
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "• Acordado", color = MidGray, fontSize = 11.sp)
                            Text(text = "18 min", color = CrispWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text(text = "• REM", color = MidGray, fontSize = 11.sp)
                            Text(text = "1 h 34 min", color = CrispWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text(text = "• Leve", color = MidGray, fontSize = 11.sp)
                            Text(text = "4 h 02 min", color = CrispWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text(text = "• Profundo", color = MidGray, fontSize = 11.sp)
                            Text(text = "1 h 14 min", color = CrispWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Card 2: Qualidade do Sono
        item {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Graphite),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GlassBorder, RoundedCornerShape(26.dp))
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
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = MidGray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Qualidade do sono",
                                color = MidGray,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "87%",
                                color = CrispWhite,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "▲ +6%",
                                color = Color(0xFF66BB6A),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Sparkline dos últimos 7 dias
                    Canvas(modifier = Modifier.size(width = 120.dp, height = 40.dp)) {
                        val path = Path()
                        val points = listOf(
                            Offset(0f, 30f),
                            Offset(20f, 26f),
                            Offset(40f, 32f),
                            Offset(60f, 20f),
                            Offset(80f, 15f),
                            Offset(100f, 18f),
                            Offset(120f, 8f)
                        )
                        points.forEachIndexed { i, p ->
                            if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
                            drawCircle(color = CrispWhite, radius = 2.5f, center = p)
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

        // Card 3: Dica de Sono
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Graphite),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(DarkGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = CrispWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "Dica de sono",
                            color = CrispWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Manter um horário regular para dormir ajuda na qualidade do seu sono profundo e no ritmo circadiano.",
                            color = MidGray,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}
