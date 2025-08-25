package com.example.splitkeyboard

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.xr.compose.testing.toDp
import com.example.splitkeyboard.ui.keyboard.SplitKeyboard
import com.example.splitkeyboard.ui.theme.SplitKeyboardTheme
import com.example.splitkeyboard.data.KeyboardLayout.qwertyLayout

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SplitKeyboardTheme {
                Column (
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.fillMaxSize()
                ){
                    Keyboard()
                }
            }
        }
    }

@Composable
fun Keyboard(){
    Box(
        modifier = Modifier.background(color = Color.Black)
    ) {
        val configuration = LocalConfiguration.current
        val screenHeightPx = configuration.screenHeightDp     // in dp
        val screenHeightDp = screenHeightPx.dp
        Box(
            modifier = Modifier.fillMaxWidth()
                .height(screenHeightDp/2f),
//            .background(color = Color.Cyan),
            contentAlignment = Alignment.BottomCenter

        ) {
            SplitKeyboard(
                layout = qwertyLayout,
                isCapsLockOn = false,
                onKeyPress = {},
                modifier = Modifier
            )
        }
    }
    }

}

