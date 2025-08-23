package com.example.splitkeyboard.ui.keyboard

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.splitkeyboard.viewmodel.KeyboardViewModel

@Composable
fun KeyboardScreen(
    viewModel: KeyboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SplitKeyboardRoot(
        currentText = uiState.currentText,
        isCapsLockOn = uiState.isCapsLockOn,
        onKeyPress = { char -> viewModel.onKeyPress(char) },
        onBackspace = { viewModel.onBackspace() },
        onToggleCapsLock = { viewModel.onToggleCapsLock() }
    )
}

@Composable
fun SplitKeyboardRoot(
    currentText: String,
    isCapsLockOn: Boolean,
    onKeyPress: (Char) -> Unit,
    onBackspace: () -> Unit,
    onToggleCapsLock: () -> Unit
) {
    // Temporary UI for testing
    Column {
        Text(text = currentText)
        Button(onClick = { onKeyPress('A') }) {
            Text("A")
        }
        Button(onClick = onBackspace) { Text("Backspace") }
        Button(onClick = onToggleCapsLock) { Text("CapsLock") }
    }
}
