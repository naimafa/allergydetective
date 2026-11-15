package com.nfar.allergydetective

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform