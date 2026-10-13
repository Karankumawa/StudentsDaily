package com.example.studentsdaily

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import com.example.studentsdaily.ui.navigation.StudentsDailyMainApp
import com.example.studentsdaily.ui.theme.StudentsDailyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StudentsDailyTheme {
                Surface {
                    StudentsDailyMainApp()
                }
            }
        }
    }
}
