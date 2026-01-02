package neth.iecal.trease.utils

import kotlin.math.pow

fun randomBiased(y: Int, bias: Double = 1.0): Int {
    require(y > 0)
    require(bias > 0)

    val r = kotlin.random.Random.nextDouble()
    val biased = r.pow(bias)

    return ((biased * y).toInt())
        .coerceIn(0, y - 1)
}
