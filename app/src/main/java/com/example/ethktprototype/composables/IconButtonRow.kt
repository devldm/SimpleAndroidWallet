package com.example.ethktprototype.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun MyRow(
    setShowPayDialog: (Boolean) -> Unit,
    setShowWalletModal: (Boolean) -> Unit,
) {

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
        MyColumn(
            icon = Icons.Filled.ArrowDownward,
            text = "Receive",
            onButtonClick = { onAddClick() })
        MyColumn(icon = Icons.Filled.ArrowOutward, text = "Send", onButtonClick = { onPayClick() })
//        MyColumn(icon = Icons.Filled.ShoppingCart, text = "Buy", onButtonClick = {})
//        MyColumn(icon = Icons.Filled.Info, text = "Try", onButtonClick = {})
    }
}

@Composable
fun MyColumn(icon: ImageVector, text: String, onButtonClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable(onClick = onButtonClick)
    ) {

        IconButton(
            onClick = onButtonClick,
            modifier = Modifier
                .size(56.dp)
                .background(
                    MaterialTheme.colorScheme.primary,
                    CircleShape
                )

        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(30.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}


