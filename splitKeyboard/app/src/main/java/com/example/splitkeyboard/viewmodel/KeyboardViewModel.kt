
package com.example.splitkeyboard.viewmodel

import androidx.lifecycle.ViewModel
import com.example.splitkeyboard.ui.keyboard.KeyboardUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class KeyboardViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(KeyboardUiState())
    val uiState: StateFlow<KeyboardUiState> = _uiState.asStateFlow()

    fun onKeyPress(key: Char) {
        _uiState.update { currentState ->
            val text = if (currentState.isCapsLockOn) key.uppercaseChar() else key
            currentState.copy(currentText = currentState.currentText + text)
        }
    }

    fun onBackspace() {
        _uiState.update { currentState ->
            currentState.copy(
                currentText = currentState.currentText.dropLast(1)
            )
        }
    }

    fun onToggleCapsLock() {
        _uiState.update { currentState ->
            currentState.copy(isCapsLockOn = !currentState.isCapsLockOn)
        }
    }
}
