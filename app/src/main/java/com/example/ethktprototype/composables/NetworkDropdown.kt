import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
        Spacer(modifier = Modifier.width(10.dp))
        Box(modifier = Modifier.clickable(onClick = { expanded.value = true })) {
            Text(text = selectedNetwork.value.displayName, fontWeight = FontWeight.Bold)

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
