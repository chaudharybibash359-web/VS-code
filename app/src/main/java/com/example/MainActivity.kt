package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.CodeStudioViewModel
import com.example.ui.MainScreen
import com.example.ui.theme.VSCodeAppTheme

class MainActivity : ComponentActivity() {

    private val viewModel: CodeStudioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            VSCodeAppTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
