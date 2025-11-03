package com.example.tempforeruda

object DevToolsInjector {

    // THIS RUNS FIRST - Captures early console logs
    val EARLY_INJECTION = """
(function() {
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

    // THIS RUNS AFTER - Loads Eruda with draggable resize functionality
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
                    displaySize: 50,
                    transparency: 0.95,
                    theme: 'dark'
                }
            });

            eruda.hide();

            // Replay early logs
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

            // ============================================
            // DRAGGABLE RESIZE FUNCTIONALITY
            // ============================================
            setTimeout(function() {
                const erudaEl = document.querySelector('#eruda');
                if (!erudaEl) {
                    console.error('❌ Eruda element not found');
                    return;
                }

                // Initial sizing based on orientation
                const isLandscape = window.innerWidth > window.innerHeight;
                let currentHeight = isLandscape ? 60 : 50; // percentage
                const minHeight = 20;
                const maxHeight = 90;

                // Create drag handle
                const dragHandle = document.createElement('div');
                dragHandle.id = 'eruda-drag-handle';
                dragHandle.innerHTML = '<div class="drag-indicator"></div>';
                erudaEl.insertBefore(dragHandle, erudaEl.firstChild);

                // Apply styles
                const style = document.createElement('style');
                style.textContent = `
                    #eruda {
                        position: fixed !important;
                        bottom: 0 !important;
                        left: 0 !important;
                        right: 0 !important;
                        width: 100% !important;
                        height: ${'$'}{currentHeight}vh !important;
                        max-width: 100% !important;
                        z-index: 9999 !important;
                        transition: none !important;
                        box-shadow: 0 -4px 20px rgba(0,0,0,0.3) !important;
                    }

                    #eruda-drag-handle {
                        position: absolute;
                        top: 0;
                        left: 0;
                        right: 0;
                        height: 40px;
                        background: linear-gradient(180deg, rgba(0,0,0,0.8) 0%, rgba(0,0,0,0.4) 100%);
                        cursor: ns-resize;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        z-index: 10000;
                        touch-action: none;
                        -webkit-user-select: none;
                        user-select: none;
                    }

                    #eruda-drag-handle .drag-indicator {
                        width: 60px;
                        height: 5px;
                        background: rgba(255,255,255,0.4);
                        border-radius: 3px;
                        transition: all 0.2s ease;
                    }

                    #eruda-drag-handle:hover .drag-indicator,
                    #eruda-drag-handle:active .drag-indicator {
                        background: rgba(255,255,255,0.8);
                        width: 80px;
                        height: 6px;
                    }

                    .eruda-console .eruda-logs,
                    .eruda-elements .eruda-section,
                    .eruda-network .eruda-requests,
                    .eruda-resources .eruda-section {
                        overflow-y: auto !important;
                        overflow-x: hidden !important;
                        max-height: calc(100% - 150px) !important;
                        -webkit-overflow-scrolling: touch !important;
                    }

                    .eruda-logs::-webkit-scrollbar,
                    .eruda-section::-webkit-scrollbar,
                    .eruda-requests::-webkit-scrollbar {
                        width: 8px !important;
                    }

                    .eruda-logs::-webkit-scrollbar-track,
                    .eruda-section::-webkit-scrollbar-track,
                    .eruda-requests::-webkit-scrollbar-track {
                        background: rgba(255,255,255,0.05) !important;
                    }

                    .eruda-logs::-webkit-scrollbar-thumb,
                    .eruda-section::-webkit-scrollbar-thumb,
                    .eruda-requests::-webkit-scrollbar-thumb {
                        background: rgba(255,255,255,0.3) !important;
                        border-radius: 4px !important;
                    }

                    .eruda-logs::-webkit-scrollbar-thumb:hover,
                    .eruda-section::-webkit-scrollbar-thumb:hover,
                    .eruda-requests::-webkit-scrollbar-thumb:hover {
                        background: rgba(255,255,255,0.5) !important;
                    }

                    .eruda-entry-btn {
                        bottom: 20px !important;
                        right: 20px !important;
                        width: 50px !important;
                        height: 50px !important;
                    }

                    /* Hide drag handle when Eruda is hidden */
                    #eruda.eruda-hide #eruda-drag-handle {
                        display: none;
                    }

                    /* Smooth resize animation */
                    #eruda.resizing {
                        transition: none !important;
                    }
                `;
                document.head.appendChild(style);

                // Drag functionality
                let isDragging = false;
                let startY = 0;
                let startHeight = currentHeight;

                function updateHeight(heightVh) {
                    currentHeight = Math.max(minHeight, Math.min(maxHeight, heightVh));
                    erudaEl.style.height = currentHeight + 'vh';

                    // Update dev-tools container
                    const devTools = erudaEl.querySelector('.eruda-dev-tools');
                    if (devTools) {
                        devTools.style.height = '100%';
                    }
                }

                function handleStart(e) {
                    isDragging = true;
                    erudaEl.classList.add('resizing');
                    startY = e.type.includes('touch') ? e.touches[0].clientY : e.clientY;
                    startHeight = currentHeight;
                    e.preventDefault();
                }

                function handleMove(e) {
                    if (!isDragging) return;

                    const clientY = e.type.includes('touch') ? e.touches[0].clientY : e.clientY;
                    const deltaY = startY - clientY;
                    const deltaVh = (deltaY / window.innerHeight) * 100;

                    updateHeight(startHeight + deltaVh);
                    e.preventDefault();
                }

                function handleEnd() {
                    if (!isDragging) return;
                    isDragging = false;
                    erudaEl.classList.remove('resizing');

                    // Haptic feedback if available
                    if (navigator.vibrate) {
                        navigator.vibrate(10);
                    }
                }

                // Touch events
                dragHandle.addEventListener('touchstart', handleStart, { passive: false });
                document.addEventListener('touchmove', handleMove, { passive: false });
                document.addEventListener('touchend', handleEnd);
                document.addEventListener('touchcancel', handleEnd);

                // Mouse events
                dragHandle.addEventListener('mousedown', handleStart);
                document.addEventListener('mousemove', handleMove);
                document.addEventListener('mouseup', handleEnd);

                // Handle orientation changes
                window.addEventListener('orientationchange', function() {
                    setTimeout(function() {
                        const newIsLandscape = window.innerWidth > window.innerHeight;
                        if (newIsLandscape !== isLandscape) {
                            currentHeight = newIsLandscape ? 60 : 50;
                            updateHeight(currentHeight);
                        }
                    }, 300);
                });

                // Initial height setup
                updateHeight(currentHeight);

                console.log('✅ Draggable resize initialized');
            }, 1000);

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

            if (window.jQuery || window.$) {
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


//package com.example.tempforeruda
//
//object DevToolsInjector {
//
//    // THIS RUNS FIRST - Captures early console logs
//    val EARLY_INJECTION = """
//(function() {
//    window._earlyLogs = [];
//
//    const original = {
//        log: console.log,
//        error: console.error,
//        warn: console.warn,
//        info: console.info,
//        debug: console.debug
//    };
//
//    function wrap(level, originalFn) {
//        return function(...args) {
//            const msg = args.map(arg => {
//                if (typeof arg === 'object') {
//                    try { return JSON.stringify(arg); }
//                    catch(e) { return String(arg); }
//                }
//                return String(arg);
//            }).join(' ');
//
//            window._earlyLogs.push({level: level, message: msg});
//            try {
//                if (window.DevBridge) {
//                    window.DevBridge.logConsole(level, msg);
//                }
//            } catch(e) {}
//            originalFn.apply(console, args);
//        };
//    }
//
//    console.log = wrap('LOG', original.log);
//    console.error = wrap('ERROR', original.error);
//    console.warn = wrap('WARN', original.warn);
//    console.info = wrap('INFO', original.info);
//    console.debug = wrap('DEBUG', original.debug);
//
//    console.log('✅ Early console capture active');
//})();
//""".trimIndent()
//
//    // THIS RUNS AFTER - Loads Eruda with draggable resize functionality
//    val LATE_INJECTION = """
//(function() {
//    console.log('🚀 DevBrowser: Loading Eruda...');
//
//    // ============================================
//    // LOAD ERUDA
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
//                    displaySize: 50,
//                    transparency: 0.95,
//                    theme: 'dark'
//                }
//            });
//
//            eruda.hide();
//
//            // Replay early logs
//            if (window._earlyLogs) {
//                window._earlyLogs.forEach(log => {
//                    const method = log.level.toLowerCase();
//                    if (console[method]) {
//                        console[method](log.message);
//                    } else {
//                        console.log('[' + log.level + '] ' + log.message);
//                    }
//                });
//                console.log('✅ Replayed ' + window._earlyLogs.length + ' early logs');
//            }
//
//            // ============================================
//            // WAIT FOR ERUDA TO BE FULLY READY
//            // ============================================
//            setTimeout(function() {
//                setupDraggableEruda();
//            }, 2000);
//
//            console.log('✅ Eruda initialized');
//        };
//
//        script.onerror = function() {
//            console.error('❌ Failed to load Eruda');
//        };
//
//        (document.head || document.documentElement).appendChild(script);
//    }
//
//    function setupDraggableEruda() {
//        const erudaEl = document.querySelector('#eruda');
//        if (!erudaEl) {
//            console.error('❌ Eruda element not found');
//            return;
//        }
//
//        console.log('🎨 Setting up draggable Eruda...');
//
//        // Initial sizing based on orientation
//        const isLandscape = () => window.innerWidth > window.innerHeight;
//        let currentHeightPx = isLandscape() ? window.innerHeight * 0.6 : window.innerHeight * 0.5;
//        const minHeightPx = window.innerHeight * 0.2;
//        const maxHeightPx = window.innerHeight * 0.9;
//
//        // Create wrapper for drag handle
//        const dragWrapper = document.createElement('div');
//        dragWrapper.id = 'eruda-drag-wrapper';
//        dragWrapper.style.cssText = 'position: fixed; bottom: 0; left: 0; right: 0; z-index: 99999; pointer-events: none;';
//        document.body.appendChild(dragWrapper);
//
//        // Create drag handle
//        const dragHandle = document.createElement('div');
//        dragHandle.id = 'eruda-drag-handle';
//        dragHandle.innerHTML = '<div class="drag-indicator"></div><div class="drag-label">Drag to resize</div>';
//        dragWrapper.appendChild(dragHandle);
//
//        // Add comprehensive styles
//        const style = document.createElement('style');
//        style.id = 'eruda-custom-styles';
//        style.textContent = `
//            /* Force Eruda positioning */
//            #eruda {
//                position: fixed !important;
//                bottom: 0 !important;
//                left: 0 !important;
//                right: 0 !important;
//                top: auto !important;
//                width: 100vw !important;
//                max-width: 100vw !important;
//                height: ${'$'}{currentHeightPx}px !important;
//                z-index: 99998 !important;
//                box-shadow: 0 -8px 32px rgba(0,0,0,0.5) !important;
//                border-radius: 20px 20px 0 0 !important;
//                overflow: hidden !important;
//            }
//
//            /* Drag wrapper */
//            #eruda-drag-wrapper {
//                height: ${'$'}{currentHeightPx}px;
//                transition: height 0.05s ease-out;
//            }
//
//            /* Drag handle styling */
//            #eruda-drag-handle {
//                position: absolute;
//                top: 0;
//                left: 0;
//                right: 0;
//                height: 50px;
//                background: linear-gradient(180deg,
//                    rgba(30, 30, 30, 0.98) 0%,
//                    rgba(30, 30, 30, 0.85) 100%);
//                cursor: ns-resize;
//                display: flex;
//                flex-direction: column;
//                align-items: center;
//                justify-content: center;
//                z-index: 100000;
//                touch-action: none;
//                -webkit-user-select: none;
//                user-select: none;
//                pointer-events: auto;
//                border-bottom: 1px solid rgba(255,255,255,0.1);
//                transition: background 0.2s ease;
//            }
//
//            #eruda-drag-handle:hover,
//            #eruda-drag-handle:active {
//                background: linear-gradient(180deg,
//                    rgba(45, 45, 45, 0.98) 0%,
//                    rgba(40, 40, 40, 0.85) 100%);
//            }
//
//            #eruda-drag-handle .drag-indicator {
//                width: 60px;
//                height: 5px;
//                background: rgba(255,255,255,0.3);
//                border-radius: 3px;
//                transition: all 0.2s ease;
//                margin-bottom: 6px;
//            }
//
//            #eruda-drag-handle:hover .drag-indicator,
//            #eruda-drag-handle:active .drag-indicator {
//                background: rgba(100, 200, 255, 0.8);
//                width: 80px;
//                height: 6px;
//                box-shadow: 0 0 10px rgba(100, 200, 255, 0.5);
//            }
//
//            #eruda-drag-handle .drag-label {
//                font-size: 11px;
//                color: rgba(255,255,255,0.5);
//                font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
//                letter-spacing: 0.5px;
//                transition: color 0.2s ease;
//            }
//
//            #eruda-drag-handle:hover .drag-label {
//                color: rgba(100, 200, 255, 0.9);
//            }
//
//            /* Adjust Eruda internal elements */
//            #eruda .eruda-dev-tools {
//                height: 100% !important;
//                padding-top: 50px !important;
//            }
//
//            #eruda .eruda-tools {
//                height: calc(100% - 50px) !important;
//            }
//
//            /* Scrollable areas */
//            .eruda-console .eruda-logs,
//            .eruda-elements .eruda-section,
//            .eruda-network .eruda-requests,
//            .eruda-resources .eruda-section {
//                overflow-y: auto !important;
//                overflow-x: hidden !important;
//                max-height: 100% !important;
//                -webkit-overflow-scrolling: touch !important;
//            }
//
//            /* Custom scrollbars */
//            .eruda-logs::-webkit-scrollbar,
//            .eruda-section::-webkit-scrollbar,
//            .eruda-requests::-webkit-scrollbar {
//                width: 10px !important;
//            }
//
//            .eruda-logs::-webkit-scrollbar-track,
//            .eruda-section::-webkit-scrollbar-track,
//            .eruda-requests::-webkit-scrollbar-track {
//                background: rgba(255,255,255,0.05) !important;
//            }
//
//            .eruda-logs::-webkit-scrollbar-thumb,
//            .eruda-section::-webkit-scrollbar-thumb,
//            .eruda-requests::-webkit-scrollbar-thumb {
//                background: rgba(100, 200, 255, 0.4) !important;
//                border-radius: 5px !important;
//            }
//
//            .eruda-logs::-webkit-scrollbar-thumb:hover,
//            .eruda-section::-webkit-scrollbar-thumb:hover,
//            .eruda-requests::-webkit-scrollbar-thumb:hover {
//                background: rgba(100, 200, 255, 0.6) !important;
//            }
//
//            /* Entry button */
//            .eruda-entry-btn {
//                bottom: 20px !important;
//                right: 20px !important;
//                width: 50px !important;
//                height: 50px !important;
//                box-shadow: 0 4px 20px rgba(0,0,0,0.3) !important;
//            }
//
//            /* Hide drag handle when Eruda is hidden */
//            #eruda.eruda-hide + #eruda-drag-wrapper {
//                display: none;
//            }
//
//            /* Active dragging state */
//            body.eruda-dragging #eruda,
//            body.eruda-dragging #eruda-drag-wrapper {
//                transition: none !important;
//            }
//
//            body.eruda-dragging {
//                cursor: ns-resize !important;
//                user-select: none !important;
//            }
//        `;
//        document.head.appendChild(style);
//
//        // Update height function
//        function updateHeight(heightPx) {
//            currentHeightPx = Math.max(minHeightPx, Math.min(maxHeightPx, heightPx));
//            erudaEl.style.height = currentHeightPx + 'px';
//            dragWrapper.style.height = currentHeightPx + 'px';
//
//            console.log('📏 Height updated:', currentHeightPx + 'px');
//        }
//
//        // Dragging state
//        let isDragging = false;
//        let startY = 0;
//        let startHeight = currentHeightPx;
//
//        function handleStart(e) {
//            isDragging = true;
//            document.body.classList.add('eruda-dragging');
//            startY = e.type.includes('touch') ? e.touches[0].clientY : e.clientY;
//            startHeight = currentHeightPx;
//            console.log('🎯 Drag started at Y:', startY);
//            e.preventDefault();
//        }
//
//        function handleMove(e) {
//            if (!isDragging) return;
//
//            const clientY = e.type.includes('touch') ? e.touches[0].clientY : e.clientY;
//            const deltaY = startY - clientY;
//            const newHeight = startHeight + deltaY;
//
//            updateHeight(newHeight);
//            e.preventDefault();
//        }
//
//        function handleEnd() {
//            if (!isDragging) return;
//            isDragging = false;
//            document.body.classList.remove('eruda-dragging');
//            console.log('✋ Drag ended at height:', currentHeightPx + 'px');
//
//            // Haptic feedback
//            if (navigator.vibrate) {
//                navigator.vibrate(20);
//            }
//        }
//
//        // Touch events
//        dragHandle.addEventListener('touchstart', handleStart, { passive: false });
//        document.addEventListener('touchmove', handleMove, { passive: false });
//        document.addEventListener('touchend', handleEnd);
//        document.addEventListener('touchcancel', handleEnd);
//
//        // Mouse events
//        dragHandle.addEventListener('mousedown', handleStart);
//        document.addEventListener('mousemove', handleMove);
//        document.addEventListener('mouseup', handleEnd);
//
//        // Handle window resize / orientation change
//        let resizeTimeout;
//        window.addEventListener('resize', function() {
//            clearTimeout(resizeTimeout);
//            resizeTimeout = setTimeout(function() {
//                const newMaxHeight = window.innerHeight * 0.9;
//                const newMinHeight = window.innerHeight * 0.2;
//
//                // Adjust current height if it's out of bounds
//                if (currentHeightPx > newMaxHeight) {
//                    updateHeight(newMaxHeight);
//                } else if (currentHeightPx < newMinHeight) {
//                    updateHeight(newMinHeight);
//                }
//
//                console.log('🔄 Window resized, adjusted constraints');
//            }, 300);
//        });
//
//        // Initial setup
//        updateHeight(currentHeightPx);
//
//        console.log('✅ Draggable Eruda setup complete!');
//        console.log('📱 Current orientation:', isLandscape() ? 'Landscape' : 'Portrait');
//        console.log('📏 Initial height:', currentHeightPx + 'px');
//    }
//
//    // ============================================
//    // NETWORK INTERCEPTOR
//    // ============================================
//    (function() {
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
//    // FRAMEWORK DETECTION
//    // ============================================
//    setTimeout(function() {
//        try {
//            if (window.React) {
//                window.DevBridge.detectFramework('React', window.React.version || 'unknown');
//            } else if (document.querySelector('[data-reactroot], [data-reactid]')) {
//                window.DevBridge.detectFramework('React', 'detected');
//            }
//
//            if (window.Vue) {
//                window.DevBridge.detectFramework('Vue', window.Vue.version || 'unknown');
//            } else if (document.querySelector('[data-v-]') || window.__VUE__) {
//                window.DevBridge.detectFramework('Vue', 'detected');
//            }
//
//            if (window.ng || window.getAllAngularRootElements) {
//                window.DevBridge.detectFramework('Angular', window.ng?.version?.full || 'detected');
//            }
//
//            if (window.__NEXT_DATA__) {
//                window.DevBridge.detectFramework('Next.js', 'detected');
//            }
//
//            if (window.__SVELTE__) {
//                window.DevBridge.detectFramework('Svelte', 'detected');
//            }
//
//            if (window.jQuery || window.$) {
//                window.DevBridge.detectFramework('jQuery', window.jQuery?.fn?.jquery || 'detected');
//            }
//
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
//    console.log('🎉 DevBrowser: All tools loaded!');
//})();
//""".trimIndent()
//}