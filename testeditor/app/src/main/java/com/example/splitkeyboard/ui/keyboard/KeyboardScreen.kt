//package com.example.splitkeyboard.ui.keyboard
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.OutlinedTextField
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import androidx.lifecycle.compose.collectAsStateWithLifecycle
//import androidx.lifecycle.viewmodel.compose.viewModel
//import com.example.splitkeyboard.data.KeyboardLayout
//import com.example.splitkeyboard.viewmodel.KeyboardViewModel
//import androidx.constraintlayout.compose.ConstraintLayout
//import androidx.constraintlayout.compose.Dimension
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material3.LocalTextStyle
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.input.TextFieldValue
//import com.example.splitkeyboard.viewmodel.KeyboardUiState
//import com.wakaztahir.codeeditor.model.CodeLang
//import com.wakaztahir.codeeditor.prettify.PrettifyParser
//import com.wakaztahir.codeeditor.theme.CodeThemeType
//import com.wakaztahir.codeeditor.utils.parseCodeAsAnnotatedString
//
//@Composable
//fun KeyboardScreen(viewModel: KeyboardViewModel = viewModel()) {
//    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
//
//    // for selecting the layout.
//    val layout = if (uiState.toggleKeyboard) {
//        KeyboardLayout.developerLayout
//    } else {
//        KeyboardLayout.qwertyLayout
//    }
//
//    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
//        val (textFieldRef, keyboardRef) = createRefs()
//
//        // Text field spans full height and middle 60% width
////        OutlinedTextField(
////            value = uiState.currentText,
////            onValueChange = {}, // Text changes are handled by the custom keyboard
////            readOnly = true,
////            label = { Text("Code Editor") },
////            textStyle = LocalTextStyle.current,
////            maxLines = Int.MAX_VALUE, // Allow text to grow indefinitely for scrolling
////            modifier = Modifier
////                .constrainAs(textFieldRef) {
////                    // Full height from top to bottom
////                    top.linkTo(parent.top)
////                    bottom.linkTo(parent.bottom)
////
////                    // Middle 60% width (0.2f to 0.8f)
////                    start.linkTo(parent.start)
////                    end.linkTo(parent.end)
////                    width = Dimension.percent(0.6f)
////                    height = Dimension.fillToConstraints // Fill available height
////
////                    centerHorizontallyTo(parent) // Center the 60% width element
////                }
////                .verticalScroll(rememberScrollState()) // Make the content scrollable
////                .padding(5.dp) // Add some padding around the text field
//
////        )
//        EditableCodeField(
//            modifier = Modifier.constrainAs(textFieldRef)  {
//                    // Full height from top to bottom
//                    top.linkTo(parent.top)
//                    bottom.linkTo(parent.bottom)
//
//                    // Middle 60% width (0.2f to 0.8f)
//                    start.linkTo(parent.start)
//                    end.linkTo(parent.end)
//                    width = Dimension.percent(0.6f)
//                    height = Dimension.fillToConstraints // Fill available height
//
//                    centerHorizontallyTo(parent) // Center the 60% width element
//                }
//                .verticalScroll(rememberScrollState()) // Make the content scrollable
//                .padding(5.dp) ,
//                uiState
//        )
//
//        // Split keyboard overlays on top of the text field
//        SplitKeyboard(
//            layout = layout,
//            isCapsLockOn = uiState.isCapsLockOn,
//            viewModel = viewModel,
//            modifier = Modifier.constrainAs(keyboardRef) {
//                // Keyboard positioned at the bottom
//                bottom.linkTo(parent.bottom)
//                start.linkTo(parent.start)
//                end.linkTo(parent.end)
//                width = Dimension.fillToConstraints // Fills available width
//                // The keyboard will naturally overlay on top of the text field
//            }
//        )
//    }
//}
//
//@Composable
//fun EditableCodeField(modifier: Modifier,uiState: KeyboardUiState) {
//    val language = CodeLang.C
//    val initialCode = "fun main() {\n    // Your code here\n}"
//    val parser = remember { PrettifyParser() }
//
//    // THE FIX: Added parentheses () to .theme to call it as a function
//    val theme = remember { CodeThemeType.Monokai.theme }
//
//    fun parse(code: String) = parseCodeAsAnnotatedString(
//        parser = parser, theme = theme, lang = language, code = code
//    )
//
//    var textFieldValue by remember {
//        mutableStateOf(TextFieldValue(annotatedString = parse(initialCode)))
//    }
//
//    OutlinedTextField(
//        value = uiState.currentText,
//        readOnly = true,
//            label = { Text("Code Editor") },
//            textStyle = LocalTextStyle.current,
//            maxLines = Int.MAX_VALUE,
//        onValueChange = {},
//        // Use fillMaxSize() to make the text field fill the available space
//        modifier=modifier.background(color = Color.Gray)
//    )
//}
//
//

package com.example.splitkeyboard.ui.keyboard

import androidx.compose.foundation.background
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import com.example.splitkeyboard.viewmodel.KeyboardUiState
import com.wakaztahir.codeeditor.model.CodeLang
import com.wakaztahir.codeeditor.prettify.PrettifyParser
import com.wakaztahir.codeeditor.theme.CodeThemeType
import com.wakaztahir.codeeditor.utils.parseCodeAsAnnotatedString

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

        EditableCodeField(
            modifier = Modifier.constrainAs(textFieldRef) {
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
                .padding(5.dp),
            uiState = uiState
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

@Composable
fun EditableCodeField(modifier: Modifier, uiState: KeyboardUiState) {
    val language = CodeLang.C // Changed to Kotlin for better highlighting
    val parser = remember { PrettifyParser() }

    // Keep it as .theme without parentheses since that works for you
    val theme = remember { CodeThemeType.Default.theme }

    fun parse(code: String) = parseCodeAsAnnotatedString(
        parser = parser,
        theme = theme,
        lang = language,
        code = code
    )

    // Parse the actual UI state text and update when it changes
    val parsedText = remember(uiState.currentText) {
        val textToParse = uiState.currentText.ifEmpty {
            "int main() {\n    printf(\"Hello World!\")\n    // Start typing your Kotlin code here...\n}"
        }
        parse(textToParse)
    }

    // Create TextFieldValue with the parsed annotated string
    val textFieldValue = remember(parsedText) {
        TextFieldValue(
            annotatedString = parsedText,
            selection = TextRange(uiState.currentText.length) // Keep cursor at end
        )
    }

    OutlinedTextField(
        value = textFieldValue, // Use the parsed annotated string
        readOnly = true,
        label = { Text("Code Editor") },
        textStyle = LocalTextStyle.current.copy(
            fontFamily = FontFamily.Monospace // Better for code display
        ),
        maxLines = Int.MAX_VALUE,
        onValueChange = {}, // Still readonly since keyboard handles input
        modifier = modifier.background(color = Color.Black) // Changed to black for better contrast with Monokai theme
    )
}