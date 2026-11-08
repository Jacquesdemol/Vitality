package com.vital.health.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vital.health.theme.*

data class TutorialStep(
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector,
    val highlight: String
)

@Composable
fun TutorialScreen(
    onRequestPermissions: () -> Unit,
    onCompleteTutorial: () -> Unit,
    onTestNotification: () -> Unit
) {
    val steps = listOf(
        TutorialStep(
            title = "Bem-vindo ao VITAL",
            subtitle = "Sua saúde centralizada e minimalista",
            description = "Uma experiência elegante inspirada no ecossistema Galaxy, com paleta monocromática sóbria, dark mode nativo e foco total no seu bem-estar sem distrações.",
            icon = Icons.Default.Favorite,
            highlight = "Dark Mode Sóbrio"
        ),
        TutorialStep(
            title = "Android Health Connect",
            subtitle = "Integração segura e local",
            description = "Conecte seus dados oficiais de passos, batimentos cardíacos, sono e calorias diretamente do Android Health Connect e relógios Zepp OS.",
            icon = Icons.Default.Bedtime,
            highlight = "Privacidade Local"
        ),
        TutorialStep(
            title = "Notificações & Incentivos",
            subtitle = "Mantenha o foco e a consistência",
            description = "Receba notificações inteligentes para se manter em movimento, alertas se permanecer muito tempo inativo e celebrações quando bater suas metas de sono e atividade.",
            icon = Icons.Default.NotificationsActive,
            highlight = "Alertas Inteligentes"
        )
    )

    var currentStepIndex by remember { mutableIntStateOf(0) }
    val step = steps[currentStepIndex]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                steps.indices.forEach { index ->
                    val isCurrent = index == currentStepIndex
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(4.dp)
                            .width(if (isCurrent) 28.dp else 12.dp)
                            .clip(CircleShape)
                            .background(if (isCurrent) CrispWhite else DarkGray)
                    )
                }
            }

            // Central Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon Container
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Graphite)
                        .border(1.dp, GlassBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = step.icon,
                        contentDescription = step.title,
                        tint = CrispWhite,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Badge
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(DarkGray)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = step.highlight,
                        color = MidGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = step.title,
                    color = CrispWhite,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = step.subtitle,
                    color = MidGray,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = step.description,
                    color = MidGray,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                // Botão de teste de notificação se estiver no passo 3
                if (currentStepIndex == 2) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onTestNotification,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Graphite,
                            contentColor = CrispWhite
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Disparar Notificação de Teste", fontSize = 13.sp)
                    }
                }
            }

            // Bottom Actions
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (currentStepIndex < steps.size - 1) {
                    Button(
                        onClick = { currentStepIndex++ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CrispWhite,
                            contentColor = DeepBlack
                        ),
                        shape = RoundedCornerShape(26.dp)
                    ) {
                        Text(text = "Próximo", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    TextButton(
                        onClick = {
                            onRequestPermissions()
                            onCompleteTutorial()
                        }
                    ) {
                        Text(text = "Pular Tutorial", color = MidGray, fontSize = 13.sp)
                    }
                } else {
                    Button(
                        onClick = {
                            onRequestPermissions()
                            onCompleteTutorial()
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
                        Text(text = "Autorizar & Começar", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}
