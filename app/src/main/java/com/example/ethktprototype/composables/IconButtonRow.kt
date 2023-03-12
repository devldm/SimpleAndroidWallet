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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun MyRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        MyColumn(icon = Icons.Filled.Add, text = "Add")
        MyColumn(icon = Icons.Filled.Send, text = "Pay")
        MyColumn(icon = Icons.Filled.ShoppingCart, text = "Buy")
        MyColumn(icon = Icons.Filled.Info, text = "Try")
    }
}

@Composable
fun MyColumn(icon: ImageVector, text: String) {
    Box(
        modifier = Modifier
            .height(IntrinsicSize.Min)
            .clickable(onClick = {
                //TODO: Handle button click
            })
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            IconButton(
                onClick = {
                    // TODO: Handle button click
                },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(5.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = text
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

