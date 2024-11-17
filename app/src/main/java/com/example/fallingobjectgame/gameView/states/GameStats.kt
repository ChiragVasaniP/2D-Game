package com.example.fallingobjectgame.gameView.states

import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.example.fallingobjectgame.gameView.gameUtils.toPx


data class GameStateNew(
    var config: GameConfig = GameConfig(),
    var playerWidth: Int = 0,
    var playerHeight: Int = 0,
    var columnWidth: Int = 0,
    var currentLevelSpeed: Int = 0,
    var playerLives: Int = 10000,
    var totalScore: Int = 0,
    var totalHitObstacles: Int = 0,
    var playerColumnPosition: Int = 0,
    var currentPlayerPosition: Position = Position(),
    var currentPlayerRotation: Float = 0f,
    var gameViewWidth: Int = 0,
    var gameViewHeight: Int = 0,
    var currentGameSpeed: Float = config.initialGameSpeed,
    var currentGameSpeedFactor: Float = 1.5f,
    var currentGameStatus: GameStatus = GameStatus.DEFAULT,
    var currentGameScoreFactor: Int = -1,
    var needToGenerateNewObject: Boolean = false,
    var coinSize: Pair<Float, Float> = Pair(0.7f, 0.7f),
    var colledibleSize: Pair<Float, Float> = Pair(0.7f, 0.7f),

    val boxPaint: Paint = Paint().apply {
        color = Color.parseColor("#4D000000") // Set color to blue
        style = Paint.Style.FILL // Set style to fill
    }
) {


    fun setGameConfig(gameConfig: GameConfig) {
        config = gameConfig
        currentGameSpeed = config.initialGameSpeed
    }

    val playerLeft
        get() =
            (currentPlayerPosition.x) + (columnWidth / 2) - (playerWidth / 2)

    val playerTop
        get() =
            (gameViewHeight - (playerHeight + TOP_FALLING_PADDING)).toFloat()

    val playerReact
        get() = RectF(
            /* left = */ playerLeft.toFloat(),
            /* top = */ playerTop.toFloat(),
            /* right = */ (playerLeft + playerWidth).toFloat(),
            /* bottom = */ (playerTop + playerHeight).toFloat()
        )
}


val TOP_FALLING_PADDING = 90.toPx()


