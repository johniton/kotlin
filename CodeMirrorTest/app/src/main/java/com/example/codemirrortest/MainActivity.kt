//package com.example.codemirrortest
//
//import android.annotation.SuppressLint
//import android.os.Bundle
//import android.webkit.WebView
//import android.webkit.WebViewClient
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.viewinterop.AndroidView
//import com.example.codemirrortest.ui.theme.CodeMirrorTestTheme
//
//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            CodeMirrorTestTheme {
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    color = MaterialTheme.colorScheme.background
//                ) {
//                    CodeMirrorScreen()
//                }
//            }
//        }
//    }
//}
//
//@SuppressLint("SetJavaScriptEnabled")
//@Composable
//fun CodeMirrorScreen() {
//    val context = LocalContext.current
//    var webView by remember { mutableStateOf<WebView?>(null) }
//    var isLoading by remember { mutableStateOf(true) }
//
//    Column(
//        modifier = Modifier.fillMaxSize()
//    ) {
//        // Top bar with controls
//        Card(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(8.dp),
//            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
//        ) {
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    text = "CodeMirror 6 Test",
//                    style = MaterialTheme.typography.headlineSmall
//                )
//
//                Row(
//                    horizontalArrangement = Arrangement.spacedBy(8.dp)
//                ) {
//                    Button(
//                        onClick = {
//                            webView?.evaluateJavascript(
//                                "window.getEditorContent()"
//                            ) { content ->
//                                println("Editor content: $content")
//                            }
//                        },
//                        enabled = !isLoading
//                    ) {
//                        Text("Get Code")
//                    }
//
//                    Button(
//                        onClick = {
//                            webView?.evaluateJavascript(
//                                """
//                                window.setEditorContent('// New code injected from Android!\nconsole.log("Hello from Kotlin!");')
//                                """.trimIndent(),
//                                null
//                            )
//                        },
//                        enabled = !isLoading
//                    ) {
//                        Text("Set Code")
//                    }
//                }
//            }
//        }
//
//        // Loading indicator
//        if (isLoading) {
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(60.dp),
//                contentAlignment = Alignment.Center
//            ) {
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.spacedBy(8.dp)
//                ) {
//                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
//                    Text("Loading CodeMirror...")
//                }
//            }
//        }
//
//        // WebView containing CodeMirror
//        AndroidView(
//            factory = { context ->
//                WebView(context).apply {
//                    webViewClient = object : WebViewClient() {
//                        override fun onPageFinished(view: WebView?, url: String?) {
//                            super.onPageFinished(view, url)
//                            isLoading = false
//                        }
//                    }
//
//                    settings.apply {
//                        javaScriptEnabled = true
//                        domStorageEnabled = true
//                        allowFileAccess = true
//                        allowContentAccess = true
//                        setSupportZoom(true)
//                        builtInZoomControls = true
//                        displayZoomControls = false
//                    }
//
//                    loadUrl("file:///android_asset/test.html")
//                    webView = this
//                }
//            },
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(horizontal = 8.dp)
//                .padding(bottom = 8.dp)
//        )
//    }
//}
//
//@Preview(showBackground = true)
//@Composable
//fun CodeMirrorScreenPreview() {
//    CodeMirrorTestTheme {
//        // Preview shows a placeholder since WebView doesn't render in preview
//        Column(
//            modifier = Modifier.fillMaxSize().padding(16.dp),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center
//        ) {
//            Text("CodeMirror 6 Preview")
//            Text("(WebView will render in actual app)")
//        }
//    }
//}

package com.example.codemirrortest

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebView.setWebContentsDebuggingEnabled
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.codemirrortest.ui.theme.CodeMirrorTestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CodeMirrorTestTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CodeMirrorScreen()
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun CodeMirrorScreen() {
    val context = LocalContext.current
    var webView by remember { mutableStateOf<WebView?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var editorReady by remember { mutableStateOf(false) }
    var loadingProgress by remember { mutableStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
    ) {
        // Compact header bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Title row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Code Editor",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Status indicator
                    Text(
                        text = when {
                            errorMessage != null -> "Error"
                            isLoading -> "Loading... $loadingProgress%"
                            editorReady -> "Ready"
                            else -> "Initializing"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            errorMessage != null -> MaterialTheme.colorScheme.error
                            isLoading -> MaterialTheme.colorScheme.onSurface.copy(0.6f)
                            editorReady -> Color(0xFF4CAF50)
                            else -> MaterialTheme.colorScheme.onSurface.copy(0.6f)
                        }
                    )
                }

                // Show error message if any
                errorMessage?.let { error ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action buttons - more compact
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = {
                            webView?.evaluateJavascript(
                                "window.getEditorContent()"
                            ) { content ->
                                val cleanContent = content
                                    ?.removePrefix("\"")
                                    ?.removeSuffix("\"")
                                    ?.replace("\\n", "\n")
                                    ?.replace("\\\"", "\"")
                                    ?.replace("\\\\", "\\")

                                println("=== EDITOR CONTENT ===")
                                println(cleanContent)
                                println("======================")
                            }
                        },
                        enabled = editorReady,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text("Get", style = MaterialTheme.typography.bodySmall)
                    }

                    Button(
                        onClick = {
                            val newCode = """// Hello from Android!
console.log('Code injected successfully!');

function greetFromKotlin(name) {
    return `Hello from Kotlin, ${'$'}{name}!`;
}

const message = greetFromKotlin('Developer');
console.log(message);

// Try some array operations
const numbers = [1, 2, 3, 4, 5];
const doubled = numbers.map(n => n * 2);
console.log('Doubled:', doubled);"""

                            // Escape the content properly for JavaScript
                            val escapedCode = newCode
                                .replace("\\", "\\\\")
                                .replace("`", "\\`")
                                .replace("$", "\\$")

                            webView?.evaluateJavascript(
                                "window.setEditorContent(`$escapedCode`)"
                            ) { result ->
                                println("Code injection result: $result")
                            }
                        },
                        enabled = editorReady,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text("Set", style = MaterialTheme.typography.bodySmall)
                    }

                    OutlinedButton(
                        onClick = {
                            webView?.evaluateJavascript(
                                "JSON.stringify(window.getEditorStats())"
                            ) { stats ->
                                println("=== EDITOR STATS ===")
                                println(stats)
                                println("===================")
                            }
                        },
                        enabled = editorReady,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text("Stats", style = MaterialTheme.typography.bodySmall)
                    }

                    OutlinedButton(
                        onClick = {
                            webView?.reload()
                            isLoading = true
                            editorReady = false
                            errorMessage = null
                            loadingProgress = 0
                        },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text("Reload", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        // Loading indicator with progress
        if (isLoading && !editorReady) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = "Loading editor... $loadingProgress%",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        if (loadingProgress > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { loadingProgress / 100f },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }

        // WebView - takes up remaining space
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp)
                .padding(bottom = 8.dp)
        ) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)

                                // Wait a bit for JavaScript to fully initialize
                                view?.postDelayed({
                                    // Check if editor functions are available
                                    view.evaluateJavascript("typeof window.getEditorContent === 'function' && typeof window.setEditorContent === 'function'") { result ->
                                        if (result == "true") {
                                            isLoading = false
                                            editorReady = true
                                            errorMessage = null
                                            println("✅ Editor is ready!")

                                            // Test the editor functionality
                                            view.evaluateJavascript("window.getEditorContent()") { content ->
                                                println("Initial editor content length: ${content?.length ?: 0}")
                                            }
                                        } else {
                                            println("⚠️ Editor functions not available yet, result: $result")
                                            // Try again after another delay
                                            view.postDelayed({
                                                view.evaluateJavascript("typeof window.getEditorContent === 'function'") { retryResult ->
                                                    if (retryResult == "true") {
                                                        isLoading = false
                                                        editorReady = true
                                                        errorMessage = null
                                                        println("✅ Editor is ready after retry!")
                                                    } else {
                                                        isLoading = false
                                                        editorReady = false
                                                        errorMessage = "Editor failed to initialize properly"
                                                        println("❌ Editor initialization failed")
                                                    }
                                                }
                                            }, 1000)
                                        }
                                    }
                                }, 500)
                            }

                            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                isLoading = true
                                editorReady = false
                                errorMessage = null
                                loadingProgress = 0
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: android.webkit.WebResourceRequest?,
                                error: android.webkit.WebResourceError?
                            ) {
                                super.onReceivedError(view, request, error)
                                isLoading = false
                                editorReady = false
                                errorMessage = "WebView Error: ${error?.description}"
                                println("❌ WebView Error: ${error?.description}")
                            }
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                super.onProgressChanged(view, newProgress)
                                loadingProgress = newProgress
                            }

                            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                                consoleMessage?.let { msg ->
                                    println("WebView Console [${msg.messageLevel()}]: ${msg.message()}")
                                }
                                return true
                            }
                        }

                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            allowFileAccess = true
                            allowContentAccess = true

                            // Disable zoom for better mobile experience
                            setSupportZoom(false)
                            builtInZoomControls = false
                            displayZoomControls = false

                            // Performance optimizations
                            cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
                            setRenderPriority(android.webkit.WebSettings.RenderPriority.HIGH)

                            // Better text rendering
                            textZoom = 100

                            // Enable debugging
                            setWebContentsDebuggingEnabled(true)
                        }

                        // Load the HTML file
                        loadUrl("file:///android_asset/codeMirror.html")
                        webView = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CodeMirrorScreenPreview() {
    CodeMirrorTestTheme {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Code Editor Preview")
            Text("(WebView will render in actual app)")
        }
    }
}