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
        CalibrationPoint(100f, 50f),    // Top-left
        CalibrationPoint(900f, 50f),    // Top-right
        CalibrationPoint(900f, 400f),   // Bottom-right
        CalibrationPoint(100f, 400f)    // Bottom-left
    )
)
    val calibrationPoints: StateFlow<List<CalibrationPoint>> = _calibrationPoints.asStateFlow()

    // Finger tracking state
    private val _fingerPosition = MutableStateFlow<FingerPosition?>(null)
    val fingerPosition: StateFlow<FingerPosition?> = _fingerPosition.asStateFlow()

    // ADD multi-finger transformed positions
    private val _transformedFingerPositions = MutableStateFlow<Map<Int, PointF>>(emptyMap())
    val transformedFingerPositions: StateFlow<Map<Int, PointF>> = _transformedFingerPositions.asStateFlow()
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
    private val tapCooldown = 200L // 300ms cooldown between taps
    private val pressVelocityThreshold = 0.03f // Adjust based on testing

    // Camera state
    private val _isCameraReady = MutableStateFlow(false)
    val isCameraReady: StateFlow<Boolean> = _isCameraReady.asStateFlow()

    // Simulated homography matrix (in real implementation, this would be calculated with OpenCV)
    private var homographyMatrix: FloatArray? = null

    private data class FingerVelocity(val vx: Float, val vy: Float)
    private val fingerVelocities = mutableMapOf<Int, FingerVelocity>()

    private fun predictiveSmooth(fingerId: Int, newPos: PointF, timestamp: Long): PointF {
        val lastPos = smoothedPositions[fingerId]
        val lastTime = lastFingerPositions[fingerId]?.timestamp

        if (lastPos == null || lastTime == null) {
            return smoothPosition(fingerId, newPos)
        }

        // Calculate velocity
        val dt = (timestamp - lastTime) / 1_000_000_000.0f
        if (dt > 0 && dt < 0.1f) {  // Sanity check
            val vx = (newPos.x - lastPos.x) / dt
            val vy = (newPos.y - lastPos.y) / dt
            fingerVelocities[fingerId] = FingerVelocity(vx, vy)
        }

        return smoothPosition(fingerId, newPos)
    }
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

    // Add to class properties
    private val _fingerPositions = MutableStateFlow<Map<Int, FingerPosition>>(emptyMap())
    val fingerPositions: StateFlow<Map<Int, FingerPosition>> = _fingerPositions.asStateFlow()

    private val lastFingerPositions = mutableMapOf<Int, FingerPosition>()
    private val tapStates = mutableMapOf<Int, TapDetectionState>()
    private val lastTapTimes = mutableMapOf<Int, Long>()


    fun updateFingerPosition(x: Float, y: Float, z: Float, timestamp: Long, fingerId: Int) {
        val newPosition = FingerPosition(x, y, z, timestamp)

        val currentFingers = _fingerPositions.value.toMutableMap()
        currentFingers[fingerId] = newPosition
        _fingerPositions.value = currentFingers

        homographyMatrix?.let { matrix ->
            val rawTransformed = applySimulatedHomography(x, y, matrix)
            val smoothed = predictiveSmooth(fingerId, rawTransformed, timestamp)
            val currentTransformed = _transformedFingerPositions.value.toMutableMap()
            currentTransformed[fingerId] = smoothed
            _transformedFingerPositions.value = currentTransformed

            if (!tapStates.containsKey(fingerId)) {
                tapStates[fingerId] = TapDetectionState.Idle
                lastTapTimes[fingerId] = 0L
            }

            processTapDetection(newPosition, fingerId)
        }

        lastFingerPositions[fingerId] = newPosition
    }

    private fun processTapDetection(currentPosition: FingerPosition, handIndex: Int) {
    Log.d("TapDetection", "Hand $handIndex - Processing tap detection")

    val lastPos = lastFingerPositions[handIndex] ?: run {
        Log.d("TapDetection", "Hand $handIndex - No previous position")
        return
    }

    val currentState = tapStates[handIndex] ?: TapDetectionState.Idle
    val lastTap = lastTapTimes[handIndex] ?: 0L

    val timeDelta = (currentPosition.timestamp - lastPos.timestamp) / 1_000_000_000.0
    if (timeDelta <= 0 || timeDelta > 1.0) return

    val velocityZ = (currentPosition.z - lastPos.z) / timeDelta.toFloat()

    when (currentState) {
        TapDetectionState.Idle -> {
            if (velocityZ > pressVelocityThreshold &&
                currentPosition.timestamp - lastTap > tapCooldown * 1_000_000) {
                tapStates[handIndex] = TapDetectionState.Pressing
            }
        }
        TapDetectionState.Pressing -> {
            if (velocityZ < -pressVelocityThreshold) {
                registerTap(currentPosition,handIndex)
                tapStates[handIndex] = TapDetectionState.Idle
                lastTapTimes[handIndex] = currentPosition.timestamp
            }
        }
    }
    Log.d("TapDetection", "Hand $handIndex - velocityZ=$velocityZ, state=${tapStates[handIndex]}")

}

    private fun registerTap(position: FingerPosition, handIndex: Int) {
        Log.d("RegisterTap", "Tap registered at raw position: (${position.x}, ${position.y})")
        val transformedPos = _transformedFingerPositions.value[handIndex] ?: run {
            Log.d("RegisterTap", "Hand $handIndex - No transformed position")
            return
        }
        // Add boundary check - only accept taps in normalized 0-1 range
        if (transformedPos.x < 0f || transformedPos.x > 1f ||
            transformedPos.y < 0f || transformedPos.y > 1f) {
            Log.d("RegisterTap", "Tap OUTSIDE calibrated area: (${transformedPos.x}, ${transformedPos.y})")
            return
        }
        Log.d("RegisterTap", "Hand $handIndex INSIDE area: (${transformedPos.x}, ${transformedPos.y})")
        val tappedKey = findKeyAtPosition(transformedPos)
        Log.d("RegisterTap", "Hand $handIndex found key: ${tappedKey?.char}")

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
        // Store the calibration rectangle bounds for mapping
        val minX = points.minOf { it.x }
        val maxX = points.maxOf { it.x }
        val minY = points.minOf { it.y }
        val maxY = points.maxOf { it.y }

        val width = maxX - minX
        val height = maxY - minY

        Log.d("VirtualKeyboard", "ViewModel: Calibration bounds - x:$minX-$maxX, y:$minY-$maxY, size:${width}x$height")

        // Store as: [minX, minY, width, height]
        return floatArrayOf(minX, minY, width, height)
    }

    private fun applySimulatedHomography(x: Float, y: Float, matrix: FloatArray): PointF {
        val minX = matrix[0]
        val minY = matrix[1]
        val width = matrix[2]
        val height = matrix[3]

        // Add 15% margin buffer around calibration area
        val marginX = width * 0.15f
        val marginY = height * 0.15f

        val expandedMinX = minX - marginX
        val expandedMinY = minY - marginY
        val expandedWidth = width + (2 * marginX)
        val expandedHeight = height + (2 * marginY)

        // Map with expanded area but still normalize to 0-1
        val normalizedX = ((x - expandedMinX) / expandedWidth).coerceIn(0f, 1f)
        val normalizedY = ((y - expandedMinY) / expandedHeight).coerceIn(0f, 1f)

        Log.v("Homography", "Input($x,$y) -> Norm($normalizedX,$normalizedY)")

        return PointF(normalizedX, normalizedY)
    }

    // Exponential Moving Average filter
    private val smoothingFactor = 0.3f  // Lower = more smoothing, higher = more responsive
    private val smoothedPositions = mutableMapOf<Int, PointF>()

    private fun smoothPosition(fingerId: Int, newPos: PointF): PointF {
        val prevPos = smoothedPositions[fingerId]

        return if (prevPos == null) {
            smoothedPositions[fingerId] = newPos
            newPos
        } else {
            val smoothed = PointF(
                prevPos.x + smoothingFactor * (newPos.x - prevPos.x),
                prevPos.y + smoothingFactor * (newPos.y - prevPos.y)
            )
            smoothedPositions[fingerId] = smoothed
            smoothed
        }
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
