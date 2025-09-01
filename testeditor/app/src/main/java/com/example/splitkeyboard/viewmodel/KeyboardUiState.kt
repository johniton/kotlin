package com.example.splitkeyboard.viewmodel

data class KeyboardUiState(
    val currentText: String = "",
    val isCapsLockOn: Boolean = false,
    val toggleKeyboard: Boolean = false // Added this line, default to true (visible)
// Add other state properties here, e.g., shift state, symbol layout, etc.
)