package neth.iecal.trease

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform