package com.example.erudademo.ui.theme.state


import android.webkit.WebView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class WebViewState {
    var url by mutableStateOf("https://example.com")
    var urlInput by mutableStateOf(url)
    var webView by mutableStateOf<WebView?>(null)
    var canGoBack by mutableStateOf(false)
    var canGoForward by mutableStateOf(false)
    var isLoading by mutableStateOf(false)
    var loadProgress by mutableStateOf(0)
    var showDevTools by mutableStateOf(false)
    var currentTool by mutableStateOf(DevTool.CONSOLE)
    var isErudaInitialized by mutableStateOf(false)

    fun navigateTo(newUrl: String) {
        url = newUrl
        urlInput = newUrl
        webView?.loadUrl(newUrl)
    }

    fun goBack() {
        webView?.goBack()
    }

    fun goForward() {
        webView?.goForward()
    }

    fun reload() {
        webView?.reload()
    }

    fun toggleDevTools() {
        showDevTools = !showDevTools
        webView?.evaluateJavascript(
            if (showDevTools) "eruda.show();" else "eruda.hide();",
            null
        )
    }

    fun switchTool(tool: DevTool) {
        currentTool = tool
        showDevTools = true
        webView?.evaluateJavascript(
            "eruda.show('${tool.erudaName}');",
            null
        )
    }

    fun updateNavigationState(view: WebView?) {
        canGoBack = view?.canGoBack() ?: false
        canGoForward = view?.canGoForward() ?: false
    }
}

enum class DevTool(val displayName: String, val erudaName: String) {
    CONSOLE("Console", "console"),
    ELEMENTS("Elements", "elements"),
    NETWORK("Network", "network"),
    RESOURCES("Resources", "resources"),
    SOURCES("Sources", "sources"),
    INFO("Info", "info")
}