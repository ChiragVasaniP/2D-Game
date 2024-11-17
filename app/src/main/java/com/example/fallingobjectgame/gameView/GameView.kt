package com.example.fallingobjectgame.gameView

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import com.example.fallingobjectgame.gameView.gameobjects.Coin
import com.example.fallingobjectgame.gameView.states.GameStatus
import com.example.fallingobjectgame.gameView.gameobjects.ObstacleNew
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

class GameView(context: Context, attrs: AttributeSet?) : AbstractGameView(context, attrs) {

    private var objectGenerationOffset = 0
    private var previousTime: Long = 0
    private var isCanvasDrawing = false
    private var lastUpdateTime = System.currentTimeMillis()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Draw lane separators (pipes)
        canvasPaint.style = Paint.Style.FILL
//        drawLaneSeparators(canvas)
        drawPlayer(canvas)

        if (gameState.currentGameStatus == GameStatus.RUNNING) {
            isCanvasDrawing = true

            objectGenerationOffset =
                (gameState.config.initialGameSpeed + getCurrentGameLevel()).roundToInt()

            // Generate objects
            if (myGameObjects.isEmpty()) generateInitialObstacles {
                generateCollectibles(it)
            }

            synchronized(myGameObjects) {
                val copyOfObstacleList = myGameObjects.toMutableList()
                copyOfObstacleList.forEachIndexed { _, objectsCopy ->
                    objectsCopy.draw(
                        canvas = canvas, gameState = gameState
                    )
                    if (objectsCopy.generateNewObject(gameState = gameState)) {
//                        generateInitialObstacles {
//                            generateCollectibles(it)
//                        }
                    }
                    if (objectsCopy.isDestroyed(gameState)) {
                        if (objectsCopy is ObstacleNew) {
                            gameState.playerLives--
                            gameState.totalHitObstacles += 1
                            performHapticFeedback(1)
                            myGameObjects.remove(objectsCopy)
                            explosion.isEffectVisible = true
                            if (gameState.config.isSoundMute.not()) {
                                gameSoundPlayer.playExplosionSound()
                            }

                            if (gameState.playerLives <= 0) {
                                endGame(hasUserQuit = false)
                            }
                        }

                        if (objectsCopy is Coin) {
                            gameState.totalScore += 1
                            myGameObjects.remove(objectsCopy)
                            coinCollect.isEffectVisible = true

                       /*     triggerCoinCollectEffect(
                                gameState.playerLeft.toInt(), gameState.playerTop.toInt()
                            )*/
                            if (gameState.config.isSoundMute.not()) {
                                gameSoundPlayer.playCongratulationSound()
                            }

                        }

                    }
                    if (objectsCopy.isOutOfScreen(gameState)) {
                        myGameObjects.remove(objectsCopy)
                        gameState.needToGenerateNewObject = true
                    }
                }
            }
            showCoinEffect(canvas)
            explosionEffect(canvas)
            isCanvasDrawing = false
        }
        drawLaneSeparators(canvas)
        drawDebugTextScore(canvas)
        invalidate()
    }

    private fun drawDebugTextScore(canvas: Canvas) {
        canvasPaint.color = Color.WHITE
        canvasPaint.textSize = 30f
        if (true) {
            canvas.drawText("$stringScore : ${gameState.totalScore}", 20f, 70f, canvasPaint)
            canvas.drawText("$stringSpeed : ${gameState.currentGameSpeed}", 20f, 100f, canvasPaint)
            canvas.drawText(
                "$stringAvailableLives : ${gameState.playerLives}",
                20f,
                130f,
                canvasPaint
            )
            canvas.drawText("Current Level : ${getCurrentGameLevel()}", 20f, 160f, canvasPaint)
        }
    }

    private fun generateInitialObstacles(callback: (list: MutableList<Int>) -> Unit) {
        Log.e("Generate", "Object:Triggered ${getCurrentGameLevel()}")

        val noOfObstaclesGenerator = when (getCurrentGameLevel()) {
            in 0..2 -> {
                (1..<gameState.config.NO_OF_COLUMN).random()
            }

            in (3..4) -> {
                (2..<gameState.config.NO_OF_COLUMN).random()
            }

            else -> {
                3
            }
        }
        val iterator = (1..noOfObstaclesGenerator).iterator()
        val laneList = mutableListOf<Int>().apply {
            for (i in 0 until gameState.config.NO_OF_COLUMN) {
                add(i)
            }
        }

        Log.e("Iterator", "Range ${noOfObstaclesGenerator}")
        obstacleIterator(iterator, laneList, callback)
    }

    /**
     * Game thread
     */
    override fun gameThread() {
        if (gameState.currentGameStatus != GameStatus.RUNNING) return
        previousTime = System.nanoTime()
        val refreshRate: Float = 120f
        val frameTime = 1e9f / refreshRate // Frame time in nanoseconds
        gameThreadJob = CoroutineScope(Dispatchers.Default).launch {
            while (gameState.currentGameStatus == GameStatus.RUNNING) {

                val currentTime = System.nanoTime()
                val deltaTime = currentTime - previousTime

                if (deltaTime >= frameTime) {
                    // Update the game state
                    updateGameSpeed() // convert deltaTime to seconds

                    // Render the game frame

                    Log.e("GameFrame", "Rendered")
                    // Set previous time for next loop iteration
                    previousTime = currentTime
                }
            }
        }
        val currentTime = System.currentTimeMillis()
        val deltaTime = (currentTime - lastUpdateTime) / 1000f
        invalidate()
    }

    private fun getCurrentGameLevel(): Int {
        return abs(gameState.totalScore / gameState.config.coinsNeededForSpeedIncrease)
    }

    private fun updateGameSpeed() {
        if (gameState.currentGameScoreFactor != getCurrentGameLevel()) {
            gameState.currentGameScoreFactor = getCurrentGameLevel()
            gameState.currentGameSpeed = if (gameState.currentGameScoreFactor > 0) {
                gameState.currentGameSpeedFactor =
                    gameState.currentGameSpeedFactor.plus(gameState.config.incrementFactor)
                gameState.config.initialGameSpeed.times(gameState.currentGameSpeedFactor)
            } else {
                gameState.config.initialGameSpeed.times(gameState.currentGameSpeedFactor)
            }
            if (gameState.currentGameSpeed > 50f) {
                gameState.currentGameSpeed = 50f
            }
        }
    }
}




