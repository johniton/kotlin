package com.example.splitkeyboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
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
    }


