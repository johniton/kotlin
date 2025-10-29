package com.example.virtualkeyboard.vision

import android.content.Context
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.virtualkeyboard.viewmodel.VirtualKeyboardViewModel
import com.google.mediapipe.framework.image.BitmapImageBuilder

private const val TAG = "VirtualKeyboard"

class MediaPipeHandTracker(
    private val context: Context,
    private val onHandLandmarks: (x: Float, y: Float, z: Float, timestamp: Long, handIndex: Int) -> Unit,
    private val onError: (String) -> Unit
) {
    private var handLandmarker: HandLandmarker? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private var isInitialized = false

    fun initialize() {
        try {
            Log.d(TAG, "MediaPipe: Starting initialization")

            val baseOptions = BaseOptions.builder()
                .setModelAssetPath("hand_landmarker.task")
                .build()
            Log.d(TAG, "MediaPipe: BaseOptions created")

            val options = HandLandmarker.HandLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setNumHands(2)
                .setMinHandDetectionConfidence(0.3f)  // Lower from 0.5f
                .setMinTrackingConfidence(0.3f)        // Lower from 0.5f
                .setMinHandPresenceConfidence(0.3f)    // Lower from 0.5f
                .setResultListener(::onHandLandmarkerResult)
                .setErrorListener(::onHandLandmarkerError)
                .build()
            Log.d(TAG, "MediaPipe: Options configured")

            handLandmarker = HandLandmarker.createFromOptions(context, options)
            isInitialized = true
            Log.d(TAG, "MediaPipe: Initialization SUCCESS")
        } catch (e: Exception) {
            Log.e(TAG, "MediaPipe: Initialization FAILED", e)
            onError("Failed to initialize MediaPipe: ${e.message}")
        }
    }

    fun detectAsync(mpImage: MPImage, timestamp: Long) {
        try {
            if (!isInitialized) {
                Log.w(TAG, "MediaPipe: detectAsync called but not initialized")
                return
            }
            handLandmarker?.detectAsync(mpImage, timestamp)
        } catch (e: Exception) {
            Log.e(TAG, "MediaPipe: detectAsync failed", e)
        }
    }

    // In MediaPipeHandTracker.kt - update onHandLandmarkerResult callback

    private fun onHandLandmarkerResult(
        result: HandLandmarkerResult,
        input: MPImage
    ) {
        try {
            if (result.landmarks().isNotEmpty()) {
                result.landmarks().forEachIndexed { handIndex, landmarks ->
                    if (landmarks.size > 8) {
                        val indexFingerTip = landmarks[8]

                        val imageWidth = input.width
                        val imageHeight = input.height

                        val pixelX = indexFingerTip.x() * imageWidth
                        val pixelY = indexFingerTip.y() * imageHeight
                        val depthZ = indexFingerTip.z()

                        // Pass actual Z coordinate from MediaPipe
                        onHandLandmarks(pixelX, pixelY, depthZ, System.nanoTime(), handIndex)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "MediaPipe: Error in result processing", e)
        }
    }

    private fun onHandLandmarkerError(error: RuntimeException) {
        Log.e(TAG, "MediaPipe: Landmarker error", error)
        onError("MediaPipe Error: ${error.message}")
    }

    fun close() {
        try {
            Log.d(TAG, "MediaPipe: Closing")
            handLandmarker?.close()
            isInitialized = false
            Log.d(TAG, "MediaPipe: Closed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "MediaPipe: Error during close", e)
        }
    }
}


class HandAnalyzer(
    private val handTracker: MediaPipeHandTracker
) : ImageAnalysis.Analyzer {

    private var frameCount = 0

    override fun analyze(imageProxy: ImageProxy) {
        try {
            frameCount++
            if (frameCount % 30 == 0) {
                Log.v(TAG, "HandAnalyzer: Processing frame $frameCount")
            }

            val bitmap = imageProxy.toBitmap()
            val mpImage = BitmapImageBuilder(bitmap).build()
            handTracker.detectAsync(mpImage, imageProxy.imageInfo.timestamp)
            imageProxy.close()
        } catch (e: Exception) {
            Log.e(TAG, "HandAnalyzer: Error in analyze", e)
            imageProxy.close()
        }
    }
}


class CameraManager(
    private val context: Context,
    private val lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    private val viewModel: VirtualKeyboardViewModel
) : DefaultLifecycleObserver {

    private var cameraProvider: androidx.camera.lifecycle.ProcessCameraProvider? = null
    private var handTracker: MediaPipeHandTracker? = null
    private var previewView: androidx.camera.view.PreviewView? = null
    private var onCameraReadyCallback: (() -> Unit)? = null
    private var onErrorCallback: ((String) -> Unit)? = null
    private var isBound = false

    init {
        Log.d(TAG, "CameraManager: Constructor called")
        lifecycleOwner.lifecycle.addObserver(this)
    }

    override fun onCreate(owner: LifecycleOwner) {
        Log.d(TAG, "CameraManager: onCreate")
    }

    override fun onStart(owner: LifecycleOwner) {
        Log.d(TAG, "CameraManager: onStart")
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        Log.d(TAG, "CameraManager: onResume - isBound=$isBound")

        if (!isBound && previewView != null && cameraProvider != null) {
            Log.d(TAG, "CameraManager: Rebinding camera on resume")
            previewView?.let { preview ->
                bindCamera(preview, onCameraReadyCallback ?: {}, onErrorCallback ?: {})
            }
        }
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        Log.d(TAG, "CameraManager: onPause - unbinding camera")
        try {
            cameraProvider?.unbindAll()
            isBound = false
            Log.d(TAG, "CameraManager: Camera unbound successfully")
        } catch (e: Exception) {
            Log.e(TAG, "CameraManager: Error unbinding camera", e)
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        Log.d(TAG, "CameraManager: onStop")
    }

    override fun onDestroy(owner: LifecycleOwner) {
        Log.d(TAG, "CameraManager: onDestroy")
    }

    fun setupCamera(
        previewView: androidx.camera.view.PreviewView,
        onCameraReady: () -> Unit,
        onError: (String) -> Unit
    ) {
        Log.d(TAG, "CameraManager: setupCamera called")

        try {
            this.previewView = previewView
            this.onCameraReadyCallback = onCameraReady
            this.onErrorCallback = onError

            Log.d(TAG, "CameraManager: Creating MediaPipe tracker")
            handTracker = MediaPipeHandTracker(
                context = context,
                onHandLandmarks = { x, y, z, timestamp, handIndex ->
                    try {
                        viewModel.updateFingerPosition(x, y, z, timestamp, handIndex)
                    } catch (e: Exception) {
                        Log.e(TAG, "CameraManager: Error in hand landmark callback", e)
                    }
                },
                onError = { error ->
                    Log.e(TAG, "CameraManager: MediaPipe error - $error")
                    onError(error)
                }
            )

            Log.d(TAG, "CameraManager: Starting MediaPipe initialization thread")
            Thread {
                try {
                    handTracker?.initialize()
                    Log.d(TAG, "CameraManager: MediaPipe initialization thread completed")
                } catch (e: Exception) {
                    Log.e(TAG, "CameraManager: MediaPipe initialization thread failed", e)
                }
            }.start()

            Log.d(TAG, "CameraManager: Getting CameraProvider")
            val cameraProviderFuture = androidx.camera.lifecycle.ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                try {
                    Log.d(TAG, "CameraManager: CameraProvider future callback")
                    cameraProvider = cameraProviderFuture.get()
                    Log.d(TAG, "CameraManager: CameraProvider obtained, binding camera")
                    bindCamera(previewView, onCameraReady, onError)
                } catch (e: Exception) {
                    Log.e(TAG, "CameraManager: Failed to get camera provider", e)
                    onError("Camera initialization failed: ${e.message}")
                }
            }, androidx.core.content.ContextCompat.getMainExecutor(context))
        } catch (e: Exception) {
            Log.e(TAG, "CameraManager: setupCamera failed", e)
            onError("Setup failed: ${e.message}")
        }
    }

    private fun bindCamera(
        previewView: androidx.camera.view.PreviewView,
        onCameraReady: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            Log.d(TAG, "CameraManager: bindCamera started")
            val cameraProvider = this.cameraProvider ?: run {
                Log.e(TAG, "CameraManager: bindCamera - cameraProvider is null")
                return
            }

            Log.d(TAG, "CameraManager: Creating preview use case")
            val preview = androidx.camera.core.Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            Log.d(TAG, "CameraManager: Creating image analyzer")
            val imageAnalyzer = androidx.camera.core.ImageAnalysis.Builder()
                .setBackpressureStrategy(androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    handTracker?.let { tracker ->
                        Log.d(TAG, "CameraManager: Setting analyzer")
                        it.setAnalyzer(
                            androidx.core.content.ContextCompat.getMainExecutor(context),
                            HandAnalyzer(tracker)
                        )
                    } ?: Log.e(TAG, "CameraManager: handTracker is null, cannot set analyzer")
                }

            val cameraSelector = androidx.camera.core.CameraSelector.DEFAULT_FRONT_CAMERA
            Log.d(TAG, "CameraManager: Camera selector: FRONT")

            Log.d(TAG, "CameraManager: Unbinding all")
            cameraProvider.unbindAll()

            Log.d(TAG, "CameraManager: Binding to lifecycle")
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalyzer
            )

            isBound = true
            Log.d(TAG, "CameraManager: Camera bound successfully")

            onCameraReady()
            viewModel.setCameraReady(true)
            Log.d(TAG, "CameraManager: bindCamera completed successfully")

        } catch (e: Exception) {
            Log.e(TAG, "CameraManager: bindCamera failed", e)
            isBound = false
            onError("Failed to bind camera: ${e.message}")
        }
    }

    fun shutdown() {
        Log.d(TAG, "CameraManager: shutdown called")
        try {
            lifecycleOwner.lifecycle.removeObserver(this)
            Log.d(TAG, "CameraManager: Lifecycle observer removed")

            handTracker?.close()
            Log.d(TAG, "CameraManager: HandTracker closed")

            cameraProvider?.unbindAll()
            Log.d(TAG, "CameraManager: Camera unbound")

            previewView = null
            onCameraReadyCallback = null
            onErrorCallback = null
            isBound = false

            Log.d(TAG, "CameraManager: shutdown completed")
        } catch (e: Exception) {
            Log.e(TAG, "CameraManager: Error during shutdown", e)
        }
    }
}