package com.example.jetpacktutorial

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.height
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import com.example.jetpacktutorial.ImageCard

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
        Column(
            modifier =
                    Modifier.fillMaxSize()
                            .padding(WindowInsets.safeDrawing.asPaddingValues()),
    ) {
        val painter: Painter = painterResource(id = R.drawable.mountain)
        val description = "Mountain Image"
        val title = "Mountain Image"
        Box(modifier = Modifier.fillMaxWidth(0.5f).padding(16.dp)){
        ImageCard(
            painter = painter,
            contentDescription = description,
            title = title,
            modifier = Modifier
                .padding(16.dp)

        )
        }   
    }
        }
    }
}

@Composable
fun ImageCard(
    painter : Painter,
    contentDescription : String,
    title : String,
    modifier : Modifier = Modifier
){
    Card(
        modifier=modifier.fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        elevation = CardDefaults.cardElevation(5.dp)
    ){
        Box(
            modifier = Modifier
            .height(200.dp)     
            ){
                Image(
                    painter = painter,
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Crop
                )
                Box(modifier=Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, 
                        Color.Black),
                        startY = 300f
                    )
                ))
                Box(modifier = Modifier.fillMaxSize().padding(12.dp),
                    contentAlignment = Alignment.BottomStart){
                    Text(title,style = TextStyle(color = Color.White,fontSize = 20.sp))
            }
        }
     
    }
}
