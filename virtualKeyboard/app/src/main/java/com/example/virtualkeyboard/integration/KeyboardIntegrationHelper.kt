package com.example.virtualkeyboard.integration

// KeyboardIntegrationHelper.kt

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.virtualkeyboard.viewmodel.VirtualKeyboardViewModel

/**
 * This is how you can integrate the virtual keyboard into your existing app
 */
class VirtualKeyboardIntegration {

    // Call this from your existing app to get typed text
    @Composable
    fun IntegrateVirtualKeyboard(
        onTextInput: (String) -> Unit,
        onKeyPress: (String) -> Unit = {}
    ): VirtualKeyboardController {
        val viewModel: VirtualKeyboardViewModel = viewModel()
        val inputText by viewModel.inputText.collectAsState()
        val tapEvent by viewModel.tapEvent.collectAsState(initial = null)

        // Forward text changes to your app
        androidx.compose.runtime.LaunchedEffect(inputText) {
            onTextInput(inputText)
        }

        // Forward individual key presses
        androidx.compose.runtime.LaunchedEffect(tapEvent) {
            tapEvent?.let { key ->
                onKeyPress(key.char)
            }
        }

        return VirtualKeyboardController(viewModel)
    }
}

class VirtualKeyboardController(
    private val viewModel: VirtualKeyboardViewModel
) {
    fun clearText() {
        viewModel.clearText()
    }

    fun resetCalibration() {
        viewModel.resetCalibration()
    }

    fun getCurrentText(): String {
        return viewModel.inputText.value
    }
}

// Usage example in your existing app:
/*
@Composable
fun YourExistingScreen() {
    val keyboardIntegration = VirtualKeyboardIntegration()
    var appText by remember { mutableStateOf("") }

    val keyboardController = keyboardIntegration.IntegrateVirtualKeyboard(
        onTextInput = { newText ->
            appText = newText
            // Update your app's text fields
        },
        onKeyPress = { key ->
            // Handle individual key presses
            when (key) {
                "ENTER" -> { /* Handle enter */ }
                "BACKSPACE" -> { /* Handle backspace */ }
                else -> { /* Handle regular character */ }
            }
        }
    )

    // Your existing UI
    Column {
        TextField(
            value = appText,
            onValueChange = { appText = it },
            label = { Text("Your Text Field") }
        )

        Button(onClick = { keyboardController.clearText() }) {
            Text("Clear Virtual Keyboard")
        }

        // Add the virtual keyboard UI
        VirtualKeyboardDemo()
    }
}
*/