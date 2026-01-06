package neth.iecal.trease.data

data class DeepFocus (
    var exceptionApps: HashSet<String> = hashSetOf(),
    var isRunning: Boolean = false,
    var duration: Long = 0L
)