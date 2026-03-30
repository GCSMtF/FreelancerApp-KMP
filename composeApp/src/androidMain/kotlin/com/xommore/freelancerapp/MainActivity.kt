package com.xommore.freelancerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.xommore.freelancerapp.data.DatabaseDriverFactory
import com.xommore.freelancerapp.db.FreelancerDatabase
import com.xommore.freelancerapp.ui.FreelancerApp
import com.xommore.freelancerapp.viewmodel.AuthViewModel
import com.xommore.freelancerapp.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Database 생성
        val driverFactory = DatabaseDriverFactory(this)
        val database = FreelancerDatabase(driverFactory.createDriver())

        // ViewModel 생성
        val mainViewModel = MainViewModel(database)
        val authViewModel = AuthViewModel()

        setContent {
            FreelancerApp(
                mainViewModel = mainViewModel,
                authViewModel = authViewModel
            )
        }
    }
}