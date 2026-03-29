package com.xommore.freelancerapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform