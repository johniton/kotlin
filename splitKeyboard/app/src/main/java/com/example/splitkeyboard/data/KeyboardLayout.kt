//package com.example.splitkeyboard.data
//
//import com.example.splitkeyboard.model.Key
//import com.example.splitkeyboard.model.KeyType
//
//object KeyboardLayout {
//    val qwertyLayout = listOf(
//        listOf(
//            Key("q", "Q"), Key("w", "W"), Key("e", "E"), Key("r", "R"), Key("t", "T"),
//            Key("y", "Y"), Key("u", "U"), Key("i", "I"), Key("o", "O"), Key("p", "P")
//        ),
//        listOf(
//            Key("a", "A"), Key("s", "S"), Key("d", "D"), Key("f", "F"), Key("g", "G"),
//            Key("h", "H"), Key("j", "J"), Key("k", "K"), Key("l", "L")
//        ),
//        listOf(
//            Key("Shift", type = KeyType.ACTION_SHIFT, weight = 0.3f),
//            Key("z", "Z"), Key("x", "X"), Key("c", "C"), Key("v", "V"),
//            Key("b", "B"), Key("n", "N"), Key("m", "M"),
//            Key("⌫", type = KeyType.ACTION_BACKSPACE, weight = 0.6f)
//        ),
//        listOf(
//            Key(" Space ", type = KeyType.ACTION_SPACE, weight = 2.4f),
//            Key("↵", type = KeyType.ACTION_ENTER, weight = 2.4f)
//        )
//    )
//}
//
//
//
//// To create more elements u just need to create more List<List<Key>>  like for the coder specific


package com.example.splitkeyboard.data

import com.example.splitkeyboard.model.Key
import com.example.splitkeyboard.model.KeyType
import com.example.splitkeyboard.model.KeyType.*

object KeyboardLayout {

    // Base weights
    private const val NORMAL_KEY = 1f
    private const val SHIFT_KEY = 1.5f
    private const val BACKSPACE_KEY = 1.5f
    private const val SPACE_KEY = 6f
    private const val ENTER_KEY = 2f

    val qwertyLayout = listOf(
        // Row 1
        listOf(
            Key("q", "Q", NORMAL_KEY), Key("w", "W", NORMAL_KEY), Key("e", "E", NORMAL_KEY),
            Key("r", "R", NORMAL_KEY), Key("t", "T", NORMAL_KEY), Key("y", "Y", NORMAL_KEY),
            Key("u", "U", NORMAL_KEY), Key("i", "I", NORMAL_KEY), Key("o", "O", NORMAL_KEY),
            Key("p", "P", NORMAL_KEY)
        ),
        // Row 2
        listOf(
            Key("a", "A", NORMAL_KEY), Key("s", "S", NORMAL_KEY), Key("d", "D", NORMAL_KEY),
            Key("f", "F", NORMAL_KEY), Key("g", "G", NORMAL_KEY), Key("h", "H", NORMAL_KEY),
            Key("j", "J", NORMAL_KEY), Key("k", "K", NORMAL_KEY), Key("l", "L", NORMAL_KEY)
        ),
        // Row 3
        listOf(
            Key("Shift", type = ACTION_SHIFT, weight = SHIFT_KEY),
            Key("z", "Z", NORMAL_KEY), Key("x", "X", NORMAL_KEY), Key("c", "C", NORMAL_KEY),
            Key("v", "V", NORMAL_KEY), Key("b", "B", NORMAL_KEY), Key("n", "N", NORMAL_KEY),
            Key("m", "M", NORMAL_KEY),
            Key("⌫", type = ACTION_BACKSPACE, weight = BACKSPACE_KEY)
        ),
        // Row 4
        listOf(
            Key("Space", type = ACTION_SPACE, weight = SPACE_KEY),
            Key("↵", type = ACTION_ENTER, weight = ENTER_KEY)
        )
    )
}
