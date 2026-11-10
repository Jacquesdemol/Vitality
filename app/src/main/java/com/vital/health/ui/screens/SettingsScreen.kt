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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vital.health.data.CalorieGoalMode
import com.vital.health.data.Gender
import com.vital.health.data.UserGoals
import com.vital.health.data.UserProfile
import com.vital.health.notifications.VitalNotificationHelper
import com.vital.health.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    userProfile: UserProfile,
    onUpdateProfile: (UserProfile) -> Unit,
    currentThemeMode: ThemeMode,
    currentThemePalette: ThemePalette,
    onThemeChanged: (ThemeMode, ThemePalette) -> Unit,
    onSyncNow: () -> Unit,
    lastSyncTimeStr: String,
    isSyncing: Boolean,
    onReplayTutorial: () -> Unit,
    onRequestPermissions: () -> Unit
) {
    val context = LocalContext.current
    val colors = VitalTheme.colors

    var notificationsEnabled by remember { mutableStateOf(true) }
    var notificationMessage by remember { mutableStateOf<String?>(null) }

    // Dialog states
    var showProfileDialog by remember { mutableStateOf(false) }
    var showGoalsDialog by remember { mutableStateOf(false) }
    var showDevicesDialog by remember { mutableStateOf(false) }

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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(colors.surface)
                            .border(1.dp, colors.cardBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = colors.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Configurações",
                            color = colors.textPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Personalize sua experiência",
                            color = colors.textSecondary,
                            fontSize = 13.sp
                        )
                    }
                }

                // Quick sync button
                IconButton(
                    onClick = onSyncNow,
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
            }
        }

        // Profile Card (Clickable to edit)
        item {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(26.dp))
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
                                .background(colors.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = colors.textSecondary,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = userProfile.name,
                                color = colors.textPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "${userProfile.age} anos • ${(userProfile.heightCm / 100.0).toString().take(4)} m • ${userProfile.weightKg} kg (IMC ${userProfile.bmi})",
                                color = colors.textSecondary,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Toque para editar perfil",
                                color = colors.textSecondary.copy(alpha = 0.7f),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Section: Aparência & Temas
        item {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(26.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(colors.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = colors.textPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Aparência & Cores",
                                color = colors.textPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Tema claro/escuro e paleta de destaque",
                                color = colors.textSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Mode selector (Sistema, Escuro, Claro)
                    Text(
                        text = "Modo de exibição",
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeMode.values().forEach { mode ->
                            val isSelected = mode == currentThemeMode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) colors.primary else colors.surfaceVariant)
                                    .clickable { onThemeChanged(mode, currentThemePalette) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mode.label,
                                    color = if (isSelected) colors.onPrimary else colors.textPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Palette selector
                    Text(
                        text = "Paleta de cores",
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemePalette.values().forEach { pal ->
                            val isSelected = pal == currentThemePalette
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) colors.surfaceVariant else Color.Transparent)
                                    .border(
                                        width = if (isSelected) 1.dp else 0.dp,
                                        color = if (isSelected) colors.primary.copy(alpha = 0.5f) else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { onThemeChanged(currentThemeMode, pal) }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(pal.previewColor)
                                            .border(1.dp, colors.cardBorder, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = pal.label,
                                        color = colors.textPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selecionado",
                                        tint = colors.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Main Navigation Options (Metas, Dispositivos Conectados, Tutorial, Permissões)
        item {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(26.dp))
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    // 1. Metas
                    SettingsRowItem(
                        icon = Icons.Default.TrackChanges,
                        title = "Painel de Metas",
                        subtitle = "${userProfile.goals.dailySteps} passos • ${userProfile.goals.dailyCalories} kcal • ${userProfile.goals.sleepHours}h sono",
                        onClick = { showGoalsDialog = true }
                    )

                    Divider(color = colors.cardBorder, modifier = Modifier.padding(horizontal = 8.dp))

                    // 2. Dispositivos Conectados
                    SettingsRowItem(
                        icon = Icons.Default.Watch,
                        title = "Dispositivos conectados",
                        subtitle = "Health Connect & Zepp OS Amazfit",
                        onClick = { showDevicesDialog = true }
                    )

                    Divider(color = colors.cardBorder, modifier = Modifier.padding(horizontal = 8.dp))

                    // 3. Rever Tutorial
                    SettingsRowItem(
                        icon = Icons.Default.School,
                        title = "Tutorial e Boas-vindas",
                        subtitle = "Rever introdução e guia de configuração",
                        onClick = onReplayTutorial
                    )

                    Divider(color = colors.cardBorder, modifier = Modifier.padding(horizontal = 8.dp))

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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(colors.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = colors.textPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Notificações & Citações",
                                    color = colors.textPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Incentivos de pessoas reais",
                                    color = colors.textSecondary,
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
                                checkedThumbColor = colors.background,
                                checkedTrackColor = colors.primary,
                                uncheckedThumbColor = colors.textSecondary,
                                uncheckedTrackColor = colors.surfaceVariant
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Envia mensagens com frases reais de Ayrton Senna, Muhammad Ali, Kobe Bryant, Marco Aurélio e Usain Bolt para te manter focado no seu progresso.",
                        color = colors.textSecondary,
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
                            containerColor = colors.surfaceVariant,
                            contentColor = colors.textPrimary
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
                            color = colors.textPrimary,
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(colors.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = colors.textPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Sobre o VITAL",
                                    color = colors.textPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Versão 1.1.0 • Visualizador Nativo",
                                    color = colors.textSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "VITAL é um visualizador de saúde sóbrio e minimalista integrado diretamente com o Android Health Connect, Zepp OS e Room Local.",
                        color = colors.textSecondary,
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
                            containerColor = colors.primary,
                            contentColor = colors.onPrimary
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
                        color = colors.textSecondary,
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
        var editGender by remember { mutableStateOf(userProfile.gender) }

        val previewWeight = editWeightStr.replace(',', '.').toDoubleOrNull() ?: userProfile.weightKg
        val previewHeight = editHeightStr.toDoubleOrNull() ?: userProfile.heightCm
        val previewAge = editAgeStr.toIntOrNull() ?: userProfile.age
        val previewBmr = when (editGender) {
            Gender.MALE -> (10.0 * previewWeight) + (6.25 * previewHeight) - (5.0 * previewAge) + 5.0
            Gender.FEMALE -> (10.0 * previewWeight) + (6.25 * previewHeight) - (5.0 * previewAge) - 161.0
            Gender.NOT_SPECIFIED -> (10.0 * previewWeight) + (6.25 * previewHeight) - (5.0 * previewAge) - 78.0
        }

        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            containerColor = colors.surface,
            title = {
                Text("Editar Perfil & Antropometria", color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Nome Completo") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.cardBorder,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        )
                    )

                    // Seletor de Sexo para cálculo do BMR
                    Text("Sexo biológico (para cálculo basal Mifflin-St Jeor):", color = colors.textSecondary, fontSize = 11.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(Gender.MALE, Gender.FEMALE).forEach { g ->
                            val isSel = editGender == g
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSel) colors.primary else colors.surfaceVariant)
                                    .clickable { editGender = g }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = g.displayName,
                                    color = if (isSel) colors.onPrimary else colors.textPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = editAgeStr,
                            onValueChange = { editAgeStr = it.filter { c -> c.isDigit() } },
                            label = { Text("Idade (anos)") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.cardBorder,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary
                            )
                        )

                        OutlinedTextField(
                            value = editHeightStr,
                            onValueChange = { editHeightStr = it.filter { c -> c.isDigit() || c == '.' } },
                            label = { Text("Altura (cm)") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.cardBorder,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary
                            )
                        )
                    }

                    OutlinedTextField(
                        value = editWeightStr,
                        onValueChange = { editWeightStr = it.filter { c -> c.isDigit() || c == '.' || c == ',' } },
                        label = { Text("Peso (kg)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.cardBorder,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        )
                    )

                    // Estimativa live do BMR
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.surfaceVariant.copy(alpha = 0.5f))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "Gasto Basal Estimado (Mifflin-St Jeor): ~${previewBmr.toInt()} kcal/dia",
                            color = colors.textSecondary,
                            fontSize = 11.sp
                        )
                    }
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
                            weightKg = newWeight,
                            gender = editGender
                        )
                        onUpdateProfile(updated)
                        showProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary)
                ) {
                    Text("Salvar Perfil", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showProfileDialog = false }) {
                    Text("Cancelar", color = colors.textSecondary)
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
        var editGoalMode by remember { mutableStateOf(userProfile.goals.calorieGoalMode) }

        AlertDialog(
            onDismissRequest = { showGoalsDialog = false },
            containerColor = colors.surface,
            title = {
                Text("Personalizar Metas", color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Modalidade da meta calórica
                    Text("Modalidade da Meta Calórica:", color = colors.textSecondary, fontSize = 11.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CalorieGoalMode.values().forEach { mode ->
                            val isSel = editGoalMode == mode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSel) colors.primary else colors.surfaceVariant)
                                    .clickable { editGoalMode = mode }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mode.shortLabel,
                                    color = if (isSel) colors.onPrimary else colors.textPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = editCaloriesStr,
                        onValueChange = { editCaloriesStr = it.filter { c -> c.isDigit() } },
                        label = {
                            Text(
                                if (editGoalMode == CalorieGoalMode.ACTIVE_CALORIES)
                                    "Meta de Calorias Ativas (kcal)"
                                else
                                    "Meta de Gasto Total Diário (kcal)"
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.cardBorder,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        )
                    )

                    OutlinedTextField(
                        value = editStepsStr,
                        onValueChange = { editStepsStr = it.filter { c -> c.isDigit() } },
                        label = { Text("Meta Diária de Passos") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.cardBorder,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        )
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = editSleepStr,
                            onValueChange = { editSleepStr = it.filter { c -> c.isDigit() || c == '.' } },
                            label = { Text("Sono (horas)") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.cardBorder,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary
                            )
                        )

                        OutlinedTextField(
                            value = editWorkoutsStr,
                            onValueChange = { editWorkoutsStr = it.filter { c -> c.isDigit() } },
                            label = { Text("Treinos / semana") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.primary,
                                unfocusedBorderColor = colors.cardBorder,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary
                            )
                        )
                    }

                    Text(
                        text = "• Gasto Basal: energia em repouso\n• Calorias Ativas: queima em movimento\n• Gasto Total: basal + calorias ativas",
                        color = colors.textSecondary,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
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
                                calorieGoalMode = editGoalMode,
                                sleepHours = newSleep,
                                weeklyWorkoutDays = newWorkouts
                            )
                        )
                        onUpdateProfile(updated)
                        showGoalsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary)
                ) {
                    Text("Salvar Metas", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoalsDialog = false }) {
                    Text("Cancelar", color = colors.textSecondary)
                }
            }
        )
    }

    // ----------------- DIALOG 3: CONNECTED DEVICES & ORIGIN PRIORITY -----------------
    if (showDevicesDialog) {
        AlertDialog(
            onDismissRequest = { showDevicesDialog = false },
            containerColor = colors.surface,
            title = {
                Text("Dispositivos & Sensores", color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Item Health Connect
                    Card(
                        colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Android Health Connect", color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("ATIVO", color = Color(0xFF66BB6A), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Centralizador de saúde do Android. O VITAL lê os dados diretamente daqui, sem necessidade de servidores intermediários.",
                                color = colors.textSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Item Prioridade de Origens
                    Card(
                        colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Prioridade de Origens dos Dados", color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("CONFIGURADO", color = colors.primary, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "1. Zepp / Huami (com.huami.watch.hmwatchmanager) — Fonte prioritária direta do relógio Amazfit.\n2. Google Fit (com.google.android.apps.fitness) — Fonte alternativa apenas se Huami não tiver dados.\n\nProteção contra soma dupla: Registros duplicados entre o relógio e o Google Fit não são combinados nem somados.",
                                color = colors.textSecondary,
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
                        Text("Última sincronização:", color = colors.textSecondary, fontSize = 11.sp)
                        Text(lastSyncTimeStr, color = colors.textPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSyncNow()
                        showDevicesDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary)
                ) {
                    Text("Sincronizar Agora", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDevicesDialog = false }) {
                    Text("Fechar", color = colors.textSecondary)
                }
            }
        )
    }
}

@Composable
fun SettingsRowItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val colors = VitalTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(colors.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.textPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    color = colors.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    color = colors.textSecondary,
                    fontSize = 11.sp
                )
            }
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = colors.textSecondary,
            modifier = Modifier.size(18.dp)
        )
    }
}
