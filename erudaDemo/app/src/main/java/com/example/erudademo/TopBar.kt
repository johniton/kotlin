package com.example.erudademo.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.erudademo.ToolButton
import com.example.erudademo.UrlTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    urlInput: String,
    onUrlChange: (String) -> Unit,
    onNavigate: () -> Unit,
    canGoBack: Boolean,
    canGoForward: Boolean,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onRefresh: () -> Unit,
    onToggleDevTools: () -> Unit,
    showDevTools: Boolean,
    currentTool: String,
    onSwitchTool: (String) -> Unit,
    isLoading: Boolean,
    loadProgress: Int
) {
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")
    val animatedColor by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "color"
    )

    val gradientColors = listOf(
        Color(0xFF6366F1).copy(alpha = 0.85f + animatedColor * 0.15f),
        Color(0xFFA855F7).copy(alpha = 0.85f + (1f - animatedColor) * 0.15f),
        Color(0xFFEC4899).copy(alpha = 0.75f + animatedColor * 0.25f)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.horizontalGradient(gradientColors))
    ) {
        // Header
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Code,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp).padding(end = 8.dp),
                        tint = Color.White
                    )
                    Text("Dev Browser", fontWeight = FontWeight.ExtraBold, color = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            actions = {
                IconButton(onClick = onToggleDevTools) {
                    Icon(
                        if (showDevTools) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        "Toggle DevTools",
                        tint = Color.White
                    )
                }
            }
        )

        // Navigation & Tools
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.15f)
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    IconButton(onClick = onBack, enabled = canGoBack) {
                        Icon(
                            Icons.Default.ArrowBack, "Back",
                            tint = if (canGoBack) Color.White else Color.White.copy(alpha = 0.3f)
                        )
                    }
                    IconButton(onClick = onForward, enabled = canGoForward) {
                        Icon(
                            Icons.Default.ArrowForward, "Forward",
                            tint = if (canGoForward) Color.White else Color.White.copy(alpha = 0.3f)
                        )
                    }
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, "Refresh", tint = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            ToolButton(Icons.Default.Terminal, "Console", currentTool == "console") {
                onSwitchTool("console")
            }
            ToolButton(Icons.Default.Code, "Elements", currentTool == "elements") {
                onSwitchTool("elements")
            }
            ToolButton(Icons.Default.NetworkCheck, "Network", currentTool == "network") {
                onSwitchTool("network")
            }
        }

        // URL Bar
        UrlTextField(
            value = urlInput,
            onValueChange = onUrlChange,
            onGo = onNavigate
        )

        // Progress
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            LinearProgressIndicator(
                progress = { loadProgress / 100f },
                modifier = Modifier.fillMaxWidth().height(3.dp),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.2f)
            )
        }
    }
}