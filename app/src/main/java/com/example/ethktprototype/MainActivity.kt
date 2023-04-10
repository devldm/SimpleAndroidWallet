package com.example.ethktprototype

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ethktprototype.screens.MyMainScreen
import com.example.ethktprototype.screens.TokenListScreen
import com.example.ethktprototype.ui.theme.EthKtPrototypeTheme


class MainActivity : ComponentActivity() {
    private lateinit var viewModel: WalletViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the view model
        viewModel = ViewModelProvider(this)[WalletViewModel::class.java]
        viewModel.loadMnemonicFromPrefs()

        setContent {
            val navController = rememberNavController()
            val isWalletMnemonicInPrefs = viewModel.mnemonicLoaded.value
            val startPoint = if (!isWalletMnemonicInPrefs) "mainScreen" else "tokenList"

            EthKtPrototypeTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    NavHost(navController = navController, startDestination = startPoint) {
                        composable("mainScreen") {
                            MyMainScreen(
                                navController = navController,
                                viewModel = viewModel
                            )
                        }
                        composable("tokenList") {
                            TokenListScreen(navController, viewModel, application)
                        }
                    }
                }
            }
        }
    }
}




