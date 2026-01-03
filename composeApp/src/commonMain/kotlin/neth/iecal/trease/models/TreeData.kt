package neth.iecal.trease.models

import kotlinx.serialization.Serializable


@Serializable
data class TreeData(
    val id: String,
    val name: String,
    val description:String,
    val creator: String,
    val donate: String,
    val variants: Int,
    val basePrice: Int,
    val isGrowable: Boolean = true,
)