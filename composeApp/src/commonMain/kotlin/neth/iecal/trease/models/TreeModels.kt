package neth.iecal.trease.models

import kotlinx.serialization.Serializable

@Serializable
data class TreeResponse(
    val entities: List<TreeData>,
    val count: Int,
)

sealed class TreeUiState {
    object Loading : TreeUiState()
    data class Success(val trees: List<TreeData>) : TreeUiState()
    data class Error(val message: String) : TreeUiState()
}