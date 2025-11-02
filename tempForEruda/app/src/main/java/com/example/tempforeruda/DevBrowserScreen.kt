package com.example.tempforeruda


import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevBrowserScreen() {
    var url by remember { mutableStateOf("https://react.dev") }
    var urlInput by remember { mutableStateOf(url) }
    var webView by remember { mutableStateOf<WebView?>(null) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var loadProgress by remember { mutableStateOf(0) }
    var showDevTools by remember { mutableStateOf(false) }
    var detectedFramework by remember { mutableStateOf("None") }
    var networkRequestCount by remember { mutableStateOf(0) }
    var consoleLogCount by remember { mutableStateOf(0) }

    val context = LocalContext.current

    val navigateTo: (String) -> Unit = { newUrl ->
        var finalUrl = newUrl.trim()
        if (!finalUrl.startsWith("http://") && !finalUrl.startsWith("https://")) {
            finalUrl = "https://$finalUrl"
        }
        url = finalUrl
        urlInput = finalUrl
        webView?.loadUrl(finalUrl)
        networkRequestCount = 0
        consoleLogCount = 0
        detectedFramework = "Detecting..."
    }

    val updateNavigation: () -> Unit = {
        canGoBack = webView?.canGoBack() ?: false
        canGoForward = webView?.canGoForward() ?: false
    }

    val toggleDevTools: () -> Unit = {
        showDevTools = !showDevTools
        webView?.evaluateJavascript(
            if (showDevTools) "eruda.show();" else "eruda.hide();",
            null
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    "DevBrowser",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )

        // URL Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button
                IconButton(
                    onClick = { webView?.goBack(); updateNavigation() },
                    enabled = canGoBack
                ) {
                    Icon(Icons.Default.ArrowBack, "Back")
                }

                // Forward Button
                IconButton(
                    onClick = { webView?.goForward(); updateNavigation() },
                    enabled = canGoForward
                ) {
                    Icon(Icons.Default.ArrowForward, "Forward")
                }

                // Reload Button
                IconButton(onClick = { webView?.reload() }) {
                    Icon(Icons.Default.Refresh, "Reload")
                }

                // URL TextField
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    placeholder = { Text("Enter URL...") },
                    trailingIcon = {
                        IconButton(onClick = { navigateTo(urlInput) }) {
                            Icon(Icons.Default.Search, "Go")
                        }
                    }
                )

                // Dev Tools Toggle
                IconButton(
                    onClick = toggleDevTools,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (showDevTools) MaterialTheme.colorScheme.primary
                        else Color.Transparent
                    )
                ) {
                    Icon(
                        Icons.Default.Build,
                        "Dev Tools",
                        tint = if (showDevTools) Color.White
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Loading Progress
        if (isLoading) {
            LinearProgressIndicator(
                progress = { loadProgress / 100f },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Stats Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Framework Badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Build,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        detectedFramework,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Network Count
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "$networkRequestCount requests",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            // Console Count
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Create,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "$consoleLogCount logs",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // WebView
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            factory = { ctx ->
                DevWebViewManager.createWebView(
                    context = ctx,
                    onPageFinished = {
                        updateNavigation()
                        isLoading = false
                    },
                    onProgressChanged = { progress ->
                        loadProgress = progress
                        isLoading = progress < 100
                    },
                    onFrameworkDetected = { framework, version ->
                        detectedFramework = "$framework $version"
                    },
                    onNetworkRequest = {
                        networkRequestCount++
                    },
                    onConsoleLog = {
                        consoleLogCount++
                    }
                ).also {
                    webView = it
                    it.loadUrl(url)
                }
            }
        )
    }
}