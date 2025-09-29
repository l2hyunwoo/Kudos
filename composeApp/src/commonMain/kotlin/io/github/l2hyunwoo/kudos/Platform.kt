package io.github.l2hyunwoo.kudos

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform