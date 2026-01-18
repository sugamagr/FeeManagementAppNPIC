package com.navoditpublic.fees

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.navoditpublic.fees.presentation.components.SplashScreen
import com.navoditpublic.fees.presentation.navigation.FeesNavHost
import com.navoditpublic.fees.presentation.theme.FeesAppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FeesAppTheme {
                var showSplash by remember { mutableStateOf(true) }
                
                LaunchedEffect(Unit) {
                    delay(2000) // Show splash for 2 seconds
                    showSplash = false
                }
                
                Surface(modifier = Modifier.fillMaxSize()) {
                    AnimatedVisibility(
                        visible = showSplash,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        SplashScreen()
                    }
                    
                    AnimatedVisibility(
                        visible = !showSplash,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        FeesNavHost()
                    }
                }
            }
        }
    }
}
