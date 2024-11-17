package com.example.fallingobjectgame.gameView.states

import android.graphics.Color

data class GameConfig(
    var initialPlayerLives: Int = 1,
    var showOverlayBox: Boolean = false,
    var showLineSeparator: Boolean = false,
    var enableSmoothMovement: Boolean = false,
    var enableLaneChangeRotation: Boolean = false,
    var lineSeparatorEndColor: Int = Color.WHITE,
    var lineSeparatorStartColor: Int = Color.WHITE,
    var coinsNeededForSpeedIncrease: Int = 10,
    var isSoundMute: Boolean = false,
    var initialGameSpeed: Float = 12f,
    var incrementFactor: Float = 0.18f,
    var playerCollisionBoxPadding: PaddingValue = PaddingValue(),
    var coinSize: Pair<Float, Float> = Pair(0.7f, 0.7f),
    var ObstaclesSize: Pair<Float, Float> = Pair(0.7f, 0.7f),
)