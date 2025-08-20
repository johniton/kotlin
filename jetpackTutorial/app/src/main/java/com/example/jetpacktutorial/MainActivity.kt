package com.example.jetpacktutorial

import androidx.compose.foundation.layout.fillMaxSize
import android.os.Bundle
import androidx.constraintlayout.compose.layoutId
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.ChainStyle
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val constraints = ConstraintSet {
                val greenBox = createRefFor("greenBox")
                val redBox = createRefFor("redBox")
                val guideline =  createGuidelineFromTop(0.5f)
                constrain(greenBox) {
                    top.linkTo(guideline)
                    start.linkTo(parent.start)
                    width = Dimension.value(100.dp)
                    height = Dimension.value(100.dp)            
                }

                constrain(redBox){
                    top.linkTo(parent.top)
                    start.linkTo(greenBox.end)
                    end.linkTo(parent.end)
                    width = Dimension.value(100.dp)
                    height = Dimension.value(100.dp)            

                }
                createHorizontalChain(greenBox, redBox,chainStyle = ChainStyle.Packed)
    }
        ConstraintLayout(constraints,modifier=Modifier.fillMaxSize()) {

                Box(modifier = Modifier
                    .background(Color.Green)
                    .layoutId("greenBox"))

                Box(modifier = Modifier
                    .background(Color.Red)
                    .layoutId("redBox"))


}
    }
    }
}