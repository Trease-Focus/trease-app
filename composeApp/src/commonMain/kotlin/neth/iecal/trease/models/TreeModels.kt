package neth.iecal.trease.models

import kotlinx.serialization.Serializable

@Serializable
data class TreeResponse(
    val trees: List<String>
)

sealed class TreeUiState {
    object Loading : TreeUiState()
    data class Success(val trees: List<String>) : TreeUiState()
    data class Error(val message: String) : TreeUiState()
}