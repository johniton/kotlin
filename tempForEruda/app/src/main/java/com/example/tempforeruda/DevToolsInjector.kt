//package com.example.tempforeruda
//
//
//object DevToolsInjector {
//
//    val COMPLETE_INJECTION = """
//(function() {
//    console.log('🚀 DevBrowser: Initializing...');
//
//    // ============================================
//    // STEP 1: CONSOLE INTERCEPTOR (IMMEDIATE)
//    // ============================================
//    (function() {
//        const original = {
//            log: console.log,
//            error: console.error,
//            warn: console.warn,
//            info: console.info
//        };
//
//        function wrap(level, originalFn) {
//            return function(...args) {
//                try {
//                    if (window.DevBridge) {
//                        window.DevBridge.logConsole(level, args.join(' '));
//                    }
//                } catch(e) {}
//                originalFn.apply(console, args);
//            };
//        }
//
//        console.log = wrap('LOG', original.log);
//        console.error = wrap('ERROR', original.error);
//        console.warn = wrap('WARN', original.warn);
//        console.info = wrap('INFO', original.info);
//
//        console.log('✅ Console interceptor active');
//    })();
//
//    // ============================================
//    // STEP 2: NETWORK INTERCEPTOR
//    // ============================================
//    (function() {
//        // Fetch Interceptor
//        const originalFetch = window.fetch;
//        window.fetch = function(...args) {
//            const startTime = Date.now();
//            const url = typeof args[0] === 'string' ? args[0] : args[0]?.url;
//            const method = args[1]?.method || 'GET';
//
//            return originalFetch.apply(this, args)
//                .then(response => {
//                    const duration = Date.now() - startTime;
//                    const clonedResponse = response.clone();
//
//                    clonedResponse.text().then(body => {
//                        try {
//                            window.DevBridge.logNetwork(JSON.stringify({
//                                url: url,
//                                method: method,
//                                status: response.status,
//                                statusText: response.statusText,
//                                duration: duration,
//                                type: 'fetch',
//                                contentType: response.headers.get('content-type') || 'unknown',
//                                bodySize: body.length
//                            }));
//                        } catch(e) {}
//                    }).catch(() => {});
//
//                    return response;
//                })
//                .catch(error => {
//                    console.error('Fetch error:', error);
//                    throw error;
//                });
//        };
//
//        // XHR Interceptor
//        const XHR = XMLHttpRequest.prototype;
//        const originalOpen = XHR.open;
//        const originalSend = XHR.send;
//
//        XHR.open = function(method, url) {
//            this._devData = { method, url, startTime: Date.now() };
//            return originalOpen.apply(this, arguments);
//        };
//
//        XHR.send = function(body) {
//            const xhr = this;
//
//            xhr.addEventListener('load', function() {
//                const duration = Date.now() - xhr._devData.startTime;
//
//                try {
//                    window.DevBridge.logNetwork(JSON.stringify({
//                        url: xhr._devData.url,
//                        method: xhr._devData.method,
//                        status: xhr.status,
//                        statusText: xhr.statusText,
//                        duration: duration,
//                        type: 'xhr',
//                        contentType: xhr.getResponseHeader('content-type') || 'unknown',
//                        bodySize: xhr.responseText?.length || 0
//                    }));
//                } catch(e) {}
//            });
//
//            return originalSend.apply(this, arguments);
//        };
//
//        // WebSocket Interceptor
//        const OriginalWebSocket = window.WebSocket;
//        window.WebSocket = function(url, protocols) {
//            console.log('🔌 WebSocket:', url);
//            const ws = new OriginalWebSocket(url, protocols);
//
//            ws.addEventListener('message', (event) => {
//                console.log('📩 WS Message:', event.data?.substring(0, 100));
//            });
//
//            return ws;
//        };
//
//        console.log('✅ Network interceptor active');
//    })();
//
//    // ============================================
//    // STEP 3: FRAMEWORK DETECTION
//    // ============================================
//    setTimeout(function() {
//        try {
//            // React
//            if (window.React) {
//                window.DevBridge.detectFramework('React', window.React.version || 'unknown');
//            } else if (document.querySelector('[data-reactroot], [data-reactid]')) {
//                window.DevBridge.detectFramework('React', 'detected');
//            }
//
//            // Vue
//            if (window.Vue) {
//                window.DevBridge.detectFramework('Vue', window.Vue.version || 'unknown');
//            } else if (document.querySelector('[data-v-]') || window.__VUE__) {
//                window.DevBridge.detectFramework('Vue', 'detected');
//            }
//
//            // Angular
//            if (window.ng || window.getAllAngularRootElements) {
//                window.DevBridge.detectFramework('Angular', window.ng?.version?.full || 'detected');
//            }
//
//            // Next.js
//            if (window.__NEXT_DATA__) {
//                window.DevBridge.detectFramework('Next.js', 'detected');
//            }
//
//            // Svelte
//            if (window.__SVELTE__) {
//                window.DevBridge.detectFramework('Svelte', 'detected');
//            }
//
//            // jQuery
//            if (window.jQuery || window.${'$'}) {
//                window.DevBridge.detectFramework('jQuery', window.jQuery?.fn?.jquery || 'detected');
//            }
//
//            // GraphQL/Apollo
//            if (window.__APOLLO_CLIENT__) {
//                window.DevBridge.detectFramework('Apollo', 'detected');
//            }
//
//            console.log('✅ Framework detection complete');
//        } catch(e) {
//            console.error('Framework detection error:', e);
//        }
//    }, 1500);
//
//    // ============================================
//    // STEP 4: LOAD ERUDA
//    // ============================================
//    if (typeof eruda === 'undefined') {
//        const script = document.createElement('script');
//        script.src = 'https://cdn.jsdelivr.net/npm/eruda';
//        script.onload = function() {
//            eruda.init({
//                tool: ['console', 'elements', 'network', 'resources', 'sources', 'info', 'snippets'],
//                useShadowDom: false,
//                autoScale: true,
//                defaults: {
//                    displaySize: 60,
//                    transparency: 0.9,
//                    theme: 'dark'
//                }
//            });
//            eruda.hide();
//            console.log('✅ Eruda initialized');
//        };
//        script.onerror = function() {
//            console.error('❌ Failed to load Eruda');
//        };
//        (document.head || document.documentElement).appendChild(script);
//    }
//
//    console.log('🎉 DevBrowser: Ready!');
//})();
//    """.trimIndent()
//}


package com.example.tempforeruda


object DevToolsInjector {

    // THIS RUNS FIRST - Captures early console logs
    val EARLY_INJECTION = """
(function() {
    // Immediately capture console logs in a buffer
    window._earlyLogs = [];
    
    const original = {
        log: console.log,
        error: console.error,
        warn: console.warn,
        info: console.info,
        debug: console.debug
    };
    
    function wrap(level, originalFn) {
        return function(...args) {
            const msg = args.map(arg => {
                if (typeof arg === 'object') {
                    try { return JSON.stringify(arg); }
                    catch(e) { return String(arg); }
                }
                return String(arg);
            }).join(' ');
            
            window._earlyLogs.push({level: level, message: msg});
            try {
                if (window.DevBridge) {
                    window.DevBridge.logConsole(level, msg);
                }
            } catch(e) {}
            originalFn.apply(console, args);
        };
    }
    
    console.log = wrap('LOG', original.log);
    console.error = wrap('ERROR', original.error);
    console.warn = wrap('WARN', original.warn);
    console.info = wrap('INFO', original.info);
    console.debug = wrap('DEBUG', original.debug);
    
    console.log('✅ Early console capture active');
})();
""".trimIndent()

    // THIS RUNS AFTER - Loads Eruda and replays logs
    val LATE_INJECTION = """
(function() {
    console.log('🚀 DevBrowser: Loading Eruda...');
    
    // ============================================
    // LOAD ERUDA
    // ============================================
    if (typeof eruda === 'undefined') {
        const script = document.createElement('script');
        script.src = 'https://cdn.jsdelivr.net/npm/eruda';
        script.onload = function() {
            eruda.init({
                tool: ['console', 'elements', 'network', 'resources', 'sources', 'info', 'snippets'],
                useShadowDom: false,
                autoScale: true,
                defaults: {
                    displaySize: 60,
                    transparency: 0.9,
                    theme: 'dark'
                }
            });
            eruda.hide();
            
        // Replay early logs into Eruda
if (window._earlyLogs) {
    window._earlyLogs.forEach(log => {
        const method = log.level.toLowerCase();
        if (console[method]) {
            console[method](log.message);
        } else {
            console.log('[' + log.level + '] ' + log.message);
        }
    });
    console.log('✅ Replayed ' + window._earlyLogs.length + ' early logs');
}
            
            console.log('✅ Eruda initialized');
        };
        script.onerror = function() {
            console.error('❌ Failed to load Eruda');
        };
        (document.head || document.documentElement).appendChild(script);
    }
    
    // ============================================
    // NETWORK INTERCEPTOR
    // ============================================
    (function() {
        const originalFetch = window.fetch;
        window.fetch = function(...args) {
            const startTime = Date.now();
            const url = typeof args[0] === 'string' ? args[0] : args[0]?.url;
            const method = args[1]?.method || 'GET';
            
            return originalFetch.apply(this, args)
                .then(response => {
                    const duration = Date.now() - startTime;
                    const clonedResponse = response.clone();
                    
                    clonedResponse.text().then(body => {
                        try {
                            window.DevBridge.logNetwork(JSON.stringify({
                                url: url,
                                method: method,
                                status: response.status,
                                statusText: response.statusText,
                                duration: duration,
                                type: 'fetch',
                                contentType: response.headers.get('content-type') || 'unknown',
                                bodySize: body.length
                            }));
                        } catch(e) {}
                    }).catch(() => {});
                    
                    return response;
                })
                .catch(error => {
                    console.error('Fetch error:', error);
                    throw error;
                });
        };
        
        const XHR = XMLHttpRequest.prototype;
        const originalOpen = XHR.open;
        const originalSend = XHR.send;
        
        XHR.open = function(method, url) {
            this._devData = { method, url, startTime: Date.now() };
            return originalOpen.apply(this, arguments);
        };
        
        XHR.send = function(body) {
            const xhr = this;
            
            xhr.addEventListener('load', function() {
                const duration = Date.now() - xhr._devData.startTime;
                
                try {
                    window.DevBridge.logNetwork(JSON.stringify({
                        url: xhr._devData.url,
                        method: xhr._devData.method,
                        status: xhr.status,
                        statusText: xhr.statusText,
                        duration: duration,
                        type: 'xhr',
                        contentType: xhr.getResponseHeader('content-type') || 'unknown',
                        bodySize: xhr.responseText?.length || 0
                    }));
                } catch(e) {}
            });
            
            return originalSend.apply(this, arguments);
        };
        
        const OriginalWebSocket = window.WebSocket;
        window.WebSocket = function(url, protocols) {
            console.log('🔌 WebSocket:', url);
            const ws = new OriginalWebSocket(url, protocols);
            
            ws.addEventListener('message', (event) => {
                console.log('📩 WS Message:', event.data?.substring(0, 100));
            });
            
            return ws;
        };
        
        console.log('✅ Network interceptor active');
    })();
    
    // ============================================
    // FRAMEWORK DETECTION
    // ============================================
    setTimeout(function() {
        try {
            if (window.React) {
                window.DevBridge.detectFramework('React', window.React.version || 'unknown');
            } else if (document.querySelector('[data-reactroot], [data-reactid]')) {
                window.DevBridge.detectFramework('React', 'detected');
            }
            
            if (window.Vue) {
                window.DevBridge.detectFramework('Vue', window.Vue.version || 'unknown');
            } else if (document.querySelector('[data-v-]') || window.__VUE__) {
                window.DevBridge.detectFramework('Vue', 'detected');
            }
            
            if (window.ng || window.getAllAngularRootElements) {
                window.DevBridge.detectFramework('Angular', window.ng?.version?.full || 'detected');
            }
            
            if (window.__NEXT_DATA__) {
                window.DevBridge.detectFramework('Next.js', 'detected');
            }
            
            if (window.__SVELTE__) {
                window.DevBridge.detectFramework('Svelte', 'detected');
            }
            
            if (window.jQuery || window.${'$'}) {
                window.DevBridge.detectFramework('jQuery', window.jQuery?.fn?.jquery || 'detected');
            }
            
            if (window.__APOLLO_CLIENT__) {
                window.DevBridge.detectFramework('Apollo', 'detected');
            }
            
            console.log('✅ Framework detection complete');
        } catch(e) {
            console.error('Framework detection error:', e);
        }
    }, 1500);
    
    console.log('🎉 DevBrowser: All tools loaded!');
})();
    """.trimIndent()
}