package com.example.virtualkeyboard.ui


import android.Manifest
import android.annotation.SuppressLint
import android.util.Log
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.virtualkeyboard.viewmodel.CalibrationPoint
import com.example.virtualkeyboard.viewmodel.CalibrationState
import com.example.virtualkeyboard.viewmodel.KeyboardKey
import com.example.virtualkeyboard.viewmodel.VirtualKeyboardViewModel
import com.example.virtualkeyboard.vision.CameraManager
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.geometry.Size
import com.example.virtualkeyboard.viewmodel.FingerPosition

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun CalibrationOverlay(
    calibrationPoints: List<CalibrationPoint>,
    fingerPositions: Map<Int, FingerPosition> = emptyMap(),  // NEW
    onPointDrag: (Int, Float, Float) -> Unit,
    onCalibrationComplete: () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val maxWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
        val maxHeightPx = with(LocalDensity.current) { maxHeight.toPx() }

        var draggedPointIndex by remember { mutableStateOf<Int?>(null) }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            // Find the closest point to start dragging
                            val closestIndex = calibrationPoints.indices.minByOrNull { index ->
                                val point = calibrationPoints[index]
                                val dx = point.x - offset.x
                                val dy = point.y - offset.y
                                dx * dx + dy * dy
                            }

                            // Only start dragging if we're close enough (within 50px)
                            closestIndex?.let { index ->
                                val point = calibrationPoints[index]
                                val distance = kotlin.math.sqrt(
                                    ((point.x - offset.x) * (point.x - offset.x) +
                                            (point.y - offset.y) * (point.y - offset.y)).toDouble()
                                ).toFloat()

                                if (distance < 100f) { // 100px touch radius
                                    draggedPointIndex = index
                                    onPointDrag(index, offset.x, offset.y)
                                }
                            }
                        },
                        onDrag = { change, _ ->
                            draggedPointIndex?.let { index ->
                                onPointDrag(index, change.position.x, change.position.y)
                            }
                        },
                        onDragEnd = {
                            draggedPointIndex = null
                        }
                    )
                }
        ) {
            // Draw calibration points
            calibrationPoints.forEachIndexed { index, point ->
                val isBeingDragged = draggedPointIndex == index
                val radius = if (isBeingDragged) 40f else 30f

                drawCircle(
                    color = if (point.isSet) Color.Green else Color.Red,
                    radius = radius,
                    center = Offset(point.x, point.y)
                )

                // Draw point labels
                drawCircle(
                    color = Color.White,
                    radius = 15f,
                    center = Offset(point.x, point.y)
                )

                // Draw point number
                drawCircle(
                    color = Color.Black,
                    radius = 8f,
                    center = Offset(point.x, point.y)
                )
            }

            // Draw connecting lines if all points are set
            if (calibrationPoints.all { it.isSet }) {
                val points = calibrationPoints.map { Offset(it.x, it.y) }
                for (i in points.indices) {
                    val start = points[i]
                    val end = points[(i + 1) % points.size]
                    drawLine(
                        color = Color.Blue,
                        start = start,
                        end = end,
                        strokeWidth = 5f
                    )
                }

                // Fill the calibrated area with semi-transparent overlay
                drawRect(
                    color = Color.Blue.copy(alpha = 0.1f),
                    topLeft = Offset(
                        minOf(points[0].x, points[1].x, points[2].x, points[3].x),
                        minOf(points[0].y, points[1].y, points[2].y, points[3].y)
                    ),
                    size = Size(
                        maxOf(points[0].x, points[1].x, points[2].x, points[3].x) -
                                minOf(points[0].x, points[1].x, points[2].x, points[3].x),
                        maxOf(points[0].y, points[1].y, points[2].y, points[3].y) -
                                minOf(points[0].y, points[1].y, points[2].y, points[3].y)
                    )
                )
            }
        }

        // Instructions and calibration button
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black.copy(alpha = 0.8f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Drag the 4 corner points to define your keyboard area",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Points set: ${calibrationPoints.count { it.isSet }}/4",
                        color = if (calibrationPoints.all { it.isSet }) Color.Green else Color.Yellow,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (calibrationPoints.all { it.isSet }) {
                Button(
                    onClick = onCalibrationComplete,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
                ) {
                    Text("Complete Calibration", fontSize = 16.sp)
                }
            } else {
                Button(
                    onClick = { /* Auto-set points for testing */
                        onPointDrag(0, 200f, 200f)
                        onPointDrag(1, 800f, 200f)
                        onPointDrag(2, 800f, 600f)
                        onPointDrag(3, 200f, 600f)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Auto-Set Points (Demo)", fontSize = 16.sp)
                }
            }
        }
    }
}
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun VirtualKeyboardDemo() {
    val viewModel: VirtualKeyboardViewModel = viewModel()
    val view = LocalView.current
    // Camera permission
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    var cameraManager by remember { mutableStateOf<CameraManager?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Collect states
    val calibrationState by viewModel.calibrationState.collectAsState()
//    val fingerPosition by viewModel.transformedFingerPosition.collectAsState()
    val transformedFingerPositions by viewModel.transformedFingerPositions.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val isCameraReady by viewModel.isCameraReady.collectAsState()


    // Handle tap events for haptic feedback
    LaunchedEffect(Unit) {
        viewModel.tapEvent.collect { key ->
            key?.let {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
        }
    }
    LaunchedEffect(calibrationState) {
        if (calibrationState == CalibrationState.InProgress && cameraManager == null) {
            // Camera will be set up
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Virtual Keyboard Demo",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Input text display
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Typed Text:",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = inputText.ifEmpty { "Start typing..." },
                    fontSize = 18.sp,
                    color = Color.Black,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Camera permission and setup
        when {
            !cameraPermissionState.status.isGranted -> {
                CameraPermissionScreen(
                    onRequestPermission = { cameraPermissionState.launchPermissionRequest() }
                )
            }

            calibrationState == CalibrationState.NotStarted -> {
                CalibrationStartScreen(
                    onStartCalibration = { viewModel.startCalibration() }
                )
            }

            calibrationState == CalibrationState.InProgress ||
                    calibrationState == CalibrationState.Completed -> {
                CalibrationScreen(
                    viewModel = viewModel,
                    onCameraManagerCreated = { manager -> cameraManager = manager },
                    showCalibrationUI = calibrationState == CalibrationState.InProgress
                )
            }
        }

        // Error display
        errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f)),
                border = BorderStroke(1.dp, Color.Red)
            ) {
                Text(
                    text = error,
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        // Action buttons
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.clearText() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Clear Text")
            }

            if (calibrationState == CalibrationState.Completed) {
                Button(
                    onClick = {
                        viewModel.resetCalibration()
                        cameraManager?.shutdown()
                        cameraManager = null
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Recalibrate")
                }
            }
        }
    }
}

@Composable
fun CameraPermissionScreen(
    onRequestPermission: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Camera Permission Required",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "This app needs camera access to track your finger movements for the virtual keyboard.",
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRequestPermission,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Grant Camera Permission")
            }
        }
    }
}

@Composable
fun CalibrationStartScreen(
    onStartCalibration: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Setup Virtual Keyboard",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Before using the virtual keyboard, you need to calibrate it by defining the keyboard area on your desk.",
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Instructions:\n• Place your phone in a stable position\n• Point camera towards your desk\n• You'll drag 4 corners to define the keyboard area",
                fontSize = 14.sp,
                textAlign = TextAlign.Start,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onStartCalibration,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Start Calibration")
            }
        }
    }
}

// In VirtualKeyboardDemo.kt - update CalibrationScreen

@Composable
fun CalibrationScreen(
    viewModel: VirtualKeyboardViewModel,
    onCameraManagerCreated: (CameraManager) -> Unit,
    showCalibrationUI: Boolean = true
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val calibrationPoints by viewModel.calibrationPoints.collectAsState()

    // NEW: Track current finger positions during calibration
    val fingerPositions by viewModel.fingerPositions.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                androidx.camera.view.PreviewView(ctx).also { preview ->
                    val cameraManager = CameraManager(ctx, lifecycleOwner, viewModel)
                    cameraManager.setupCamera(preview, {}, {})
                    onCameraManagerCreated(cameraManager)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (showCalibrationUI) {
            CalibrationOverlay(
                calibrationPoints = calibrationPoints,
                fingerPositions = fingerPositions,  // NEW: Pass finger positions
                onPointDrag = { index, x, y ->
                    // NEW: Get Z value from current finger position
                    val z = fingerPositions.values.firstOrNull()?.z ?: 0f
                    viewModel.updateCalibrationPoint(index, x, y, z)
                },
                onCalibrationComplete = {
                    viewModel.completeCalibration()
                }
            )
        } else {
            val transformedFingerPositions by viewModel.transformedFingerPositions.collectAsState()
            val keyboardLayout by viewModel.keyboardLayout.collectAsState()

            Canvas(modifier = Modifier.fillMaxSize()) {
                drawKeyboard(keyboardLayout, transformedFingerPositions)
            }
        }
    }
}



private fun DrawScope.drawKeyboard(
    keyboardLayout: List<List<KeyboardKey>>,
//    fingerPosition: android.graphics.PointF?
    fingerPositions: Map<Int, android.graphics.PointF>

) {
    val canvasWidth = size.width
    val canvasHeight = size.height

    // Draw keyboard keys
    keyboardLayout.forEach { row ->
        row.forEach { key ->
            val left = key.x * canvasWidth
            val top = key.y * canvasHeight
            val right = left + (key.width * canvasWidth)
            val bottom = top + (key.height * canvasHeight)

            // Key background
            drawRoundRect(
                color = Color.LightGray,
                topLeft = Offset(left, top),
                size = androidx.compose.ui.geometry.Size(right - left, bottom - top),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f)
            )

            // Key border
            drawRoundRect(
                color = Color.Gray,
                topLeft = Offset(left, top),
                size = androidx.compose.ui.geometry.Size(right - left, bottom - top),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
            )
        }
    }

    fingerPositions.forEach { (handIndex, pos) ->
        val x = pos.x * canvasWidth
        val y = pos.y * canvasHeight

        val isInBounds = pos.x in 0f..1f && pos.y in 0f..1f
        val color = if (handIndex == 0) Color.Red else Color.Blue  // Different colors per hand

        Log.d("KeyboardDraw", "Hand $handIndex at canvas: ($x, $y), inBounds=$isInBounds")

        drawCircle(
            color = if (isInBounds) color else Color.Yellow,
            radius = 25f,
            center = Offset(x, y)
        )

        drawCircle(
            color = Color.White,
            radius = 18f,
            center = Offset(x, y)
        )

        // Draw hand index number
        drawCircle(
            color = Color.Black,
            radius = 10f,
            center = Offset(x, y)
        )
    }
}