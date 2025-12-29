package neth.iecal.trease.models

enum class TimerStatus {
    Idle, Running,

    HAS_WON, // user wins to see the "you won" dialog
    POST_WIN, // The dialog is closed and user sees his grown tree

    HAS_QUIT, // user just lost and the "You lost" dialog is visible
    POST_QUIT // The dialog is closed and user sees the weathered tree

}
