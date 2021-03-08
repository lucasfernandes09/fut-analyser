package com.example.futanalyser.model

class Competition(
    val name: String?,
    val matches: MutableList<Match>?
) {
    override fun toString(): String {
        return "$name\n" +
            "$matches\n\n"
    }
}
