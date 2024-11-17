package com.example.fallingobjectgame.gameView.states

data class PaddingValue(
    val top: Int = 0,
    val bottom: Int = 0,
    val right: Int = 0,
    val left: Int = 0
) {
    constructor(vertical: Int, horizontal: Int) : this(
        top = vertical,
        bottom = vertical,
        right = horizontal,
        left = horizontal
    )
}