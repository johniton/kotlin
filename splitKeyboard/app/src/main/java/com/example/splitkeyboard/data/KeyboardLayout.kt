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
            Key("1", null, NORMAL_KEY), Key("2", null, NORMAL_KEY), Key("3", null, NORMAL_KEY),
            Key("4", null, NORMAL_KEY), Key("5", null, NORMAL_KEY), Key("6", null, NORMAL_KEY),
            Key("7", null, NORMAL_KEY), Key("8", null, NORMAL_KEY), Key("9", null, NORMAL_KEY),
            Key("0", null, NORMAL_KEY)
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
            Key("Shift", type = ACTION_SHIFT, weight = NORMAL_KEY),
            Key("z", "Z", NORMAL_KEY), Key("x", "X", NORMAL_KEY), Key("c", "C", NORMAL_KEY),
            Key("v", "V", NORMAL_KEY), Key("b", "B", NORMAL_KEY), Key("n", "N", NORMAL_KEY),
            Key("m", "M", NORMAL_KEY),Key(text = ",", shiftText = null, weight = NORMAL_KEY),Key(text = ".", shiftText = null, weight = NORMAL_KEY)
        ),
        listOf(
            Key(text = "Ctrl", shiftText = null, weight = NORMAL_KEY),
            Key("Space", type = ACTION_SPACE, weight = NORMAL_KEY*3),
            Key("⌫", type = ACTION_BACKSPACE, weight = NORMAL_KEY),

            Key(text = "Alt", shiftText = null, weight = NORMAL_KEY),
            Key("↵", type = ACTION_ENTER, weight = NORMAL_KEY*1.5f),
            Key(text = "Shift", shiftText = null, weight = NORMAL_KEY)
            )
    )
}
