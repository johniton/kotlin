package com.example.erudademo


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.erudademo.ui.theme.state.DevTool
import com.example.erudademo.ui.theme.state.WebViewState

@Composable
fun NavigationControls(state: WebViewState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back/Forward/Refresh group
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White.copy(alpha = 0.15f),
            modifier = Modifier.shadow(4.dp, RoundedCornerShape(12.dp))
        ) {
            Row(modifier = Modifier.padding(4.dp)) {
                IconButton(
                    onClick = { state.goBack() },
                    enabled = state.canGoBack
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        "Back",
                        tint = if (state.canGoBack) Color.White else Color.White.copy(alpha = 0.3f)
                    )
                }
                IconButton(
                    onClick = { state.goForward() },
                    enabled = state.canGoForward
                ) {
                    Icon(
                        Icons.Default.ArrowForward,
                        "Forward",
                        tint = if (state.canGoForward) Color.White else Color.White.copy(alpha = 0.3f)
                    )
                }
                IconButton(
                    onClick = { state.reload() }
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        "Refresh",
                        tint = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Dev Tools Quick Access
        DevToolButton(
            icon = Icons.Default.Terminal,
            label = "Console",
            isActive = state.currentTool == DevTool.CONSOLE
        ) {
            state.switchTool(DevTool.CONSOLE)
        }

        DevToolButton(
            icon = Icons.Default.Code,
            label = "Elements",
            isActive = state.currentTool == DevTool.ELEMENTS
        ) {
            state.switchTool(DevTool.ELEMENTS)
        }

        DevToolButton(
            icon = Icons.Default.NetworkCheck,
            label = "Network",
            isActive = state.currentTool == DevTool.NETWORK
        ) {
            state.switchTool(DevTool.NETWORK)
        }
    }
}