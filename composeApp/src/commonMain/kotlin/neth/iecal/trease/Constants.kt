package neth.iecal.trease

object Constants {
    val isDebugBuild:Boolean = false
    val cdn = "https://trease-focus.github.io/cache-trees-cdn"
    val preOwnedTrees = listOf("tree")

    var default_quitter_text =
        "I am walking away from my promises. I admit my comfort is worth more than my future. I choose to be mediocre."
    init {
        if (isDebugBuild) {
            default_quitter_text = ""
        }
    }
}