package com.example.virtualkeyboard.vision

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.ImageFormat.*
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Log
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Camera analyzer for CameraX integration
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.virtualkeyboard.viewmodel.VirtualKeyboardViewModel
import com.google.mediapipe.framework.image.BitmapImageBuilder


class MediaPipeHandTracker(
    private val context: Context,
    private val onHandLandmarks: (x: Float, y: Float, z: Float, timestamp: Long) -> Unit,
    private val onError: (String) -> Unit
) {
    private var handLandmarker: HandLandmarker? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    fun initialize() {

        try {
            Log.d("HandTracker", "Initializing MediaPipe hand tracker...")
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath("hand_landmarker.task") // You'll need to add this to assets
                .build()

            val options = HandLandmarker.HandLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setNumHands(1)
                .setMinHandDetectionConfidence(0.5f)
                .setMinTrackingConfidence(0.5f)
                .setMinHandPresenceConfidence(0.5f)
                .setResultListener(::onHandLandmarkerResult)
                .setErrorListener(::onHandLandmarkerError)
                .build()

            handLandmarker = HandLandmarker.createFromOptions(context, options)
            Log.d("HandTracker", "MediaPipe initialized successfully")
        } catch (e: Exception) {
            onError("Failed to initialize MediaPipe: ${e.message}")
        }
    }

    fun detectAsync(mpImage: MPImage, timestamp: Long) {
        handLandmarker?.detectAsync(mpImage, timestamp)
    }
    private fun onHandLandmarkerResult(
        result: HandLandmarkerResult,
        input: MPImage
    ) {
        coroutineScope.launch {
            if (result.landmarks().isNotEmpty()) {
                val landmarks = result.landmarks()[0]

                if (landmarks.size > 8) {
                    val indexFingerTip = landmarks[8]

                    val imageWidth = input.width
                    val imageHeight = input.height

                    val pixelX = indexFingerTip.x() * imageWidth
                    val pixelY = indexFingerTip.y() * imageHeight
                    val depthZ = indexFingerTip.z()

                    // THIS IS CRITICAL - actually call the callback
                    onHandLandmarks(pixelX, pixelY, depthZ, System.nanoTime())
                }
            }
        }
    }
//    private fun onHandLandmarkerResult(
//        result: HandLandmarkerResult,
//        input: MPImage
//    ) {
//        Log.d("HandTracker", "=== MediaPipe Result ===")
//        Log.d("HandTracker", "Hands detected: ${result.landmarks().size}")
//        println("=== MediaPipe Result ===")
//        println("Hands detected: ${result.landmarks().size}")
////        coroutineScope.launch {
////            if (result.landmarks().isNotEmpty()) {
////                val landmarks = result.landmarks()[0]
////                println("Landmarks count: ${landmarks.size}")
////
////                // Get index finger tip (landmark #8)
////                if (landmarks.size > 8) {
////                    val indexFingerTip = landmarks[8]
////
////                    // MediaPipe provides normalized coordinates (0.0 to 1.0)
////                    // Convert to pixel coordinates
////                    val imageWidth = input.width
////                    val imageHeight = input.height
////
////                    val pixelX = indexFingerTip.x() * imageWidth
////                    val pixelY = indexFingerTip.y() * imageHeight
////                    val depthZ = indexFingerTip.z() // Relative depth from wrist
////
////                    println("Raw finger position: x=$pixelX, y=$pixelY, z=$depthZ")
////                    onHandLandmarks(pixelX, pixelY, depthZ, System.nanoTime())
////                }
////            }
////        }
//        coroutineScope.launch {
//            if (result.landmarks().isNotEmpty()) {
//                val landmarks = result.landmarks()[0]
//                Log.d("HandTracker", "Landmarks count: ${landmarks.size}")
//
//                // Debug: print index fingertip coordinates
//                if (landmarks.size > 8) {
//                    val indexFingerTip = landmarks[8]
//
//                    val imageWidth = input.width
//                    val imageHeight = input.height
//
//                    val pixelX = indexFingerTip.x() * imageWidth
//                    val pixelY = indexFingerTip.y() * imageHeight
//                    val depthZ = indexFingerTip.z()
//
//                    Log.d("HandTracker", "Fingertip coords: x=$pixelX, y=$pixelY, z=$depthZ")
//
//                    println("=== Fingertip coords: x=$pixelX, y=$pixelY, z=$depthZ")
//                }
//            } else {
//                Log.d("HandTracker", "No hand detected in this frame")
//            }
//        }
//    }

    private fun onHandLandmarkerError(error: RuntimeException) {
        onError("MediaPipe Error: ${error.message}")
    }

    fun close() {
        handLandmarker?.close()
    }
}


class HandAnalyzer(
    private val handTracker: MediaPipeHandTracker
) : ImageAnalysis.Analyzer {

override fun analyze(imageProxy: ImageProxy) {
    Log.d("HandTracker", "Analyzer called, format=${imageProxy.format}")
    val bitmap = imageProxy.toBitmap() // <-- built-in extension
    val mpImage = BitmapImageBuilder(bitmap).build()
//    handTracker.detectAsync(mpImage, imageProxy.imageInfo.timestamp)
    handTracker.detectAsync(mpImage, imageProxy.imageInfo.timestamp)
    imageProxy.close()
}


    private fun yuvToRgb(imageProxy: ImageProxy): Bitmap {
        val yBuffer = imageProxy.planes[0].buffer
        val uBuffer = imageProxy.planes[1].buffer
        val vBuffer = imageProxy.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, NV21, imageProxy.width, imageProxy.height, null)
        val out = java.io.ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, imageProxy.width, imageProxy.height), 100, out)
        val imageBytes = out.toByteArray()

        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    private fun imageProxyToBitmap(imageProxy: ImageProxy): android.graphics.Bitmap {
        val buffer = imageProxy.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)

        // Convert YUV_420_888 to RGB bitmap
        // This is a simplified conversion - in production, use more robust conversion
        return android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            ?: createDummyBitmap()
    }

    private fun createDummyBitmap(): android.graphics.Bitmap {
        // Fallback bitmap in case conversion fails
//        return android.graphics.Bitmap.createBitmap(640, 480, android.graphics.Bitmap.Config.RGB_565)
        return android.graphics.Bitmap.createBitmap(640, 480, android.graphics.Bitmap.Config.ARGB_8888)
    }
}

// Camera manager class
class CameraManager(
    private val context: Context,
    private val lifecycleOwner: androidx.lifecycle.LifecycleOwner,
//    private val viewModel: com.demo.virtualkeyboard.viewmodel.VirtualKeyboardViewModel
    private val viewModel: VirtualKeyboardViewModel
) {
    private var cameraProvider: androidx.camera.lifecycle.ProcessCameraProvider? = null
    private var handTracker: MediaPipeHandTracker? = null

    fun setupCamera(
        previewView: androidx.camera.view.PreviewView,
        onCameraReady: () -> Unit,
        onError: (String) -> Unit
    ) {
        // Initialize MediaPipe hand tracker
        handTracker = MediaPipeHandTracker(
            context = context,
            onHandLandmarks = { x, y, z, timestamp ->
                viewModel.updateFingerPosition(x, y, z, timestamp)
            },
            onError = onError
        )
//        handTracker?.initialize()

        Thread {
            handTracker?.initialize()
        }.start()

        val cameraProviderFuture = androidx.camera.lifecycle.ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                bindCamera(previewView, onCameraReady, onError)
            } catch (e: Exception) {
                onError("Camera initialization failed: ${e.message}")
            }
        }, androidx.core.content.ContextCompat.getMainExecutor(context))
    }

    private fun bindCamera(
        previewView: androidx.camera.view.PreviewView,
        onCameraReady: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val cameraProvider = this.cameraProvider ?: return

            // Preview use case
            val preview = androidx.camera.core.Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            // Image analysis use case
            val imageAnalyzer = androidx.camera.core.ImageAnalysis.Builder()
                .setBackpressureStrategy(androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    handTracker?.let { tracker ->
                        it.setAnalyzer(
                            androidx.core.content.ContextCompat.getMainExecutor(context),
                            HandAnalyzer(tracker)
                        )
                    }
                }

            // Select camera (front camera preferred for this use case)
            val cameraSelector = androidx.camera.core.CameraSelector.DEFAULT_FRONT_CAMERA


            // Bind use cases to lifecycle
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalyzer
            )

            onCameraReady()
            viewModel.setCameraReady(true)

        } catch (e: Exception) {
            onError("Failed to bind camera: ${e.message}")
        }
    }

    fun shutdown() {
        handTracker?.close()
        cameraProvider?.unbindAll()
    }
}

