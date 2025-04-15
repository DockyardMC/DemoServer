package io.github.dockyard.demo.utils

fun easeInOutQuartWave(time: Float): Float {
    val t = time.coerceIn(0f, 1f)

    val ease = if (t < 0.5f) {
        8f * t * t * t * t
    } else {
        val f = t - 1f
        1f - 8f * f * f * f * f
    }

    return ease * 2f - 1f
}