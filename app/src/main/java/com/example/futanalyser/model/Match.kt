package com.example.futanalyser.model

class Match(
    val link: String?,
    val startAt: String?,
    val homeVsAway: String?
) {
    override fun toString(): String {
        return "$homeVsAway"
    }
}
