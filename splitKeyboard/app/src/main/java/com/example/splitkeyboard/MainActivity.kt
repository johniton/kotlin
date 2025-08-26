package com.example.splitkeyboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.splitkeyboard.data.KeyboardLayout.developerLayout
import com.example.splitkeyboard.data.KeyboardLayout.qwertyLayout
import com.example.splitkeyboard.ui.keyboard.KeyboardScreen
import com.example.splitkeyboard.ui.keyboard.SplitKeyboard
import com.example.splitkeyboard.ui.theme.SplitKeyboardTheme
import com.example.splitkeyboard.viewmodel.KeyboardViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Hide system bars (immersive mode)

        WindowCompat.setDecorFitsSystemWindows(window, false)  // makes the bars hide
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars()) // hide both status + nav
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE   // Swiping makes the bars appear
        }

        setContent {
            // Detect if device has soft navigation bar
            val view = LocalView.current
            val insets = ViewCompat.getRootWindowInsets(view)
            val hasSoftNav =
                (insets?.getInsets(WindowInsetsCompat.Type.navigationBars())?.bottom ?: 0) > 0

            // Creating a view model in the highest parent so it can be passed down
            val keyboardViewModel: KeyboardViewModel = viewModel()

            SplitKeyboardTheme {
                Column(
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.fillMaxSize()
                        .background(color = Color.Black)
                        .windowInsetsPadding(WindowInsets.displayCutout)
                        .then(
                            if (hasSoftNav) Modifier else Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                        ),  // To avoid nodge

                ) {
                    KeyboardScreen(viewModel = keyboardViewModel)
                }
            }
        }
    }
}
