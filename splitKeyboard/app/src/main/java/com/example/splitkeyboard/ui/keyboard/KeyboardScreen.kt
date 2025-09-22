package com.example.splitkeyboard.ui.keyboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.splitkeyboard.data.KeyboardLayout
import com.example.splitkeyboard.viewmodel.KeyboardViewModel
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalTextStyle

@Composable
fun KeyboardScreen(viewModel: KeyboardViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // for selecting the layout.
    val layout = if (uiState.toggleKeyboard) {
        KeyboardLayout.developerLayout
    } else {
        KeyboardLayout.qwertyLayout
    }

    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val (textFieldRef, keyboardRef) = createRefs()

        // Text field spans full height and middle 60% width
        OutlinedTextField(
            value = uiState.currentText,
            onValueChange = {}, // Text changes are handled by the custom keyboard
            readOnly = true,
            label = { Text("Code Editor") },
            textStyle = LocalTextStyle.current,
            maxLines = Int.MAX_VALUE, // Allow text to grow indefinitely for scrolling
            modifier = Modifier
                .constrainAs(textFieldRef) {
                    // Full height from top to bottom
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)

                    // Middle 60% width (0.2f to 0.8f)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.percent(0.6f)
                    height = Dimension.fillToConstraints // Fill available height

                    centerHorizontallyTo(parent) // Center the 60% width element
                }
                .verticalScroll(rememberScrollState()) // Make the content scrollable
                .padding(5.dp) // Add some padding around the text field
        )

        // Split keyboard overlays on top of the text field
        SplitKeyboard(
            layout = layout,
            isCapsLockOn = uiState.isCapsLockOn,
            viewModel = viewModel,
            modifier = Modifier.constrainAs(keyboardRef) {
                // Keyboard positioned at the bottom
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints // Fills available width
                // The keyboard will naturally overlay on top of the text field
            }
        )
    }
}


