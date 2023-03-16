package com.example.ethktprototype.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun MyRow(showPayDialog: Boolean, setShowPayDialog: (Boolean) -> Unit, showWalletModal: Boolean, setShowWalletModal: (Boolean) -> Unit) {
    // function to handle the pay button click event
    fun onPayClick() {
        setShowPayDialog(true)
    }

    fun onAddClick() {
        setShowWalletModal(true)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        MyColumn(icon = Icons.Filled.Add, text = "Add", onButtonClick = {onAddClick()})
        MyColumn(icon = Icons.Filled.Send, text = "Pay", onButtonClick = { onPayClick() })
//        MyColumn(icon = Icons.Filled.ShoppingCart, text = "Buy", onButtonClick = {})
//        MyColumn(icon = Icons.Filled.Info, text = "Try", onButtonClick = {})
    }
}

@Composable
fun MyColumn(icon: ImageVector, text: String, onButtonClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(IntrinsicSize.Min)
            .clickable(onClick = onButtonClick)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(5.dp)
            )

            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

