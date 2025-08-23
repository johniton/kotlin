package com.example.splitkeyboard.ui.keyboard



data class KeyboardUiState(
    val currentText: String = "",
    val isCapsLockOn: Boolean = false,
// Add other state properties here, e.g., shift state, symbol layout, etc.
)
