package com.vital.health.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vital.health.data.UserGoals
import com.vital.health.data.UserProfile
import com.vital.health.notifications.VitalNotificationHelper
import com.vital.health.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    userProfile: UserProfile,
    onUpdateProfile: (UserProfile) -> Unit,
    onSyncNow: () -> Unit,
    lastSyncTimeStr: String,
    isSyncing: Boolean,
    onReplayTutorial: () -> Unit,
    onRequestPermissions: () -> Unit
) {
    val context = LocalContext.current
    var notificationsEnabled by remember { mutableStateOf(true) }
    var notificationMessage by remember { mutableStateOf<String?>(null) }

    // Dialog states
    var showProfileDialog by remember { mutableStateOf(false) }
    var showGoalsDialog by remember { mutableStateOf(false) }
    var showDevicesDialog by remember { mutableStateOf(false) }

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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Graphite)
                            .border(1.dp, GlassBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = CrispWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Configurações",
                            color = CrispWhite,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Personalize sua experiência",
                            color = MidGray,
                            fontSize = 13.sp
                        )
                    }
                }

                // Botão de Sincronização rápida
                IconButton(
                    onClick = onSyncNow,
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
            }
        }

        // Profile Card (Clicável para editar)
        item {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Graphite),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GlassBorder, RoundedCornerShape(26.dp))
                    .clickable { showProfileDialog = true }
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
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(DarkGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MidGray,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = userProfile.name,
                                color = CrispWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "${userProfile.age} anos • ${(userProfile.heightCm / 100.0).toString().take(4)} m • ${userProfile.weightKg} kg (IMC ${userProfile.bmi})",
                                color = MidGray,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Toque para editar perfil",
                                color = MidGray.copy(alpha = 0.7f),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 2.dp)
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

        // Main Navigation Options (Metas, Dispositivos Conectados, Tutorial, Permissões)
        item {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Graphite),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GlassBorder, RoundedCornerShape(26.dp))
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    // 1. Metas
                    SettingsRowItem(
                        icon = Icons.Default.TrackChanges,
                        title = "Painel de Metas",
                        subtitle = "${userProfile.goals.dailySteps} passos • ${userProfile.goals.dailyCalories} kcal • ${userProfile.goals.sleepHours}h sono",
                        onClick = { showGoalsDialog = true }
                    )

                    Divider(color = GlassBorder, modifier = Modifier.padding(horizontal = 8.dp))

                    // 2. Dispositivos Conectados
                    SettingsRowItem(
                        icon = Icons.Default.Watch,
                        title = "Dispositivos conectados",
                        subtitle = "Health Connect & Zepp OS Amazfit",
                        onClick = { showDevicesDialog = true }
                    )

                    Divider(color = GlassBorder, modifier = Modifier.padding(horizontal = 8.dp))

                    // 3. Rever Tutorial
                    SettingsRowItem(
                        icon = Icons.Default.School,
                        title = "Tutorial e Boas-vindas",
                        subtitle = "Rever introdução e guia de configuração",
                        onClick = onReplayTutorial
                    )

                    Divider(color = GlassBorder, modifier = Modifier.padding(horizontal = 8.dp))

                    // 4. Permissões Health Connect
                    SettingsRowItem(
                        icon = Icons.Default.Security,
                        title = "Permissões de Saúde",
                        subtitle = "Gerenciar autorizações no Health Connect",
                        onClick = onRequestPermissions
                    )
                }
            }
        }

        // Card Notificações & Incentivos com Citações Reais
        item {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Graphite),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GlassBorder, RoundedCornerShape(26.dp))
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
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(DarkGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = CrispWhite,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Notificações & Citações",
                                    color = CrispWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Incentivos de pessoas reais",
                                    color = MidGray,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = {
                                notificationsEnabled = it
                                if (it) {
                                    onRequestPermissions()
                                    VitalNotificationHelper.schedulePeriodicHealthReminders(context)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = DeepBlack,
                                checkedTrackColor = CrispWhite,
                                uncheckedThumbColor = MidGray,
                                uncheckedTrackColor = DarkGray
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Envia mensagens com frases reais de Ayrton Senna, Muhammad Ali, Kobe Bryant, Marco Aurélio e Usain Bolt para te manter focado no seu progresso.",
                        color = MidGray,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Botão de testar notificação agora
                    Button(
                        onClick = {
                            VitalNotificationHelper.sendSampleNotification(context, "motivation")
                            notificationMessage = "Notificação com citação enviada ao sistema!"
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkGray,
                            contentColor = CrispWhite
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Disparar Citação Real Agora", fontSize = 13.sp)
                    }

                    if (notificationMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = notificationMessage ?: "",
                            color = CrispWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // About VITAL & GitHub Repository
        item {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Graphite),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GlassBorder, RoundedCornerShape(26.dp))
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
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(DarkGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = CrispWhite,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Sobre o VITAL",
                                    color = CrispWhite,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Versão 1.0.0 • Visualizador Nativo",
                                    color = MidGray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "VITAL é um visualizador de saúde sóbrio e minimalista integrado diretamente com o Android Health Connect, Zepp OS e Room Local.",
                        color = MidGray,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botão para o Repositório do GitHub do usuário
                    Button(
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://github.com/Jacquesdemol/Vitality")
                            )
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CrispWhite,
                            contentColor = DeepBlack
                        ),
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Repositório no GitHub",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "github.com/Jacquesdemol/Vitality",
                        color = MidGray,
                        fontSize = 11.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }

    // ----------------- DIALOG 1: EDIT PROFILE -----------------
    if (showProfileDialog) {
        var editName by remember { mutableStateOf(userProfile.name) }
        var editAgeStr by remember { mutableStateOf(userProfile.age.toString()) }
        var editHeightStr by remember { mutableStateOf(userProfile.heightCm.toString()) }
        var editWeightStr by remember { mutableStateOf(userProfile.weightKg.toString()) }

        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            containerColor = Graphite,
            title = {
                Text("Editar Perfil", color = CrispWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Nome Completo") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CrispWhite,
                            unfocusedBorderColor = GlassBorder,
                            focusedTextColor = CrispWhite,
                            unfocusedTextColor = CrispWhite
                        )
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = editAgeStr,
                            onValueChange = { editAgeStr = it.filter { c -> c.isDigit() } },
                            label = { Text("Idade (anos)") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CrispWhite,
                                unfocusedBorderColor = GlassBorder,
                                focusedTextColor = CrispWhite,
                                unfocusedTextColor = CrispWhite
                            )
                        )

                        OutlinedTextField(
                            value = editHeightStr,
                            onValueChange = { editHeightStr = it.filter { c -> c.isDigit() || c == '.' } },
                            label = { Text("Altura (cm)") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CrispWhite,
                                unfocusedBorderColor = GlassBorder,
                                focusedTextColor = CrispWhite,
                                unfocusedTextColor = CrispWhite
                            )
                        )
                    }

                    OutlinedTextField(
                        value = editWeightStr,
                        onValueChange = { editWeightStr = it.filter { c -> c.isDigit() || c == '.' || c == ',' } },
                        label = { Text("Peso (kg)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CrispWhite,
                            unfocusedBorderColor = GlassBorder,
                            focusedTextColor = CrispWhite,
                            unfocusedTextColor = CrispWhite
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newAge = editAgeStr.toIntOrNull() ?: userProfile.age
                        val newHeight = editHeightStr.toDoubleOrNull() ?: userProfile.heightCm
                        val newWeight = editWeightStr.replace(',', '.').toDoubleOrNull() ?: userProfile.weightKg

                        val updated = userProfile.copy(
                            name = if (editName.isNotBlank()) editName else userProfile.name,
                            age = newAge,
                            heightCm = newHeight,
                            weightKg = newWeight
                        )
                        onUpdateProfile(updated)
                        showProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrispWhite, contentColor = DeepBlack)
                ) {
                    Text("Salvar Perfil", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showProfileDialog = false }) {
                    Text("Cancelar", color = MidGray)
                }
            }
        )
    }

    // ----------------- DIALOG 2: EDIT GOALS -----------------
    if (showGoalsDialog) {
        var editStepsStr by remember { mutableStateOf(userProfile.goals.dailySteps.toString()) }
        var editCaloriesStr by remember { mutableStateOf(userProfile.goals.dailyCalories.toString()) }
        var editSleepStr by remember { mutableStateOf(userProfile.goals.sleepHours.toString()) }
        var editWorkoutsStr by remember { mutableStateOf(userProfile.goals.weeklyWorkoutDays.toString()) }

        AlertDialog(
            onDismissRequest = { showGoalsDialog = false },
            containerColor = Graphite,
            title = {
                Text("Personalizar Metas", color = CrispWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editStepsStr,
                        onValueChange = { editStepsStr = it.filter { c -> c.isDigit() } },
                        label = { Text("Meta Diária de Passos") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CrispWhite,
                            unfocusedBorderColor = GlassBorder,
                            focusedTextColor = CrispWhite,
                            unfocusedTextColor = CrispWhite
                        )
                    )

                    OutlinedTextField(
                        value = editCaloriesStr,
                        onValueChange = { editCaloriesStr = it.filter { c -> c.isDigit() } },
                        label = { Text("Meta de Calorias Ativas (kcal)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CrispWhite,
                            unfocusedBorderColor = GlassBorder,
                            focusedTextColor = CrispWhite,
                            unfocusedTextColor = CrispWhite
                        )
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = editSleepStr,
                            onValueChange = { editSleepStr = it.filter { c -> c.isDigit() || c == '.' } },
                            label = { Text("Sono (horas)") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CrispWhite,
                                unfocusedBorderColor = GlassBorder,
                                focusedTextColor = CrispWhite,
                                unfocusedTextColor = CrispWhite
                            )
                        )

                        OutlinedTextField(
                            value = editWorkoutsStr,
                            onValueChange = { editWorkoutsStr = it.filter { c -> c.isDigit() } },
                            label = { Text("Treinos / semana") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CrispWhite,
                                unfocusedBorderColor = GlassBorder,
                                focusedTextColor = CrispWhite,
                                unfocusedTextColor = CrispWhite
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newSteps = editStepsStr.toLongOrNull() ?: userProfile.goals.dailySteps
                        val newCalories = editCaloriesStr.toIntOrNull() ?: userProfile.goals.dailyCalories
                        val newSleep = editSleepStr.toFloatOrNull() ?: userProfile.goals.sleepHours
                        val newWorkouts = editWorkoutsStr.toIntOrNull() ?: userProfile.goals.weeklyWorkoutDays

                        val updated = userProfile.copy(
                            goals = UserGoals(
                                dailySteps = newSteps,
                                dailyCalories = newCalories,
                                sleepHours = newSleep,
                                weeklyWorkoutDays = newWorkouts
                            )
                        )
                        onUpdateProfile(updated)
                        showGoalsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrispWhite, contentColor = DeepBlack)
                ) {
                    Text("Salvar Metas", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoalsDialog = false }) {
                    Text("Cancelar", color = MidGray)
                }
            }
        )
    }

    // ----------------- DIALOG 3: CONNECTED DEVICES -----------------
    if (showDevicesDialog) {
        AlertDialog(
            onDismissRequest = { showDevicesDialog = false },
            containerColor = Graphite,
            title = {
                Text("Dispositivos Conectados", color = CrispWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Item Health Connect
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkGray),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Android Health Connect", color = CrispWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("ATIVO", color = Color(0xFF66BB6A), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Centralizador oficial de saúde do Android. Coleta passos, batimentos, sono e treinos.",
                                color = MidGray,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Item Zepp / Amazfit
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkGray),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Amazfit / Zepp OS", color = CrispWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("SINCRONIZADO", color = CrispWhite, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Como sincronizar sua pulseira Amazfit:\n1. Abra o aplicativo Zepp\n2. Vá em Perfil > Adicionar Contas > Health Connect\n3. Ative todas as permissões para o VITAL visualizar tudo automaticamente.",
                                color = MidGray,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Última sincronização:", color = MidGray, fontSize = 11.sp)
                        Text(lastSyncTimeStr, color = CrispWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSyncNow()
                        showDevicesDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrispWhite, contentColor = DeepBlack)
                ) {
                    Text("Sincronizar Agora", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDevicesDialog = false }) {
                    Text("Fechar", color = MidGray)
                }
            }
        )
    }
}
