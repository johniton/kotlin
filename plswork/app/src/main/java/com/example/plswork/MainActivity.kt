//package com.example.plswork
//
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.OutlinedTextField
//import androidx.compose.material3.Surface
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.TextRange
//import androidx.compose.ui.text.input.TextFieldValue
//
//import com.wakaztahir.codeeditor.model.CodeLang
//import com.wakaztahir.codeeditor.prettify.PrettifyParser
//import com.wakaztahir.codeeditor.theme.CodeThemeType
//import com.wakaztahir.codeeditor.utils.parseCodeAsAnnotatedString
//
//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            MaterialTheme {
//                Surface(Modifier.fillMaxSize()) {
//                    CodeEditorDemo()
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun CodeEditorDemo() {
//    val parser = remember { PrettifyParser() }
//    val theme = remember { CodeThemeType.Monokai.theme }
//
//    var code by remember {
//        mutableStateOf(
//            TextFieldValue(
//                text = "fun main() {\n    println(\"Hello Compose!\")\n}",
//                selection = TextRange(0)
//            )
//        )
//    }
//
//    OutlinedTextField(
//        value = code,
////        colors = Color.Black,
//        onValueChange = { newVal ->
//            code = newVal.copy(
//                annotatedString = parseCodeAsAnnotatedString(
//                    parser = parser,
//                    theme = theme,
////                    lang = CodeLang.Kotlin,
//                    lang = CodeLang.C,
//                    code = newVal.text
//                )
//            )
//        },
//        modifier = Modifier.fillMaxSize()
//            .background(color = Color.Gray),
//        maxLines = Int.MAX_VALUE
//
//    )
//}

// Make sure this package name matches your project's package name
package com.example.plswork

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.wakaztahir.codeeditor.model.CodeLang
import com.wakaztahir.codeeditor.prettify.PrettifyParser
import com.wakaztahir.codeeditor.theme.CodeThemeType
import com.wakaztahir.codeeditor.utils.parseCodeAsAnnotatedString

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // This Column will display both of your code editor examples
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Read-Only Code Viewer:", style = MaterialTheme.typography.titleMedium)
                        SimpleCodeViewer()

                        Text("Editable Code Field:", style = MaterialTheme.typography.titleMedium)
                        // Use a Box with a weight to make the editable field take up more space
                        Box(modifier = Modifier.weight(1f)) {
                            EditableCodeField()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SimpleCodeViewer() {
    val language = CodeLang.Kotlin
    val code = """
        package com.example.myapp
        fun main() {
            println("Hello, Compose Code Editor!")
        }
    """.trimIndent()
    val parser = remember { PrettifyParser() }

    // THE FIX: Added parentheses () to .theme to call it as a function
    val theme = remember { CodeThemeType.Monokai.theme }

    val parsedCode = remember(code, theme) {
        parseCodeAsAnnotatedString(
            parser = parser,
            theme = theme,
            lang = language,
            code = code
        )
    }
    Text(parsedCode)
}

@Composable
fun EditableCodeField() {
    val language = CodeLang.Kotlin
    val initialCode = "fun main() {\n    // Your code here\n}"
    val parser = remember { PrettifyParser() }

    // THE FIX: Added parentheses () to .theme to call it as a function
    val theme = remember { CodeThemeType.Monokai.theme }

    fun parse(code: String) = parseCodeAsAnnotatedString(
        parser = parser, theme = theme, lang = language, code = code
    )

    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(annotatedString = parse(initialCode)))
    }

    OutlinedTextField(
        value = textFieldValue,
        onValueChange = {
            textFieldValue = it.copy(
                annotatedString = parse(it.text)
            )
        },
        // Use fillMaxSize() to make the text field fill the available space
        modifier = Modifier.fillMaxWidth().fillMaxHeight()
    )
}
