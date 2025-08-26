package com.example.splitkeyboard.ui.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.example.splitkeyboard.model.Key
import com.example.splitkeyboard.model.KeyType
import com.example.splitkeyboard.viewmodel.KeyboardViewModel

@Composable
fun SplitKeyboard(
    layout: List<List<Key>>,
    isCapsLockOn: Boolean,
//    onKeyPress: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: KeyboardViewModel
) {
    val configuration = LocalConfiguration.current
    val screenHeightDp = configuration.screenHeightDp.dp
    val keyboardHeight = screenHeightDp / 2f

    // Calculate dynamic row height based on available space and number of rows
    val numberOfRows = layout.size
    val totalVerticalPadding = 16.dp // padding top + bottom + between rows
    val availableHeight = keyboardHeight - totalVerticalPadding
    val rowHeight = availableHeight / numberOfRows

    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        modifier = modifier
            .fillMaxWidth()
            .height(keyboardHeight)
            .padding(vertical = 4.dp, horizontal = 2.dp),
    ) {
        layout.forEach { rowOfKeys ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(rowHeight), // Dynamic height instead of fixed 56.dp
                horizontalArrangement = Arrangement.Center
            ) {
                // Determine the split point
                val splitIndex = (rowOfKeys.size + 1) / 2
                val leftHalf = rowOfKeys.subList(0, splitIndex)
                val rightHalf = rowOfKeys.subList(splitIndex, rowOfKeys.size)

                // Render left half
                Row(
                    modifier = Modifier.weight(0.2f),
                    horizontalArrangement = Arrangement.End
                ) {
                    leftHalf.forEach { key ->
                        KeyboardKey(
                            key,
                            isCapsLockOn,
                            onKeyPress = { text ->
//                                when (key.type) {
//                                    KeyType.ACTION_TOGGLE -> viewModel.onToggleKeyboard()
//                                    KeyType.ACTION_BACKSPACE -> viewModel.onBackspace()
//                                    KeyType.ACTION_ENTER -> viewModel.onKeyPress('\n')
//                                    KeyType.ACTION_SPACE -> viewModel.onKeyPress(' ')
//                                    else -> viewModel.onKeyPress(text.first())
//                                }
                                when (key.type) {
                                    KeyType.ACTION_TOGGLE -> viewModel.onToggleKeyboard()
                                    KeyType.ACTION_BACKSPACE -> viewModel.onBackspace()
                                    KeyType.ACTION_ENTER -> viewModel.onKeyPress('\n')
                                    KeyType.ACTION_SPACE -> viewModel.onKeyPress(' ')
                                    KeyType.ACTION_SHIFT -> viewModel.onToggleCapsLock()
                                    else -> viewModel.onKeyPress(text.first())
                                }
                            },
                            modifier.background(
                                shape = RectangleShape,
                                color = Color.Black
                            )
                        )
                    }
                }

                // The gap in the middle
                Spacer(Modifier.weight(0.6f))

                // Render right half
                Row(
                    modifier = Modifier.weight(0.2f),
                    horizontalArrangement = Arrangement.Start
                ) {
                    rightHalf.forEach { key ->
                        KeyboardKey(
                            key,
                            isCapsLockOn,
                            onKeyPress = { text ->
                                when (key.type) {
                                    KeyType.ACTION_TOGGLE -> viewModel.onToggleKeyboard()
                                    KeyType.ACTION_BACKSPACE -> viewModel.onBackspace()
                                    KeyType.ACTION_ENTER -> viewModel.onKeyPress('\n')
                                    KeyType.ACTION_SPACE -> viewModel.onKeyPress(' ')
                                    KeyType.ACTION_SHIFT -> viewModel.onToggleCapsLock()
                                    else -> viewModel.onKeyPress(text.first())
                                }
                            },
                            modifier.background(
                                shape = RoundedCornerShape(50.dp),
                                color = Color.Black
                            )
                        )
                    }
                }
            }
        }
    }
}
