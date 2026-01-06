package neth.iecal.trease.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class FocusStats @OptIn(ExperimentalUuidApi::class) constructor(
    var duration:Long, // in seconds
    val treeId: String,
    val isFailed: Boolean,
    val failureTree: String = "weathered_0",
    val id:String = Uuid.generateV7().toString(),
    val completedOn: Long = kotlin.time.Clock.System.now().toEpochMilliseconds(),
    val treeSeed: Int = 0,

    )