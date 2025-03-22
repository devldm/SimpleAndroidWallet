package com.example.ethktprototype.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.ethktprototype.WalletViewModel
import com.example.ethktprototype.composables.ClearBlocklist
import com.example.ethktprototype.composables.RemoveWallet

@Composable
fun SettingsScreen(navController: NavHostController, viewModel: WalletViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(22.dp)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        RemoveWallet(viewModel = viewModel)
        Spacer(modifier = Modifier.height(8.dp))
        ClearBlocklist(viewModel = viewModel)

    }
}