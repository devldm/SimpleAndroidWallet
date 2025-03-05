package com.example.ethktprototype

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ethktprototype.screens.ImportWalletScreen
import com.example.ethktprototype.screens.SettingsScreen
import com.example.ethktprototype.screens.TokenListScreen
import com.example.ethktprototype.ui.theme.EthKtPrototypeTheme


class MainActivity : ComponentActivity() {
    private lateinit var viewModel: WalletViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[WalletViewModel::class.java]

        setContent {
            val navController = rememberNavController()
            val uiState by viewModel.uiState.collectAsState()
            val startPoint = if (!uiState.mnemonicLoaded) "importWallet" else "tokenList"

            EthKtPrototypeTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    NavHost(navController = navController, startDestination = startPoint) {
                        composable("importWallet") {
                            ImportWalletScreen(
                                navController = navController,
                                viewModel = viewModel
                            )
                        }
                        composable("tokenList") {
                            TokenListScreen(navController, viewModel, application)
                        }
                        composable("settingsScreen") {
                            SettingsScreen(navController = navController, viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}




