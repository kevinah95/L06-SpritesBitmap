package io.github.kevinah95.delete_l06

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform