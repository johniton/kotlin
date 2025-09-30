package com.example.virtualkeyboard.viewmodel


import android.graphics.PointF
import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.*
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

data class CalibrationPoint(
    val x: Float,
    val y: Float,
    val isSet: Boolean = false
)

data class FingerPosition(
    val x: Float,
    val y: Float,
    val z: Float,
    val timestamp: Long
)

data class KeyboardKey(
    val char: String,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float
)

sealed class CalibrationState {
    object NotStarted : CalibrationState()
    object InProgress : CalibrationState()
    object Completed : CalibrationState()
}

sealed class TapDetectionState {
    object Idle : TapDetectionState()
    object Pressing : TapDetectionState()
}

class VirtualKeyboardViewModel : ViewModel() {

    // Calibration state
    private val _calibrationState = MutableStateFlow<CalibrationState>(CalibrationState.NotStarted)
    val calibrationState: StateFlow<CalibrationState> = _calibrationState.asStateFlow()

    private val _calibrationPoints = MutableStateFlow(
        listOf(
            CalibrationPoint(100f, 100f),   // Top-left
            CalibrationPoint(500f, 100f),   // Top-right
            CalibrationPoint(500f, 400f),   // Bottom-right
            CalibrationPoint(100f, 400f)    // Bottom-left
        )
    )
    val calibrationPoints: StateFlow<List<CalibrationPoint>> = _calibrationPoints.asStateFlow()

    // Finger tracking state
    private val _fingerPosition = MutableStateFlow<FingerPosition?>(null)
    val fingerPosition: StateFlow<FingerPosition?> = _fingerPosition.asStateFlow()

    private val _transformedFingerPosition = MutableStateFlow<PointF?>(null)
    val transformedFingerPosition: StateFlow<PointF?> = _transformedFingerPosition.asStateFlow()

    // Tap detection
    private val _tapEvent = MutableSharedFlow<KeyboardKey?>()
    val tapEvent: SharedFlow<KeyboardKey?> = _tapEvent.asSharedFlow()

    // Text input
    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    // Keyboard layout
    private val _keyboardLayout = MutableStateFlow(createQwertyLayout())
    val keyboardLayout: StateFlow<List<List<KeyboardKey>>> = _keyboardLayout.asStateFlow()

    // Tap detection state machine
    private var tapDetectionState: TapDetectionState = TapDetectionState.Idle
    private var lastFingerPosition: FingerPosition? = null
    private var lastTapTime = 0L
    private val tapCooldown = 500L // 300ms cooldown between taps
    private val pressVelocityThreshold = 0.05f // Adjust based on testing

    // Camera state
    private val _isCameraReady = MutableStateFlow(false)
    val isCameraReady: StateFlow<Boolean> = _isCameraReady.asStateFlow()

    // Simulated homography matrix (in real implementation, this would be calculated with OpenCV)
    private var homographyMatrix: FloatArray? = null

    fun startCalibration() {
        _calibrationState.value = CalibrationState.InProgress
    }

    fun updateCalibrationPoint(index: Int, x: Float, y: Float) {
        val currentPoints = _calibrationPoints.value.toMutableList()
        if (index in 0..3) {
            currentPoints[index] = CalibrationPoint(x, y, true) // Mark as set!
            _calibrationPoints.value = currentPoints
            println("Updated calibration point $index to ($x, $y)")

            // Auto-complete calibration when all points are set
            if (currentPoints.all { it.isSet }) {
                Log.d("ViewModel", "Auto-completing calibration")
                completeCalibration()
            }
        }
    }
    fun completeCalibration() {
        val points = _calibrationPoints.value
        if (points.all { it.isSet }) {
            // In real implementation, calculate homography matrix here using OpenCV
            homographyMatrix = calculateSimulatedHomography(points)
            Log.d("ViewModel", "Calibration complete, matrix: ${homographyMatrix?.contentToString()}") // ADD THIS
            _calibrationState.value = CalibrationState.Completed
        }
    }

    fun setCameraReady(ready: Boolean) {
        _isCameraReady.value = ready
    }

    fun updateFingerPosition(x: Float, y: Float, z: Float, timestamp: Long) {
        Log.d("ViewModel", "Finger update: x=$x, y=$y, z=$z")
        val newPosition = FingerPosition(x, y, z, timestamp)
        _fingerPosition.value = newPosition
        println("Finger position updated")

        // Apply homography transformation (simulated for demo)
        homographyMatrix?.let { matrix ->
            Log.d("ViewModel", "Homography EXISTS, processing tap")
            val transformed = applySimulatedHomography(x, y, matrix)
            _transformedFingerPosition.value = transformed
            println("Transformed position: x=${transformed.x}, y=${transformed.y}")

            // Process tap detection
            processTapDetection(newPosition)
        }?: Log.d("ViewModel", "NO HOMOGRAPHY - skipping tap detection")

        lastFingerPosition = newPosition
    }

    private fun processTapDetection(currentPosition: FingerPosition) {
        println("=== Tap Detection ===")
        Log.d("tap detection","=== Tap Detection ===")
        val lastPos = lastFingerPosition ?: run {
            println("No last position available")
            return
        }
        val currentTime = currentPosition.timestamp
        val timeDelta = (currentPosition.timestamp - lastPos.timestamp) / 1_000_000_000.0 // ns to seconds

        if (timeDelta <= 0 || timeDelta > 1.0) { // Sanity check
            Log.d("ViewModel", "Invalid time delta: $timeDelta")
            return
        }

        val velocityZ = (currentPosition.z - lastPos.z) / timeDelta.toFloat()
        println("Z velocity: $velocityZ, threshold: $pressVelocityThreshold")
        println("Current tap state: $tapDetectionState")

        when (tapDetectionState) {
            TapDetectionState.Idle -> {
                // Detect forward press (Z increasing, becoming less negative)
                if (velocityZ > pressVelocityThreshold &&
                    currentTime - lastTapTime > tapCooldown * 1_000_000) { // Convert ms to ns
                    Log.d("ViewModel", "*** PRESSING STATE ENTERED ***")
                    tapDetectionState = TapDetectionState.Pressing
                }
            }
            TapDetectionState.Pressing -> {
                // Detect retraction (Z decreasing, becoming more negative)
                if (velocityZ < -pressVelocityThreshold) {
                    Log.d("ViewModel", "*** TAP DETECTED! ***")
                    registerTap(currentPosition)
                    tapDetectionState = TapDetectionState.Idle
                    lastTapTime = currentTime
                }
            }
        }
    }

    private fun registerTap(position: FingerPosition) {
        println("=== Register Tap ===")
        val transformedPos = _transformedFingerPosition.value ?: run {
            println("No transformed position available")
            return
        }
        println("Looking for key at position: x=${transformedPos.x}, y=${transformedPos.y}")
        val tappedKey = findKeyAtPosition(transformedPos)
        println("Key found: ${tappedKey?.char ?: "NONE"}")

        viewModelScope.launch {
            _tapEvent.emit(tappedKey)
            println("Tap event emitted")

            tappedKey?.let { key ->
                println("Processing key: ${key.char}")
                when (key.char) {
                    "BACKSPACE" -> {
                        if (_inputText.value.isNotEmpty()) {
                            _inputText.value = _inputText.value.dropLast(1)
                        }
                    }
                    "SPACE" -> {
                        _inputText.value += " "
                    }
                    else -> {
                        _inputText.value += key.char
                    }
                }
            }
        }
    }

    private fun findKeyAtPosition(position: PointF): KeyboardKey? {
        val layout = _keyboardLayout.value
        for (row in layout) {
            for (key in row) {
                if (position.x >= key.x && position.x <= key.x + key.width &&
                    position.y >= key.y && position.y <= key.y + key.height) {
                    return key
                }
            }
        }
        return null
    }

    private fun calculateSimulatedHomography(points: List<CalibrationPoint>): FloatArray {
        // This is a simplified simulation. In real implementation, use OpenCV's getPerspectiveTransform
        // For demo purposes, we'll create a simple transformation
        return floatArrayOf(
            1.0f, 0.0f, points[0].x,
            0.0f, 1.0f, points[0].y,
            0.0f, 0.0f, 1.0f
        )
    }

    private fun applySimulatedHomography(x: Float, y: Float, matrix: FloatArray): PointF {
        // Simplified transformation for demo
        // In real implementation, use OpenCV's perspectiveTransform
        val normalizedX = (x - matrix[2]) / 1000f // Normalize to 0-1 range
        val normalizedY = (y - matrix[5]) / 600f  // Normalize to 0-1 range

        return PointF(
            normalizedX.coerceIn(0f, 1f),
            normalizedY.coerceIn(0f, 1f)
        )
    }

    private fun createQwertyLayout(): List<List<KeyboardKey>> {
        val keyWidth = 0.08f
        val keyHeight = 0.15f
        val keySpacing = 0.01f

        return listOf(
            // Row 1
            listOf(
                KeyboardKey("Q", 0.05f, 0.1f, keyWidth, keyHeight),
                KeyboardKey("W", 0.15f, 0.1f, keyWidth, keyHeight),
                KeyboardKey("E", 0.25f, 0.1f, keyWidth, keyHeight),
                KeyboardKey("R", 0.35f, 0.1f, keyWidth, keyHeight),
                KeyboardKey("T", 0.45f, 0.1f, keyWidth, keyHeight),
                KeyboardKey("Y", 0.55f, 0.1f, keyWidth, keyHeight),
                KeyboardKey("U", 0.65f, 0.1f, keyWidth, keyHeight),
                KeyboardKey("I", 0.75f, 0.1f, keyWidth, keyHeight),
                KeyboardKey("O", 0.85f, 0.1f, keyWidth, keyHeight),
                KeyboardKey("P", 0.95f, 0.1f, keyWidth, keyHeight)
            ),
            // Row 2
            listOf(
                KeyboardKey("A", 0.08f, 0.35f, keyWidth, keyHeight),
                KeyboardKey("S", 0.18f, 0.35f, keyWidth, keyHeight),
                KeyboardKey("D", 0.28f, 0.35f, keyWidth, keyHeight),
                KeyboardKey("F", 0.38f, 0.35f, keyWidth, keyHeight),
                KeyboardKey("G", 0.48f, 0.35f, keyWidth, keyHeight),
                KeyboardKey("H", 0.58f, 0.35f, keyWidth, keyHeight),
                KeyboardKey("J", 0.68f, 0.35f, keyWidth, keyHeight),
                KeyboardKey("K", 0.78f, 0.35f, keyWidth, keyHeight),
                KeyboardKey("L", 0.88f, 0.35f, keyWidth, keyHeight)
            ),
            // Row 3
            listOf(
                KeyboardKey("Z", 0.12f, 0.6f, keyWidth, keyHeight),
                KeyboardKey("X", 0.22f, 0.6f, keyWidth, keyHeight),
                KeyboardKey("C", 0.32f, 0.6f, keyWidth, keyHeight),
                KeyboardKey("V", 0.42f, 0.6f, keyWidth, keyHeight),
                KeyboardKey("B", 0.52f, 0.6f, keyWidth, keyHeight),
                KeyboardKey("N", 0.62f, 0.6f, keyWidth, keyHeight),
                KeyboardKey("M", 0.72f, 0.6f, keyWidth, keyHeight),
                KeyboardKey("BACKSPACE", 0.82f, 0.6f, keyWidth * 1.5f, keyHeight)
            ),
            // Row 4
            listOf(
                KeyboardKey("SPACE", 0.25f, 0.85f, keyWidth * 5f, keyHeight)
            )
        )
    }

    fun clearText() {
        _inputText.value = ""
    }

    fun resetCalibration() {
        _calibrationState.value = CalibrationState.NotStarted
        _calibrationPoints.value = listOf(
            CalibrationPoint(0f, 0f),
            CalibrationPoint(0f, 0f),
            CalibrationPoint(0f, 0f),
            CalibrationPoint(0f, 0f)
        )
        homographyMatrix = null
    }
}
