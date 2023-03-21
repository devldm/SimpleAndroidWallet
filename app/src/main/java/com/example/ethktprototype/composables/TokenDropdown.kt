package com.example.ethktprototype.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ethktprototype.TokenBalance

@Composable
fun TokenDropdown(
    tokens: List<TokenBalance>,
    selectedToken: TokenBalance?,
    updateSelectedToken: (TokenBalance) -> Unit
) {
    val expanded = remember { mutableStateOf(false) }
    Row(modifier = Modifier.padding(16.dp, 0.dp)) {
        Text(text = "Token:")
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .clickable(onClick = { expanded.value = true }),
            contentAlignment = Alignment.CenterStart
        ) {
            Row() {
                Text(
                    text = "${selectedToken?.name}",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                )
            }


            DropdownMenu(
                expanded = expanded.value,
                onDismissRequest = { expanded.value = false },
            ) {
                tokens.forEach { token ->
                    DropdownMenuItem(
                        onClick = {
                            updateSelectedToken(token)
                            expanded.value = false
                        },
                        text = { TokenItem(name = "${token.name}") }
                    )
                }
            }
        }
    }
}


@Composable
fun TokenItem(name: String) {
    Text(text = name)
}
