package org.example.appbbmges

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform