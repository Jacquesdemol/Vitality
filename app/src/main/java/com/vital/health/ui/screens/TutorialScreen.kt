package com.vital.health.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vital.health.data.UserGoals
import com.vital.health.data.UserProfile
import com.vital.health.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorialScreen(
    initialName: String = "Iury Henrique",
    onRequestPermissions: () -> Unit,
    onCompleteTutorial: (UserProfile) -> Unit,
    onTestNotification: () -> Unit
) {
    var currentStep by remember { mutableIntStateOf(0) }

    // User profile inputs for Step 2
    var name by remember { mutableStateOf(initialName) }
    var ageStr by remember { mutableStateOf("24") }
    var heightStr by remember { mutableStateOf("173") }
    var weightStr by remember { mutableStateOf("73.8") }
    var stepGoalStr by remember { mutableStateOf("10000") }

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Progress Indicators
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(0, 1, 2).forEach { stepIndex ->
                        val active = stepIndex == currentStep
                        Box(
                            modifier = Modifier
                                .height(4.dp)
                                .width(if (active) 32.dp else 14.dp)
                                .clip(CircleShape)
                                .background(if (active) CrispWhite else DarkGray)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = when (currentStep) {
                        0 -> "Passo 1 de 3 • Boas-vindas"
                        1 -> "Passo 2 de 3 • Seu Perfil Pessoal"
                        else -> "Passo 3 de 3 • Conexão & Notificações"
                    },
                    color = MidGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Step Content
            when (currentStep) {
                // STEP 0: Welcome & Philosophy
                0 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(Graphite)
                                .border(1.dp, GlassBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = CrispWhite,
                                modifier = Modifier.size(44.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(DarkGray)
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Visualizador Oficial de Saúde",
                                color = MidGray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Bem-vindo ao VITAL",
                            color = CrispWhite,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Um painel visualizador elegante, sóbrio e minimalista que sincroniza dados reais do Android Health Connect e relógios Zepp OS / Amazfit.",
                            color = MidGray,
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Card(
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = Graphite),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, GlassBorder, RoundedCornerShape(22.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = CrispWhite, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("Leitura direta do Android Health Connect", color = CrispWhite, fontSize = 12.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = CrispWhite, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("100% de privacidade e dados locais no seu aparelho", color = CrispWhite, fontSize = 12.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = CrispWhite, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("Citações motivacionais reais de lendas do esporte", color = CrispWhite, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // STEP 1: Profile Creation
                1 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Crie seu Perfil",
                            color = CrispWhite,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Esses dados são usados para calcular seu IMC e personalizar suas metas.",
                            color = MidGray,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Nome
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Seu Nome ou Apelido") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CrispWhite,
                                unfocusedBorderColor = GlassBorder,
                                focusedTextColor = CrispWhite,
                                unfocusedTextColor = CrispWhite,
                                focusedLabelColor = CrispWhite,
                                unfocusedLabelColor = MidGray
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Idade e Altura
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = ageStr,
                                onValueChange = { ageStr = it.filter { c -> c.isDigit() } },
                                label = { Text("Idade (anos)") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CrispWhite,
                                    unfocusedBorderColor = GlassBorder,
                                    focusedTextColor = CrispWhite,
                                    unfocusedTextColor = CrispWhite,
                                    focusedLabelColor = CrispWhite,
                                    unfocusedLabelColor = MidGray
                                ),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = heightStr,
                                onValueChange = { heightStr = it.filter { c -> c.isDigit() || c == '.' } },
                                label = { Text("Altura (cm)") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CrispWhite,
                                    unfocusedBorderColor = GlassBorder,
                                    focusedTextColor = CrispWhite,
                                    unfocusedTextColor = CrispWhite,
                                    focusedLabelColor = CrispWhite,
                                    unfocusedLabelColor = MidGray
                                ),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Peso e Meta de Passos
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = weightStr,
                                onValueChange = { weightStr = it.filter { c -> c.isDigit() || c == '.' || c == ',' } },
                                label = { Text("Peso (kg)") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CrispWhite,
                                    unfocusedBorderColor = GlassBorder,
                                    focusedTextColor = CrispWhite,
                                    unfocusedTextColor = CrispWhite,
                                    focusedLabelColor = CrispWhite,
                                    unfocusedLabelColor = MidGray
                                ),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = stepGoalStr,
                                onValueChange = { stepGoalStr = it.filter { c -> c.isDigit() } },
                                label = { Text("Meta de Passos") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CrispWhite,
                                    unfocusedBorderColor = GlassBorder,
                                    focusedTextColor = CrispWhite,
                                    unfocusedTextColor = CrispWhite,
                                    focusedLabelColor = CrispWhite,
                                    unfocusedLabelColor = MidGray
                                ),
                                singleLine = true
                            )
                        }
                    }
                }

                // STEP 2: Permissions & Notifications with Real Quotes
                2 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Graphite)
                                .border(1.dp, GlassBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = CrispWhite,
                                modifier = Modifier.size(38.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = "Notificações & Health Connect",
                            color = CrispWhite,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Autorize o VITAL a ler os dados de saúde do seu telefone e enviar alertas com frases de lendas como Ayrton Senna, Muhammad Ali e Kobe Bryant.",
                            color = MidGray,
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Test Notification Button
                        Button(
                            onClick = onTestNotification,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Graphite,
                                contentColor = CrispWhite
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Testar Notificação com Citação Real", fontSize = 13.sp)
                        }
                    }
                }
            }

            // Bottom Navigation Actions
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp, top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (currentStep < 2) {
                    Button(
                        onClick = { currentStep++ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CrispWhite,
                            contentColor = DeepBlack
                        ),
                        shape = RoundedCornerShape(26.dp)
                    ) {
                        Text(text = "Avançar", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    if (currentStep == 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = { currentStep = 1 }
                        ) {
                            Text(text = "Pular Introdução", color = MidGray, fontSize = 13.sp)
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            val parsedAge = ageStr.toIntOrNull() ?: 24
                            val parsedHeight = heightStr.toDoubleOrNull() ?: 173.0
                            val parsedWeight = weightStr.replace(',', '.').toDoubleOrNull() ?: 73.8
                            val parsedStepGoal = stepGoalStr.toLongOrNull() ?: 10000L

                            val finalProfile = UserProfile(
                                name = if (name.isNotBlank()) name else "Iury Henrique",
                                age = parsedAge,
                                heightCm = parsedHeight,
                                weightKg = parsedWeight,
                                goals = UserGoals(dailySteps = parsedStepGoal)
                            )

                            onRequestPermissions()
                            onCompleteTutorial(finalProfile)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CrispWhite,
                            contentColor = DeepBlack
                        ),
                        shape = RoundedCornerShape(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Autorizar e Começar", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}
