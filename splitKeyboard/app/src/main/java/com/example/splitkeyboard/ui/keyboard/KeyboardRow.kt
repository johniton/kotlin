package com.example.splitkeyboard.ui.keyboard
//
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.example.splitkeyboard.model.Key

@Composable
fun KeyboardRow(
    keys: List<Key>,
    isCapsLockOn: Boolean,
    onKeyPress: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenWidthPx = configuration.screenWidthDp     // in dp
    val screenWidthDp = screenWidthPx.dp

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(screenWidthDp/2.5f), // A typical key height
        horizontalArrangement = Arrangement.Center
    ) {

        keys.forEach { key ->
            KeyboardKey(
                key = key,
                isCapsLockOn = isCapsLockOn,
                onKeyPress = onKeyPress,
                modifier
            )
        }
    }
}
