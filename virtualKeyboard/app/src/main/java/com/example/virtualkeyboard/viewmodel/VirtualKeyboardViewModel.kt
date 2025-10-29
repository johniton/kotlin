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

    private var deskZPlane: Float? = null
    // Using a list with placeholder values for Z coordinates
    private val calibrationZValues = mutableListOf(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE)

private val _calibrationPoints = MutableStateFlow(
    listOf(
        CalibrationPoint(0f, 0f), // Default, isSet = false
        CalibrationPoint(0f, 0f), // Default, isSet = false
        CalibrationPoint(0f, 0f), // Default, isSet = false
        CalibrationPoint(0f, 0f)  // Default, isSet = false
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
    private val tapCooldown = 500L // 300ms cooldown between taps
    private val pressVelocityThreshold = 0.05f // Adjust based on testing

    // Camera state
    private val _isCameraReady = MutableStateFlow(false)
    val isCameraReady: StateFlow<Boolean> = _isCameraReady.asStateFlow()

    // Simulated homography matrix (in real implementation, this would be calculated with OpenCV)
    private var homographyMatrix: FloatArray? = null

    fun startCalibration() {
        _calibrationState.value = CalibrationState.InProgress
        calibrationZValues.fill(Float.MIN_VALUE) // Reset Z values to placeholders
        Log.d("ViewModel", "Calibration started. Z values reset: $calibrationZValues")
    }

    // Modify updateCalibrationPoint to capture Z values into the list
    fun updateCalibrationPoint(index: Int, x: Float, y: Float, z: Float = 0f) {
        val currentPoints = _calibrationPoints.value.toMutableList()
        if (index in 0..3) {
            currentPoints[index] = CalibrationPoint(x, y, true)
            _calibrationPoints.value = currentPoints

            // Store Z value in the list at the specified index
            calibrationZValues[index] = z
            Log.d("ViewModel", "Calibration point $index: ($x, $y, z=$z), updated calibrationZValues: $calibrationZValues")

            if (currentPoints.all { it.isSet }) {
                completeCalibration()
            }
        }
    }

    // Modify completeCalibration to use the list and check for placeholders
    fun completeCalibration() {
        Log.d("ViewModel", "completeCalibration called.")
        val points = _calibrationPoints.value
        if (points.all { it.isSet }) {
            Log.d("ViewModel", "All calibration points are set. Proceeding with homography and Z-plane.")
            homographyMatrix = calculateSimulatedHomography(points)

            // Calculate average desk Z-plane if all Z values have been updated
            if (!calibrationZValues.any { it == Float.MIN_VALUE }) {
                deskZPlane = calibrationZValues.average().toFloat()
                Log.d("ViewModel", "Desk Z-plane set to: $deskZPlane from values: $calibrationZValues")
            } else {
                deskZPlane = null // Ensure it's null if not all Z values were set
                Log.w("ViewModel", "Cannot set desk Z-plane: One or more Z values were not updated from initial. Values: $calibrationZValues")
            }

            _calibrationState.value = CalibrationState.Completed
        } else {
            Log.w("ViewModel", "completeCalibration: Not all points are set. Points: $points")
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


    fun updateFingerPosition(x: Float, y: Float, z: Float, timestamp: Long, handIndex: Int) {
        Log.d("FingerTracking", "Hand $handIndex update: x=$x, y=$y, z=$z")
        val newPosition = FingerPosition(x, y, z, timestamp)

        val currentFingers = _fingerPositions.value.toMutableMap()
        currentFingers[handIndex] = newPosition
        _fingerPositions.value = currentFingers

        Log.d("FingerTracking", "Total hands tracked: ${currentFingers.size}")

        homographyMatrix?.let { matrix ->
            val transformed = applySimulatedHomography(x, y, matrix)
            // Store per-hand transformed position
            val currentTransformed = _transformedFingerPositions.value.toMutableMap()
            currentTransformed[handIndex] = transformed
            _transformedFingerPositions.value = currentTransformed
            Log.d("FingerTracking", "Hand $handIndex transformed: (${transformed.x}, ${transformed.y})")

            // Initialize tap state for new finger
            if (!tapStates.containsKey(handIndex)) {
                tapStates[handIndex] = TapDetectionState.Idle
                lastTapTimes[handIndex] = 0L
            }

            processTapDetection(newPosition, handIndex)
        }

        lastFingerPositions[handIndex] = newPosition
    }

// Replace processTapDetection function in VirtualKeyboardViewModel.kt
private fun processTapDetection(currentPosition: FingerPosition, handIndex: Int) {
    val lastPos = lastFingerPositions[handIndex] ?: return
    Log.d("TapDebug", "Hand $handIndex: processTapDetection called. currentZ=${currentPosition.z}, lastZ=${lastPos.z}, timestampDelta=${currentPosition.timestamp - lastPos.timestamp}")

    val timeDelta = (currentPosition.timestamp - lastPos.timestamp) / 1_000_000_000.0
    if (timeDelta <= 0 || timeDelta > 0.1) { // Max 100ms between updates for velocity calc
        Log.d("TapDebug", "Hand $handIndex: timeDelta out of range: $timeDelta")
        return
    }

    val velocityZ = (currentPosition.z - lastPos.z) / timeDelta.toFloat()

    val currentState = tapStates[handIndex] ?: TapDetectionState.Idle
    val lastTap = lastTapTimes[handIndex] ?: 0L

    // Use desk plane if available
    val nearDesk = deskZPlane?.let { plane ->
        kotlin.math.abs(currentPosition.z - plane) < 0.03f  // Within 3cm
    } ?: false
    Log.d("TapDebug", "Hand $handIndex: nearDesk=$nearDesk (currentZ=${currentPosition.z}, deskZ=${deskZPlane ?: "null"})")


    when (currentState) {
        TapDetectionState.Idle -> {
            val cooldownPassed = currentPosition.timestamp - lastTap > tapCooldown * 1_000_000
            Log.d("TapDebug", "Hand $handIndex: Idle state. nearDesk=$nearDesk, velocityZ=$velocityZ, cooldownPassed=$cooldownPassed (tapCooldown=${tapCooldown * 1_000_000}, lastTap=$lastTap, currentTS=${currentPosition.timestamp})")
            if ((nearDesk || velocityZ > 0.02f) && cooldownPassed) {
                Log.d("TapDebug", "Hand $handIndex: Idle -> Pressing")
                tapStates[handIndex] = TapDetectionState.Pressing
            }
        }
        TapDetectionState.Pressing -> {
            Log.d("TapDebug", "Hand $handIndex: Pressing state. nearDesk=$nearDesk, velocityZ=$velocityZ")
            if (nearDesk && velocityZ < -0.01f) {
                 Log.d("TapDebug", "Hand $handIndex: Pressing -> Idle (TAP REGISTERED)")
                registerTap(currentPosition, handIndex)
                tapStates[handIndex] = TapDetectionState.Idle
                lastTapTimes[handIndex] = currentPosition.timestamp
            }
        }
    }
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
        // Matrix format: [minX, minY, width, height]
        val minX = matrix[0]
        val minY = matrix[1]
        val width = matrix[2]
        val height = matrix[3]

        // Map camera coordinates to normalized 0-1 range within calibration area
        val normalizedX = ((x - minX) / width).coerceIn(0f, 1f)
        val normalizedY = ((y - minY) / height).coerceIn(0f, 1f)

        Log.d("VirtualKeyboard", "ViewModel: Homography transform - input($x,$y) -> normalized($normalizedX,$normalizedY)")

        return PointF(normalizedX, normalizedY)
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
            CalibrationPoint(0f, 0f), // Default, isSet = false
            CalibrationPoint(0f, 0f), // Default, isSet = false
            CalibrationPoint(0f, 0f), // Default, isSet = false
            CalibrationPoint(0f, 0f)  // Default, isSet = false
        )
        homographyMatrix = null
        calibrationZValues.fill(Float.MIN_VALUE) // Reset Z values to placeholders
        deskZPlane = null
        Log.d("ViewModel", "Calibration reset.")
    }
}
