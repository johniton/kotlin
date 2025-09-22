package com.example.splitkeyboard.data

import com.example.splitkeyboard.model.Key
import com.example.splitkeyboard.model.KeyType.*

object KeyboardLayout {

    // Base weights
    private const val NORMAL_KEY = 1f
    private const val SHIFT_KEY = 3f
    private const val BACKSPACE_KEY = 1.5f
    private const val SPACE_KEY = 6f
    private const val ENTER_KEY = 2f


    val qwertyLayout = listOf(
        // Row 1
        listOf(
            Key("1", "1", NORMAL_KEY), Key("2", "2", NORMAL_KEY), Key("3", "3", NORMAL_KEY),
            Key("4", "4", NORMAL_KEY), Key("5", "5", NORMAL_KEY), Key("6", "6", NORMAL_KEY),
            Key("7", "7", NORMAL_KEY), Key("8", "8", NORMAL_KEY), Key("9", "9", NORMAL_KEY),
            Key("0", "0", NORMAL_KEY)
        ),
        // Row 2
        listOf(
            Key("q", "Q", NORMAL_KEY), Key("w", "W", NORMAL_KEY), Key("e", "E", NORMAL_KEY),
            Key("r", "R", NORMAL_KEY), Key("t", "T", NORMAL_KEY), Key("y", "Y", NORMAL_KEY),
            Key("u", "U", NORMAL_KEY), Key("i", "I", NORMAL_KEY), Key("o", "O", NORMAL_KEY),
            Key("p", "P", NORMAL_KEY)
        ),
        // Row 3
        listOf(
            Key("a", "A", NORMAL_KEY), Key("s", "S", NORMAL_KEY), Key("d", "D", NORMAL_KEY),
            Key("f", "F", NORMAL_KEY), Key("g", "G", NORMAL_KEY), Key("h", "H", NORMAL_KEY),
            Key("j", "J", NORMAL_KEY), Key("k", "K", NORMAL_KEY), Key("l", "L", NORMAL_KEY),
            Key(text = ";", shiftText = ":", weight = NORMAL_KEY)
        ),
        // Row 4
        listOf(
            Key("Shift", shiftText = "⬆\uFE0F", type = ACTION_SHIFT, weight = NORMAL_KEY),
            Key("z", "Z", NORMAL_KEY), Key("x", "X", NORMAL_KEY), Key("c", "C", NORMAL_KEY),
            Key("v", "V", NORMAL_KEY), Key("b", "B", NORMAL_KEY), Key("n", "N", NORMAL_KEY),
            Key("m", "M", NORMAL_KEY),Key(text = ",", shiftText = ",", weight = NORMAL_KEY),Key(text = ".", shiftText = ".", weight = NORMAL_KEY)
        ),
        //Row 5
        listOf(
            Key("🌐", type = ACTION_TOGGLE, weight = NORMAL_KEY),
            Key("Space", type = ACTION_SPACE, weight = NORMAL_KEY*3),
            Key(text = "{}", shiftText = "{}", weight = NORMAL_KEY),


            Key(text = "()", shiftText = "()", weight = NORMAL_KEY),
            Key("↵", type = ACTION_ENTER, weight = NORMAL_KEY*1.5f),
            Key("⌫", type = ACTION_BACKSPACE, weight = NORMAL_KEY),
            )
    )


    val developerLayout = listOf(
        //Row 1
        listOf(
            Key("{", "{", NORMAL_KEY), Key("}", "}", NORMAL_KEY), Key("(", "(", NORMAL_KEY),
            Key(")", ")", NORMAL_KEY), Key("[", "[", NORMAL_KEY), Key("]", "]", NORMAL_KEY),
            Key("<", "<", NORMAL_KEY), Key(">", ">", NORMAL_KEY), Key(";", ";", NORMAL_KEY),
            Key(":", ":", NORMAL_KEY),
        ),

        //Row 2
        listOf(
            Key(".", ".", NORMAL_KEY), Key(",", ",", NORMAL_KEY), Key("\"", "\"", NORMAL_KEY),
            Key("'", "'", NORMAL_KEY), Key("`", "`", NORMAL_KEY), Key("\\", "\\", NORMAL_KEY),
            Key("/", "/", NORMAL_KEY), Key("|", "|", NORMAL_KEY), Key("&", "&", NORMAL_KEY),
            Key("=", "=", NORMAL_KEY),
            ),

        // Row 3
        listOf(
            Key("+", "+", NORMAL_KEY), Key("-", "-", NORMAL_KEY), Key("*", "*", NORMAL_KEY),
            Key("%", "%", NORMAL_KEY), Key("!", "!", NORMAL_KEY), Key("?", "?", NORMAL_KEY),
            Key("~", "~", NORMAL_KEY), Key("^", "^", NORMAL_KEY), Key("#", "#", NORMAL_KEY),
            Key("$", "$", NORMAL_KEY),
        ),

        // Row 4
        listOf(
            Key("@", "@", NORMAL_KEY), Key(":", ":", NORMAL_KEY), Key("_", "_", NORMAL_KEY),
            Key("?", "?", NORMAL_KEY), Key("&&", "&&", NORMAL_KEY), Key("\t", "\t", NORMAL_KEY), // tab/indent
            Key("\n", "\n", NORMAL_KEY), Key("=>", "=>", NORMAL_KEY),Key("→", "→", NORMAL_KEY),
            Key("*", "*", NORMAL_KEY)
        ),

        // Row 5
        listOf(
            Key("\uD83D\uDD24", type = ACTION_TOGGLE, weight = NORMAL_KEY), Key("Ctrl", type = ACTION_SHIFT, weight = NORMAL_KEY), Key("Space", type = ACTION_SPACE, weight = NORMAL_KEY*3),

            Key("=>", "=>", NORMAL_KEY),Key("→", "→", NORMAL_KEY), Key("*", "*", NORMAL_KEY)
        )
    )
}
//@Composable
//fun RowScope.KeyboardKey(
//    key: Key,
//    isCapsLockOn: Boolean,
//    onKeyPress: (String) -> Unit,
//    modifier: Modifier = Modifier
//) {
//    val interactionSource = remember { MutableInteractionSource() }
//    val isPressed by interactionSource.collectIsPressedAsState()
//
//    // Animate scale
//    val scale by animateFloatAsState(
//        targetValue = if (isPressed) 0.92f else 1.0f,
//        label = "keyScaleAnimation"
//    )
//
//    val textToShow = when {
//        isCapsLockOn -> key.shiftText ?: key.text.uppercase()
//        else -> key.text
//    }
//
//    // Base colors per key type (dark theme palette)
//    val baseColor = when (key.type) {
//        KeyType.CHARACTER -> Color(0xFF1C1C1E) // dark charcoal
//        KeyType.ACTION_SHIFT,
//        KeyType.ACTION_ENTER,
//        KeyType.ACTION_BACKSPACE -> Color(0xFF2C2C30) // slightly lighter gray
//        else -> Color(0xFF050A30) // deep accent blue
//    }
//
//    // Pressed → slightly darker
//    val backgroundColor = if (isPressed) baseColor.copy(alpha = 0.85f) else baseColor
//
//    Surface(
//        modifier = Modifier
//            .graphicsLayer(scaleX = scale, scaleY = scale) // keep press animation
//            .weight(key.weight)
//            .padding(1.dp)
//            .fillMaxHeight()
//            .clickable(
//                interactionSource = interactionSource,
//                indication = null,
//                onClick = { onKeyPress(textToShow) }
//            ),
//        color = backgroundColor,
//        tonalElevation = 3.dp,
//        shape = RoundedCornerShape(6.dp),
//        border = BorderStroke(
//            1.dp,
//            if (isCapsLockOn && key.text=="⇧")
//                Color(0xFF00E5FF) // teal glow for active Caps
//            else Color(0xFF2E2E38)
//        )
//    ) {
//        Box(
//            contentAlignment = Alignment.Center,
//            modifier = Modifier.fillMaxSize()
//        ) {
//            Text(
//                text = textToShow,
//                style = MaterialTheme.typography.bodySmall,
//                color = if (isPressed) Color(0xFFB0C4DE) else Color.White
//            )
//        }
//    }
//}
