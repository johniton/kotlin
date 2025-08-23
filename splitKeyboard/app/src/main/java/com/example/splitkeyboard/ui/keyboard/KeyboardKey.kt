package com.example.splitkeyboard.ui.keyboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.splitkeyboard.model.Key
import com.example.splitkeyboard.model.KeyType
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.graphicsLayer
@Composable
fun RowScope.KeyboardKey(
    key: Key,
    isCapsLockOn: Boolean,
    onKeyPress: (String) -> Unit,
    modifier: Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
// Animate scale based on the isPressed state
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1.0f,
        label = "keyScaleAnimation"
    )
    val textToShow = when {
        isCapsLockOn -> key.shiftText?: key.text.uppercase()
        else -> key.text
    }
        val keyColor = when (key.type) {
        KeyType.CHARACTER -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.secondaryContainer
    }
//... (rest of the key logic)
    Surface(
        modifier = Modifier
            .graphicsLayer(scaleX = scale, scaleY = scale) // Apply the animated scale
            .weight(key.weight)
            .padding(2.dp)
            .fillMaxHeight()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onKeyPress(textToShow) }
            ),
        shape = MaterialTheme.shapes.medium,
        color = keyColor,
        tonalElevation = 3.dp
    ) {
       Box(contentAlignment = Alignment.Center) {
         Text(
              text = textToShow,
              style = MaterialTheme.typography.bodyLarge
          )
      }
    }
}