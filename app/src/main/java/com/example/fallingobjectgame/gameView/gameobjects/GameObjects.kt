package com.example.fallingobjectgame.gameView.gameobjects

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import com.example.fallingobjectgame.gameView.states.GameStateNew
import kotlin.math.abs

private fun isVisibleOnScreen(yPosition: Float, gameState: GameStateNew): Boolean {
    return yPosition >= 0 && yPosition <= gameState.gameViewHeight
}

abstract class GameObjects {
    abstract val lane: Int
    abstract var startY: Float
    abstract var bitmap: Bitmap

    var rectObject: RectF = RectF()

    abstract fun draw(
        canvas: Canvas,
        gameState: GameStateNew
    )

    abstract fun generateNewObject(gameState: GameStateNew): Boolean

    abstract fun isDestroyed(
        gameState: GameStateNew
    ): Boolean

    abstract fun isOutOfScreen(gameState: GameStateNew): Boolean

    fun readyToPrintBoxInDebug(gameState: GameStateNew, canvas: Canvas) {
        if (gameState.config.showOverlayBox) {
            canvas.drawRect(rectObject, gameState.boxPaint) // Draw the rectangle
            canvas.drawRect(gameState.playerReact, gameState.boxPaint) // Draw the rectangle
        }
    }
}

data class ObstacleNew(
    override var lane: Int,
    override var startY: Float,
    override var bitmap: Bitmap,
    var isCollusionHappen: Boolean = false
) :
    GameObjects() {


    override fun draw(
        canvas: Canvas,
        gameState: GameStateNew
    ) {

        val obstacleX =
            (lane * gameState.columnWidth) + (gameState.columnWidth / 2) - (gameState.playerWidth / 2)
        val getCurrentGameLevel = abs(gameState.totalScore / 10)
        startY =
            startY.plus(gameState.currentGameSpeed.minus((gameState.config.initialGameSpeed + getCurrentGameLevel)))
        if (isVisibleOnScreen(startY, gameState).not()) return
        canvas.save()
        val reducedWidth = (gameState.playerWidth * gameState.config.ObstaclesSize.first).toInt()
        val reducedHeight = (gameState.playerHeight * gameState.config.ObstaclesSize.second).toInt()
        val centeredX =
            obstacleX + (gameState.playerWidth - reducedWidth) / 2 // Center horizontally
        val centeredY = startY - reducedHeight // Center vertically above the Y position
        rectObject.left = centeredX.toFloat()
        rectObject.top = centeredY
        rectObject.right = centeredX.toFloat() + reducedWidth.toFloat()
        rectObject.bottom = centeredY + reducedHeight.toFloat()

        readyToPrintBoxInDebug(gameState, canvas)

        canvas.drawBitmap(bitmap, centeredX.toFloat(), centeredY, null)
        canvas.restore()
    }

    override fun generateNewObject(gameState: GameStateNew): Boolean {
        if (startY >= gameState.gameViewHeight.minus(gameState.gameViewHeight / 3)) {
            if (gameState.needToGenerateNewObject) {
                gameState.needToGenerateNewObject = false
                return true
            }
        }
        return false
    }


    override fun isDestroyed(
        gameState: GameStateNew
    ): Boolean {
        if (lane == gameState.playerColumnPosition && !isCollusionHappen) {
            return rectObject.intersect(gameState.playerReact)
        }
        return false
    }

    override fun isOutOfScreen(gameState: GameStateNew): Boolean {
        if (startY > (gameState.gameViewHeight + (gameState.playerHeight))) {
            return true
        }
        return false
    }
}

data class Coin(
    override var lane: Int,
    override var startY: Float,
    override var bitmap: Bitmap,
    var isCollusionHappen: Boolean = false
) :
    GameObjects() {

    override fun draw(
        canvas: Canvas,
        gameState: GameStateNew
    ) {

        val objectX =
            (lane * gameState.columnWidth) + (gameState.columnWidth / 2) - (gameState.playerWidth / 2)
        val getCurrentGameLevel = abs(gameState.totalScore / 10)
        startY =
            startY.plus(gameState.currentGameSpeed.minus((gameState.config.initialGameSpeed + getCurrentGameLevel)))
        if (isVisibleOnScreen(startY, gameState).not()) return
        val reduceFactor = 0.6f // Reduce to 50%
        val reducedWidth = (gameState.playerWidth * gameState.config.coinSize.first).toInt()
        val reducedHeight = (gameState.playerHeight * gameState.config.coinSize.second).toInt()
        // Center the bitmap
        val centeredX = objectX + (gameState.playerWidth - reducedWidth) / 2 // Center horizontally
        val centeredY = startY - reducedHeight // Center vertically above the Y position

        rectObject.left = centeredX.toFloat()
        rectObject.top = centeredY
        rectObject.right = centeredX.toFloat() + reducedWidth.toFloat()
        rectObject.bottom = centeredY + reducedHeight.toFloat()
        // Draw the scaled bitmap on the canvas
        readyToPrintBoxInDebug(gameState, canvas)
        canvas.drawBitmap(bitmap, centeredX.toFloat(), centeredY, null)
    }

    override fun generateNewObject(gameState: GameStateNew): Boolean {
        return false
    }


    override fun isDestroyed(
        gameState: GameStateNew
    ): Boolean {
        if (lane == gameState.playerColumnPosition) {
            return rectObject.intersect(gameState.playerReact)
        }

        return false
    }

    override fun isOutOfScreen(gameState: GameStateNew): Boolean {
        if (startY > gameState.gameViewHeight) {
            return true
        }
        return false
    }

}