package com.example.erudademo

import android.content.Context
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import java.io.ByteArrayInputStream
import java.net.URL

object WebViewManager {

    fun createWebView(context: Context, onPageFinished: () -> Unit, onProgressChanged: (Int) -> Unit): WebView {
        return WebView(context).apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                allowFileAccess = true
                allowContentAccess = true
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
                loadWithOverviewMode = true
                useWideViewPort = true
                mediaPlaybackRequiresUserGesture = false
                javaScriptCanOpenWindowsAutomatically = true
            }

            webViewClient = object : WebViewClient() {

                override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                    val url = request?.url?.toString() ?: return super.shouldInterceptRequest(view, request)

                    // Only intercept HTML pages (not CSS, JS, images, etc.)
                    if (request.method == "GET" &&
                        !url.endsWith(".js") &&
                        !url.endsWith(".css") &&
                        !url.endsWith(".png") &&
                        !url.endsWith(".jpg") &&
                        !url.endsWith(".jpeg") &&
                        !url.endsWith(".gif") &&
                        !url.endsWith(".svg") &&
                        !url.endsWith(".woff") &&
                        !url.endsWith(".woff2") &&
                        !url.endsWith(".ttf")) {

                        try {
                            // Fetch the original HTML
                            val connection = URL(url).openConnection()
                            connection.connectTimeout = 5000
                            connection.readTimeout = 5000

                            val inputStream = connection.getInputStream()
                            var html = inputStream.bufferedReader().use { it.readText() }
                            inputStream.close()

                            // Check if it's actually HTML
                            if (!html.trim().startsWith("<!") && !html.contains("<html")) {
                                return super.shouldInterceptRequest(view, request)
                            }

                            // Inject Eruda script at the beginning of <head> or <html>
                            val erudaInjection = """
                                <script src="https://cdn.jsdelivr.net/npm/eruda"></script>
                                <script>
                                    eruda.init({
                                        tool: ['console', 'elements', 'network', 'resources', 'sources', 'info'],
                                        useShadowDom: false,
                                        autoScale: true,
                                        defaults: {
                                            displaySize: 60,
                                            transparency: 0.95,
                                            theme: 'dark'
                                        }
                                    });
                                    eruda.hide();
                                    console.log('✅ Eruda loaded from HTML injection');
                                </script>
                            """.trimIndent()

                            // Try to inject after <head> tag
                            html = if (html.contains("<head>", ignoreCase = true)) {
                                html.replaceFirst(
                                    Regex("<head>", RegexOption.IGNORE_CASE),
                                    "<head>\n$erudaInjection"
                                )
                            } else if (html.contains("<html>", ignoreCase = true)) {
                                // If no <head>, inject after <html>
                                html.replaceFirst(
                                    Regex("<html[^>]*>", RegexOption.IGNORE_CASE),
                                    "$0\n<head>\n$erudaInjection\n</head>"
                                )
                            } else {
                                // Last resort: prepend to entire document
                                erudaInjection + html
                            }

                            // Return the modified HTML
                            return WebResourceResponse(
                                "text/html",
                                "UTF-8",
                                ByteArrayInputStream(html.toByteArray(Charsets.UTF_8))
                            )

                        } catch (e: Exception) {
                            Log.e("WebViewManager", "Failed to inject Eruda: ${e.message}")
                        }
                    }

                    return super.shouldInterceptRequest(view, request)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    onPageFinished()
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    onProgressChanged(newProgress)
                }

                override fun onConsoleMessage(message: ConsoleMessage?): Boolean {
                    message?.let {
                        val logMessage = "[${it.messageLevel()}] ${it.message()} -- ${it.sourceId()}:${it.lineNumber()}"
                        when (it.messageLevel()) {
                            ConsoleMessage.MessageLevel.ERROR -> Log.e("WebView Console", logMessage)
                            ConsoleMessage.MessageLevel.WARNING -> Log.w("WebView Console", logMessage)
                            ConsoleMessage.MessageLevel.LOG -> Log.i("WebView Console", logMessage)
                            else -> Log.d("WebView Console", logMessage)
                        }
                    }
                    return true
                }
            }
        }
    }
}