package com.vital.health

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.vital.health.healthconnect.HealthConnectRepository
import com.vital.health.theme.DeepBlack
import com.vital.health.theme.VitalTheme
import com.vital.health.ui.components.VitalBottomBar
import com.vital.health.ui.components.VitalTab

class MainActivity : ComponentActivity() {

    private val repository: HealthConnectRepository by lazy {
        HealthConnectRepository(this, (application as VitalApplication).database)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            VitalTheme {
                var currentTab by remember { mutableStateOf(VitalTab.HOME) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = DeepBlack,
                    bottomBar = {
                        VitalBottomBar(
                            currentTab = currentTab,
                            onTabSelected = { currentTab = it }
                        )
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        // Navegação entre as telas MVVM (Sono, Exercícios, Início, Configurações)
                    }
                }
            }
        }
    }
}
