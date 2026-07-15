package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.data.repository.GeminiRepositoryImpl
import com.example.domain.usecase.GetTroubleshootingUseCase
import com.example.ui.screens.TroubleshootScreen
import com.example.ui.screens.TroubleshootViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    // Setup Clean Architecture and MVVM manually with lazy initialization
    private val repository = GeminiRepositoryImpl()
    private val getTroubleshootingUseCase = GetTroubleshootingUseCase(repository)
    private val viewModel: TroubleshootViewModel by viewModels {
        TroubleshootViewModel.Factory(getTroubleshootingUseCase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TroubleshootScreen(viewModel = viewModel)
                }
            }
        }
    }
}
