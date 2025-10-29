
package com.example.erudademo

import android.os.Bundle
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF6366F1),
                    secondary = Color(0xFFA855F7),
                    tertiary = Color(0xFFEC4899),
                    background = Color(0xFF0F172A),
                    surface = Color(0xFF1E293B),
                    onPrimary = Color.White,
                    onSecondary = Color.White,
                    onBackground = Color(0xFFE2E8F0),
                    onSurface = Color(0xFFE2E8F0)
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WebViewScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
    var currentTool by remember { mutableStateOf("Console") }

    // Animated gradient colors
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")
    val animatedColor by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "color"
    )

    val gradientColors = listOf(
        Color(0xFF6366F1).copy(alpha = 0.8f + animatedColor * 0.2f),
        Color(0xFFA855F7).copy(alpha = 0.8f + (1f - animatedColor) * 0.2f),
        Color(0xFFEC4899).copy(alpha = 0.7f + animatedColor * 0.3f)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Sexy gradient top bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(gradientColors)
                    )
            ) {
                Column {
                    // Top App Bar
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Code,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .padding(end = 8.dp),
                                    tint = Color.White
                                )
                                Text(
                                    "Dev Browser",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        ),
                        actions = {
                            IconButton(onClick = {
                                showDevTools = !showDevTools
                                webView?.evaluateJavascript(
                                    if (showDevTools) "eruda.show();" else "eruda.hide();",
                                    null
                                )
                            }) {
                                Icon(
                                    if (showDevTools) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    "Toggle DevTools",
                                    tint = Color.White
                                )
                            }
                        }
                    )

                    // Navigation Controls
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
                                    onClick = { webView?.goBack() },
                                    enabled = canGoBack
                                ) {
                                    Icon(
                                        Icons.Default.ArrowBack,
                                        "Back",
                                        tint = if (canGoBack) Color.White else Color.White.copy(alpha = 0.3f)
                                    )
                                }
                                IconButton(
                                    onClick = { webView?.goForward() },
                                    enabled = canGoForward
                                ) {
                                    Icon(
                                        Icons.Default.ArrowForward,
                                        "Forward",
                                        tint = if (canGoForward) Color.White else Color.White.copy(alpha = 0.3f)
                                    )
                                }
                                IconButton(
                                    onClick = { webView?.reload() }
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
                            isActive = currentTool == "Console"
                        ) {
                            currentTool = "Console"
                            showDevTools = true
                            webView?.evaluateJavascript("eruda.show('console');", null)
                        }

                        DevToolButton(
                            icon = Icons.Default.Code,
                            label = "Inspect",
                            isActive = currentTool == "Inspect"
                        ) {
                            currentTool = "Inspect"
                            showDevTools = true
                            webView?.evaluateJavascript("eruda.show('elements');", null)
                        }

                        DevToolButton(
                            icon = Icons.Default.NetworkCheck,
                            label = "Network",
                            isActive = currentTool == "Network"
                        ) {
                            currentTool = "Network"
                            showDevTools = true
                            webView?.evaluateJavascript("eruda.show('network');", null)
                        }
                    }

                    // URL Bar
                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                            .padding(bottom = 12.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.15f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                            focusedBorderColor = Color.White.copy(alpha = 0.5f),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White.copy(alpha = 0.9f),
                            cursorColor = Color.White
                        ),
                        placeholder = {
                            Text(
                                "Enter URL...",
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Language,
                                null,
                                tint = Color.White.copy(alpha = 0.7f)
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    url = urlInput
                                    webView?.loadUrl(url)
                                },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                            ) {
                                Icon(
                                    Icons.Default.Send,
                                    "Go",
                                    tint = Color.White
                                )
                            }
                        },
                        singleLine = true
                    )

                    // Animated Progress Bar
                    AnimatedVisibility(
                        visible = isLoading,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        LinearProgressIndicator(
                            progress = loadProgress / 100f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.2f)
                        )
                    }
                }
            }

            // WebView
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                factory = { context ->
                    WebView(context).apply {
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            allowFileAccess = true
                            allowContentAccess = true
                            allowFileAccessFromFileURLs = true
                            allowUniversalAccessFromFileURLs = true
                            setSupportZoom(true)
                            builtInZoomControls = true
                            displayZoomControls = false
                            loadWithOverviewMode = true
                            useWideViewPort = true
                        }

                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                canGoBack = view?.canGoBack() ?: false
                                canGoForward = view?.canGoForward() ?: false
                                isLoading = false

                                view?.let { wv ->
                                    val erudaJs = loadAssetFile(context, "eruda.js")
                                    if (erudaJs.isNotEmpty()) {
                                        wv.evaluateJavascript(erudaJs, null)
                                        wv.evaluateJavascript("""
                                            eruda.init({
                                                tool: ['console', 'elements', 'network', 'resources', 'sources', 'info', 'snippets'],
                                                useShadowDom: true,
                                                autoScale: true,
                                                defaults: {
                                                    displaySize: 50,
                                                    transparency: 0.95,
                                                    theme: 'dark'
                                                }
                                            });
                                        """.trimIndent(), null)
                                    }
                                }
                            }

                            override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
                                super.doUpdateVisitedHistory(view, url, isReload)
                                urlInput = url ?: ""
                                canGoBack = view?.canGoBack() ?: false
                                canGoForward = view?.canGoForward() ?: false
                            }
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                super.onProgressChanged(view, newProgress)
                                loadProgress = newProgress
                                isLoading = newProgress < 100
                            }
                        }

                        loadUrl(url)
                        webView = this
                    }
                },
                update = { view ->
                    if (view.url != url) {
                        view.loadUrl(url)
                    }
                }
            )
        }

        // Floating status indicator
        AnimatedVisibility(
            visible = showDevTools,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF6366F1),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Text(
                        "$currentTool Active",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun DevToolButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isActive)
            Color.White.copy(alpha = 0.3f)
        else
            Color.White.copy(alpha = 0.1f),
        modifier = Modifier.shadow(
            if (isActive) 6.dp else 2.dp,
            RoundedCornerShape(12.dp)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                icon,
                label,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

fun loadAssetFile(context: android.content.Context, fileName: String): String {
    return try {
        val inputStream = context.assets.open(fileName)
        val reader = BufferedReader(InputStreamReader(inputStream))
        val stringBuilder = StringBuilder()
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            stringBuilder.append(line).append("\n")
        }
        reader.close()
        stringBuilder.toString()
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}