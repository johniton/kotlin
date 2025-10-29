package com.example.erudademo.ui

import android.webkit.WebView
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.erudademo.StatusBadge
import com.example.erudademo.WebViewManager
import com.example.erudademo.ui.components.TopBar

@Composable
fun WebViewScreen() {
    var url by remember { mutableStateOf("https://example.com") }
    var urlInput by remember { mutableStateOf(url) }
    var webView by remember { mutableStateOf<WebView?>(null) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var loadProgress by remember { mutableStateOf(0) }
    var showDevTools by remember { mutableStateOf(false) }
    var currentTool by remember { mutableStateOf("console") }

    val context = LocalContext.current

    // Functions
    val navigateTo: (String) -> Unit = { newUrl ->
        var finalUrl = newUrl.trim()
        if (!finalUrl.startsWith("http://") && !finalUrl.startsWith("https://")) {
            finalUrl = "https://$finalUrl"
        }
        url = finalUrl
        urlInput = finalUrl
        webView?.loadUrl(finalUrl)
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

    val switchTool: (String) -> Unit = { tool ->
        currentTool = tool
        showDevTools = true
        webView?.evaluateJavascript("eruda.show('$tool');", null)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(
                urlInput = urlInput,
                onUrlChange = { urlInput = it },
                onNavigate = { navigateTo(urlInput) },
                canGoBack = canGoBack,
                canGoForward = canGoForward,
                onBack = { webView?.goBack(); updateNavigation() },
                onForward = { webView?.goForward(); updateNavigation() },
                onRefresh = { webView?.reload() },
                onToggleDevTools = toggleDevTools,
                showDevTools = showDevTools,
                currentTool = currentTool,
                onSwitchTool = switchTool,
                isLoading = isLoading,
                loadProgress = loadProgress
            )

            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                factory = { ctx ->
                    WebViewManager.createWebView(
                        context = ctx,
                        onPageFinished = {
                            updateNavigation()
                            isLoading = false
                        },
                        onProgressChanged = { progress ->
                            loadProgress = progress
                            isLoading = progress < 100
                        }
                    ).also {
                        webView = it
                        it.loadUrl(url)
                    }
                }
            )
        }

        AnimatedVisibility(
            visible = showDevTools,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            StatusBadge(text = "${currentTool.replaceFirstChar { it.uppercase() }} Active")
        }
    }
}