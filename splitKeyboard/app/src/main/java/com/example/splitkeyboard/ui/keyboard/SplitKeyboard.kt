//package com.example.splitkeyboard.ui.keyboard
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.aspectRatio
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.RectangleShape
//import androidx.compose.ui.platform.LocalConfiguration
//import androidx.compose.ui.unit.dp
//import com.example.splitkeyboard.model.Key
//
//
//@Composable
//fun SplitKeyboard(
//    layout: List<List<Key>>,
//    isCapsLockOn: Boolean,
//    onKeyPress: (String) -> Unit,
//    modifier: Modifier = Modifier
//) {
//    val configuration = LocalConfiguration.current
//    val screenHeightDp = configuration.screenHeightDp.dp
//    val keyboardHeight = screenHeightDp / 2f
//
//    // Calculates dynamic row height based on available space and number of rows
//    val numberOfRows = layout.size
//    val totalVerticalPadding = 16.dp // padding top + bottom + between rows
//    val availableHeight = keyboardHeight - totalVerticalPadding
//    val rowHeight = availableHeight / numberOfRows
//    Column(
////        verticalArrangement = Arrangement.Bottom,
//        verticalArrangement = Arrangement.SpaceEvenly,
//        modifier = modifier
//            .fillMaxWidth()
//            .padding(1.dp),
//
//    ) {
//        layout.forEach { rowOfKeys ->
//            Row(
//                modifier = Modifier.fillMaxWidth().height(rowHeight),
//                horizontalArrangement = Arrangement.Center
//            ) {
//// Determine the split point
//                val splitIndex = (rowOfKeys.size + 1) / 2
//                val leftHalf = rowOfKeys.subList(0, splitIndex)
//                val rightHalf = rowOfKeys.subList(splitIndex, rowOfKeys.size)
//
//
//// Render left half
//                Row(
//                    modifier = Modifier.weight(0.2f),
//                    horizontalArrangement = Arrangement.End
//                ) {
//                    leftHalf.forEach { key ->
//                        KeyboardKey(key, isCapsLockOn, onKeyPress,modifier.aspectRatio(1f).background(shape = RectangleShape, color = Color.Black))
//                    }
//                }
//// The gap in the middle
//                Spacer(Modifier.weight(0.6f))
//// Render right half
//                Row(
//                    modifier = Modifier.weight(0.2f),
//                    horizontalArrangement = Arrangement.Start
//                ) {
//                    rightHalf.forEach { key ->
//                        KeyboardKey(key, isCapsLockOn, onKeyPress,modifier.aspectRatio(1f).background(shape = RectangleShape, color = Color.Black))
//                    }
//                }
//            }
//        }
//    }
//}
///////
package com.example.splitkeyboard.ui.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.example.splitkeyboard.model.Key

@Composable
fun SplitKeyboard(
    layout: List<List<Key>>,
    isCapsLockOn: Boolean,
    onKeyPress: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenHeightDp = configuration.screenHeightDp.dp
    val keyboardHeight = screenHeightDp / 2f

    // Calculate dynamic row height based on available space and number of rows
    val numberOfRows = layout.size
    val totalVerticalPadding = 16.dp // padding top + bottom + between rows
    val availableHeight = keyboardHeight - totalVerticalPadding
    val rowHeight = availableHeight / numberOfRows

    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        modifier = modifier
            .fillMaxWidth()
            .height(keyboardHeight)
            .padding(vertical = 4.dp, horizontal = 2.dp),
    ) {
        layout.forEach { rowOfKeys ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(rowHeight), // Dynamic height instead of fixed 56.dp
                horizontalArrangement = Arrangement.Center
            ) {
                // Determine the split point
                val splitIndex = (rowOfKeys.size + 1) / 2
                val leftHalf = rowOfKeys.subList(0, splitIndex)
                val rightHalf = rowOfKeys.subList(splitIndex, rowOfKeys.size)

                // Render left half
                Row(
                    modifier = Modifier.weight(0.2f),
                    horizontalArrangement = Arrangement.End
                ) {
                    leftHalf.forEach { key ->
                        KeyboardKey(
                            key,
                            isCapsLockOn,
                            onKeyPress,
                            modifier.background(
                                shape = RectangleShape,
                                color = Color.Black
                            )
                        )
                    }
                }

                // The gap in the middle
                Spacer(Modifier.weight(0.6f))

                // Render right half
                Row(
                    modifier = Modifier.weight(0.2f),
                    horizontalArrangement = Arrangement.Start
                ) {
                    rightHalf.forEach { key ->
                        KeyboardKey(
                            key,
                            isCapsLockOn,
                            onKeyPress,
                            modifier.background(
                                shape = RectangleShape,
                                color = Color.Black
                            )
                        )
                    }
                }
            }
        }
    }
}

//////
//package com.example.splitkeyboard.ui.keyboard
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.RectangleShape
//import androidx.compose.ui.platform.LocalConfiguration
//import androidx.compose.ui.unit.dp
//import com.example.splitkeyboard.model.Key
//
//@Composable
//fun SplitKeyboard(
//    layout: List<List<Key>>,
//    isCapsLockOn: Boolean,
//    onKeyPress: (String) -> Unit,
//    modifier: Modifier = Modifier
//) {
//    val configuration = LocalConfiguration.current
//    val screenHeightDp = configuration.screenHeightDp.dp
//    val keyboardHeight = screenHeightDp / 2f
//
//    // Calculate dynamic row height based on available space and number of rows
//    val numberOfRows = layout.size
//    val totalVerticalPadding = 16.dp // padding top + bottom + between rows
//    val availableHeight = keyboardHeight - totalVerticalPadding
//    val rowHeight = availableHeight / numberOfRows
//
//    Column(
//        verticalArrangement = Arrangement.SpaceEvenly,
//        modifier = modifier
//            .fillMaxWidth()
//            .height(keyboardHeight) // THIS IS CRUCIAL - constrains the total height
//            .padding(vertical = 4.dp, horizontal = 1.dp),
//    ) {
//        layout.forEach { rowOfKeys ->
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(rowHeight), // Dynamic height for each row
//                horizontalArrangement = Arrangement.Center
//            ) {
//                // Determine the split point
//                val splitIndex = (rowOfKeys.size + 1) / 2
//                val leftHalf = rowOfKeys.subList(0, splitIndex)
//                val rightHalf = rowOfKeys.subList(splitIndex, rowOfKeys.size)
//
//                // Render left half
//                Row(
//                    modifier = Modifier.weight(0.2f),
//                    horizontalArrangement = Arrangement.End
//                ) {
//                    leftHalf.forEach { key ->
//                        KeyboardKey(
//                            key,
//                            isCapsLockOn,
//                            onKeyPress,
//                            Modifier.background(
//                                shape = RectangleShape,
//                                color = Color.Black
//                            )
//                        )
//                    }
//                }
//
//                // The gap in the middle
//                Spacer(Modifier.weight(0.6f))
//
//                // Render right half
//                Row(
//                    modifier = Modifier.weight(0.2f),
//                    horizontalArrangement = Arrangement.Start
//                ) {
//                    rightHalf.forEach { key ->
//                        KeyboardKey(
//                            key,
//                            isCapsLockOn,
//                            onKeyPress,
//                            Modifier.background(
//                                shape = RectangleShape,
//                                color = Color.Black
//                            )
//                        )
//                    }
//                }
//            }
//        }
//    }
//}