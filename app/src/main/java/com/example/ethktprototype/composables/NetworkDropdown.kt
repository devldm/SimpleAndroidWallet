import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ethktprototype.Network

@Composable
fun NetworkDropdown(
    networks: List<Network>,
    selectedNetwork: MutableState<Network>,
    updateSelectedNetwork: (Network) -> Unit
) {
    val expanded = remember { mutableStateOf(false) }
    Row(modifier = Modifier.padding(16.dp, 0.dp)) {
        Text(text = "Chain:")
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .clickable(onClick = { expanded.value = true }),
            contentAlignment = Alignment.CenterStart
        ) {
            Row() {
                Text(
                    text = selectedNetwork.value.displayName,
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
                networks.forEach { network ->
                    DropdownMenuItem(
                        onClick = {
                            updateSelectedNetwork(network)
                            expanded.value = false
                        },
                        text = { NetworkItem(displayName = network.displayName) }
                    )
                }
            }
        }
    }
}


@Composable
fun NetworkItem(displayName: String) {
    Text(text = displayName)
}
