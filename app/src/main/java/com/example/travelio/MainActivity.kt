package com.example.travelio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.travelio.ui.AddTripScreen
import com.example.travelio.ui.HomeScreen
import com.example.travelio.ui.theme.TravelioTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TravelioTheme {
                HomeScreen()
            }
        }
    }
}
