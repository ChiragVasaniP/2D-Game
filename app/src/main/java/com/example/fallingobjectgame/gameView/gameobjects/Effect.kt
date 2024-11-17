package com.example.fallingobjectgame.gameView.gameobjects

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import com.example.fallingobjectgame.R
import com.example.fallingobjectgame.gameView.states.GameStateNew

data class Effect(
    private val context: Context,
    private val rows: Int,
    private val columns: Int,
    private val scaleFactor: Float = 1f,
    private val res: Int?
) {
    private val spiritList = mutableListOf<Bitmap>()
    private val getSpiritBitmap: Bitmap?
        get() {
            return AppCompatResources.getDrawable(
                context,
                res ?: R.drawable.ic_explosion_spiritsheet
            )?.toBitmap()
        }


    fun loadSprites(
    ) {
        val spriteSheet = getSpiritBitmap ?: return
        val originalFrameWidth = spriteSheet.width / columns
        val originalFrameHeight = spriteSheet.height / rows
        val scaledFrameWidth = (originalFrameWidth * scaleFactor).toInt()
        val scaledFrameHeight = (originalFrameHeight * scaleFactor).toInt()

        for (row in 0 until rows) {
            for (col in 0 until columns) {
                // Extract each frame
                val frame = Bitmap.createBitmap(
                    spriteSheet,
                    col * originalFrameWidth,
                    row * originalFrameHeight,
                    originalFrameWidth,
                    originalFrameHeight
                )

                // Scale the frame
                val scaledFrame =
                    Bitmap.createScaledBitmap(frame, scaledFrameWidth, scaledFrameHeight, true)

                // Add to the provided sprite list
                spiritList.add(scaledFrame)
            }
        }
    }

    var isEffectVisible = false
    private var currentFrameIndex = 0 // Index of the current explosion frame
    private var frameDuration = 2 // Number of draws per frame (adjust for speed)
    private var frameDurationCounter = frameDuration // Counter for frame timing
    private var yOffset: Float? = null // Current vertical position of the effect
    private val speed = 10f // Speed of upward movement

    fun playEffect(canvas: Canvas, gameState: GameStateNew, isMoving: Boolean) {
        if (!isEffectVisible || spiritList.isEmpty()) return

        // Initialize or update the yOffset for upward movement
        if (yOffset == null) {
            if (isMoving) {
                yOffset =
                    gameState.playerTop + spiritList[0].height // Start effect below the player
            } else {
                yOffset = (gameState.playerTop - spiritList[0].height)
            }
        }

        // Update yOffset only if movement is enabled
        if (isMoving) {
            yOffset = yOffset?.minus(speed) // Move upward by 'speed'
        }

        // Calculate explosion position with offset
        val explosionLeft =
            (gameState.playerLeft + (gameState.playerWidth / 2) - (spiritList[0].width / 2)).toInt()
        val explosionTop = yOffset?.toInt() ?: (gameState.playerTop - spiritList[0].height).toInt()

        // Draw the current frame
        val currentFrame = spiritList[currentFrameIndex]
        canvas.drawBitmap(currentFrame, explosionLeft.toFloat(), explosionTop.toFloat(), null)

        // Update frame timing for animation
        frameDurationCounter--
        if (frameDurationCounter <= 0) {
            frameDurationCounter = frameDuration
            currentFrameIndex++
        }

        // Check if animation should stop
        if (currentFrameIndex >= spiritList.size || (isMoving && explosionTop + spiritList[0].height < 0)) {
            isEffectVisible = false
            currentFrameIndex = 0
            yOffset = null // Reset offset
        }
    }
}