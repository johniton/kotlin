package com.example.splitkeyboard.ui.keyboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.splitkeyboard.model.Key


@Composable
fun SplitKeyboard(
    layout: List<List<Key>>,
    isCapsLockOn: Boolean,
    onKeyPress: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        layout.forEach { rowOfKeys ->
            Row(
                modifier = Modifier.fillMaxWidth().height(56.dp),
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
                        KeyboardKey(key, isCapsLockOn, onKeyPress,modifier)
                    }
                }
// The gap in the middle
                Spacer(Modifier.weight(0.6f)) // Or use a weighted spacer
// Render right half
                Row(
                    modifier = Modifier.weight(0.2f),
                    horizontalArrangement = Arrangement.Start
                ) {
                    rightHalf.forEach { key ->
                        KeyboardKey(key, isCapsLockOn, onKeyPress,modifier)
                    }
                }
            }
        }
    }
}
