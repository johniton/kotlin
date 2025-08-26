package com.example.splitkeyboard.ui.keyboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.splitkeyboard.data.KeyboardLayout
import com.example.splitkeyboard.viewmodel.KeyboardViewModel



@Composable
fun KeyboardScreen(viewModel: KeyboardViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val layout = if (viewModel.toggleKeyboard) {
        KeyboardLayout.developerLayout
    } else {
        KeyboardLayout.qwertyLayout
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = uiState.currentText,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            label = { Text("Custom Input") }
        )

        Spacer(modifier = Modifier.weight(1f))

        SplitKeyboard(
            layout = layout,
            isCapsLockOn = uiState.isCapsLockOn,
            viewModel = viewModel,
            modifier = Modifier
        )
    }
}
