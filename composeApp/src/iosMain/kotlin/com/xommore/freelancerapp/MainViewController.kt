package com.xommore.freelancerapp

import androidx.compose.ui.window.ComposeUIViewController
import com.xommore.freelancerapp.data.DatabaseDriverFactory
import com.xommore.freelancerapp.db.FreelancerDatabase
import com.xommore.freelancerapp.ui.FreelancerApp
import com.xommore.freelancerapp.viewmodel.AuthViewModel
import com.xommore.freelancerapp.viewmodel.MainViewModel

fun MainViewController() = ComposeUIViewController {
    val driverFactory = DatabaseDriverFactory()
    val database = FreelancerDatabase(driverFactory.createDriver())
    val mainViewModel = MainViewModel(database)
    val authViewModel = AuthViewModel()

    FreelancerApp(
        mainViewModel = mainViewModel,
        authViewModel = authViewModel
    )
}
