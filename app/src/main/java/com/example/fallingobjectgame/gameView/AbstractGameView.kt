package com.example.fallingobjectgame.gameView

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.ContextCompat
import androidx.core.graphics.scale
import com.example.fallingobjectgame.R
import com.example.fallingobjectgame.gameView.gameUtils.GameSoundEffectPlayer
import com.example.fallingobjectgame.gameView.gameobjects.Coin
import com.example.fallingobjectgame.gameView.gameobjects.Effect
import com.example.fallingobjectgame.gameView.states.GameConfig
import com.example.fallingobjectgame.gameView.states.GameStateNew
import com.example.fallingobjectgame.gameView.states.GameStatus
import com.example.fallingobjectgame.gameView.gameobjects.ObstacleNew
import com.example.fallingobjectgame.gameView.states.TOP_FALLING_PADDING
import com.example.fallingobjectgame.gameView.gameUtils.getDrawable
import com.example.fallingobjectgame.gameView.gameobjects.GameObjects
import com.example.fallingobjectgame.gameView.gameobjects.Player
import kotlinx.coroutines.Job
import java.util.LinkedList
import kotlin.math.abs

abstract class AbstractGameView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    protected var gameThreadJob: Job? = null

    protected val canvasPaint by lazy {
        Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER) }
    }
    protected val gameSoundPlayer by lazy { GameSoundEffectPlayer(context) }

    protected val explosion = Effect(context, 4, 4, 1.20f, R.drawable.ic_explosion_spiritsheet)
    protected val coinCollect = Effect(context, 4, 4, 0.40f, R.drawable.ic_coin_spirit)
    protected val player = Player(context, 1, 8, 8.5f, R.drawable.ic_plater_walk_up)
    private val maxRotationAngle = 15f

    private var movementAnimator: ValueAnimator? = null
    private val movementQueue = LinkedList<Int>()

    protected var myGameObjects = ArrayList<GameObjects>()
    private var characterObjectAsset =
        ContextCompat.getDrawable(context, R.drawable.ic_launcher_background)



    private val collectibleObjectAsset by lazy {
        listOfNotNull(
            getDrawable(R.drawable.ic_launcher_background).toBitmap(),
        )
    }
    private val callableObjectAsset by lazy {
        listOfNotNull(
            getDrawable(R.drawable.ic_launcher_foreground).toBitmap(),
            getDrawable(R.drawable.ic_launcher_foreground).toBitmap(),
        )
    }

    protected val stringScore by lazy { "Score" }
    protected val stringSpeed by lazy { "Speed" }
    protected val stringDamage by lazy { "Damage" }
    protected val stringAvailableLives by lazy { "Lives" }


    private var explosionDrawableTime = 10


    var gameState = GameStateNew()
    var gameConfig = GameConfig()

    protected var listener: GameListener? = null

    abstract fun gameThread()

    fun setGameListener(listener: GameListener): AbstractGameView {
        this.listener = listener
        return this
    }

    fun setGameConfig(builderAction: GameConfig.() -> Unit) {
        gameConfig.apply(builderAction)
        gameState.setGameConfig(gameConfig)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        with(gameState) {
            gameViewWidth = w
            gameViewHeight = h
            playerWidth = w / 4
            playerHeight = playerWidth + 10
            columnWidth = w / 4
        }
        explosion.loadSprites()
        coinCollect.loadSprites()
        player.loadSprites()
        // Calculate robot width and height only once when the size changes
    }

    fun obstacleIterator(
        iterator: IntIterator,
        laneList: MutableList<Int>,
        callback: (MutableList<Int>) -> Unit
    ) {
        if (iterator.hasNext()) {
            val objectNo = iterator.next()
            Log.e("Iterator", "NextItemTriggered ${objectNo}")
            generateObstacles(laneList) {
                Log.e("Iterator", "MoveToNext")
                obstacleIterator(iterator, it, callback)
            }
        } else {
            callback.invoke(laneList)
        }
    }

    fun generateCollectibles(lanes: MutableList<Int>) {
        Log.e("Generate", "Coint:Triggered")
        if (lanes.isEmpty()) {
            return
        } else {
            val lane = lanes.random()
            lanes.remove(lane)
            val reducedWidth = (gameState.playerWidth * gameState.config.coinSize.first).toInt()
            val reducedHeight = (gameState.playerHeight * gameState.config.coinSize.second).toInt()
            myGameObjects.add(
                Coin(
                    lane = lane,
                    startY = gameState.currentGameSpeed,
                    bitmap = collectibleObjectAsset.random().scale(reducedWidth, reducedHeight),
                )
            )
        }

    }

    private fun generateObstacles(
        seedRange: MutableList<Int>,
        callback: (MutableList<Int>) -> Unit
    ) {
        if (seedRange.isEmpty()) callback.invoke(seedRange)
        val firstObjectLane = (seedRange).random()
        seedRange.remove(firstObjectLane)
        val reducedWidth = (gameState.playerWidth * gameState.config.ObstaclesSize.first).toInt()
        val reducedHeight = (gameState.playerHeight * gameState.config.ObstaclesSize.second).toInt()
        myGameObjects.add(
            ObstacleNew(
                lane = firstObjectLane,
                startY = 0f,
                isCollusionHappen = false,
                bitmap = callableObjectAsset.random().scale(reducedWidth, reducedHeight)
            )
        )
        //  }
        callback.invoke(seedRange)

    }

    protected fun drawPlayer(canvas: Canvas) {

        val playerWidth = (gameState.playerWidth)
        val aspectRatio = (characterObjectAsset?.intrinsicHeight
            ?: 1).toFloat() / (characterObjectAsset?.intrinsicWidth ?: 1)

        val playerHeight = (playerWidth * aspectRatio).toInt()

        if (gameState.playerHeight != playerHeight) {
            gameState.playerHeight = playerHeight
        }

        canvas.save()
        canvas.translate(gameState.playerLeft, gameState.playerTop)

        if (gameState.config.enableLaneChangeRotation) {
            canvas.rotate(
                gameState.currentPlayerRotation,
                (playerWidth / 2).toFloat(),
                (playerHeight / 2).toFloat()
            )
        }

        characterObjectAsset?.setBounds(
            0,
            0,
            playerWidth,
            playerHeight - characterGrowingFactor.toInt()
        )
        characterObjectAsset?.draw(canvas)
        player.isEffectVisible=true
        player.playEffect(canvas, gameState, true)
        canvas.restore()

        manageCharacterGrowing()
    }

    private var characterGrowingFactor = 15.0
    private var isCharacterGrowing = false
    private fun manageCharacterGrowing() {
        if (gameState.currentGameStatus == GameStatus.RUNNING) {
            if (isCharacterGrowing) {
                characterGrowingFactor -= 0.5
                if (characterGrowingFactor < 0) {
                    isCharacterGrowing = false
                }
            } else {
                characterGrowingFactor += 0.5
                if (characterGrowingFactor > 15) {
                    isCharacterGrowing = true
                }
            }
        }
    }

    protected fun explosionEffect(canvas: Canvas) {
        explosion.playEffect(canvas,gameState,false)
    }


    protected fun drawLaneSeparators(canvas: Canvas) {
        if (gameState.config.showLineSeparator.not()) return
        val gradient = LinearGradient(
            0f, 0f, 0f, gameState.gameViewHeight.toFloat(),
            gameState.config.lineSeparatorStartColor,
            gameState.config.lineSeparatorEndColor,
            Shader.TileMode.CLAMP
        )

        canvasPaint.color = Color.GRAY // Fallback color if needed
        canvasPaint.strokeWidth = 3f // Width of the separators
        canvasPaint.pathEffect =
            DashPathEffect(floatArrayOf(10f, 10f), 0f) // Define dash and gap lengths

        canvasPaint.shader = gradient

        val laneWidth = gameState.gameViewWidth / 4
        for (i in 1 until 4) { // Draw separators between 4 lanes
            val x = (i * laneWidth).toFloat() // Centered between lanes
            canvas.drawLine(
                x,
                TOP_FALLING_PADDING.toFloat(),
                x,
                (gameState.gameViewHeight / 1.2).toFloat(),
                canvasPaint
            )
        }
    }

    protected fun showCoinEffect(canvas: Canvas) {
        coinCollect.playEffect(canvas = canvas, gameState = gameState,true)
    }




    fun moveCharacterToLeft() {
        if (gameState.playerColumnPosition > 0) {


            if (gameState.config.enableSmoothMovement) {
                val targetLane = gameState.playerColumnPosition - 1
                movementQueue.add(targetLane)
                processMovementQueue()
            } else {
                gameState.playerColumnPosition--
                gameState.currentPlayerPosition.x =
                    (gameState.playerColumnPosition * gameState.columnWidth).toFloat()
            }
        }
    }

    fun moveCharacterToRight() {
        if (gameState.playerColumnPosition < 3) { // Changed to accommodate 4 lanes
            if (gameState.config.enableSmoothMovement) {
                val targetLane = gameState.playerColumnPosition + 1
                movementQueue.add(targetLane)
                processMovementQueue()
            } else {
                gameState.playerColumnPosition++
                gameState.currentPlayerPosition.x =
                    (gameState.playerColumnPosition * gameState.columnWidth).toFloat()
            }
        }
    }


    private fun processMovementQueue() {
        if (movementAnimator?.isRunning == true || movementQueue.isEmpty()) {
            return
        }

        val targetLane = movementQueue.poll() ?: 0

        val startX = gameState.currentPlayerPosition.x
        val endX = (targetLane * gameState.columnWidth).toFloat()

        val angle =
            if (targetLane < gameState.playerColumnPosition) -maxRotationAngle else maxRotationAngle

        movementAnimator = ValueAnimator.ofFloat(startX, endX).apply {
            duration = 200L
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animator ->
                val currentX = animator.animatedValue as Float
                gameState.currentPlayerPosition.x = currentX

                if (gameState.config.enableLaneChangeRotation) {
                    val deltaX = abs(currentX - startX) / abs(endX - startX)
                    gameState.currentPlayerRotation = angle * (1 - abs(2 * deltaX - 1))
                }
            }

            addListener(object : AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}

                override fun onAnimationEnd(animation: Animator) {
                    movementAnimator = null
                    processMovementQueue()
                }

                override fun onAnimationCancel(animation: Animator) {}

                override fun onAnimationRepeat(animation: Animator) {}
            })

            start()
        }

        gameState.playerColumnPosition = targetLane
    }

    fun clearGameView() {
        myGameObjects.clear()
    }

    fun startGame() {
        if (gameState.currentGameStatus != GameStatus.RUNNING) {
            gameState.currentGameStatus = GameStatus.RUNNING
            gameThread()
            listener?.onGameStateChange(gameState.currentGameStatus, gameState.totalScore, false)
        }

    }

    fun pauseGame() {
        gameState.currentGameStatus = GameStatus.PAUSE
        gameThreadJob?.cancel()
    }

    fun resumeGame() {
        if (gameState.currentGameStatus != GameStatus.RUNNING) {
            gameState.currentGameStatus = GameStatus.RUNNING
            gameThread()
        }
    }

    protected fun endGame(hasUserQuit: Boolean) {
        // this.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        gameThreadJob?.cancel()
        gameState.currentGameStatus = GameStatus.ENDED
        listener?.onGameStateChange(gameState.currentGameStatus, gameState.totalScore, hasUserQuit)
        clearGameView()
    }

    private fun Drawable.toBitmap(): Bitmap {
        if (this is BitmapDrawable) {
            return this.bitmap
        }
        val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        setBounds(0, 0, canvas.width, canvas.height)
        draw(canvas)
        return bitmap
    }

    /*    private fun loadExplosionSprites(spriteSheet: Bitmap, rows: Int, columns: Int) {
            val frameWidth = spriteSheet.width / columns
            val frameHeight = spriteSheet.height / rows

            for (row in 0 until rows) {
                for (col in 0 until columns) {
                    val frame = Bitmap.createBitmap(
                        spriteSheet,
                        col * frameWidth,
                        row * frameHeight,
                        frameWidth,
                        frameHeight
                    )
                    (fireSprites as MutableList).add(frame)
                }
            }
        }*/


    /*    private fun loadSpriteSheet(context: Context, resourceId: Int, rows: Int, columns: Int) {
            // Load the sprite sheet bitmap
            val spriteSheet = BitmapFactory.decodeResource(context.resources, resourceId)

            // Calculate dimensions of each sprite
            val spriteWidth = spriteSheet.width / columns
            val spriteHeight = spriteSheet.height / rows

            // Extract sprites using a nested loop
            for (row in 0 until rows) {
                for (col in 0 until columns) {
                    val x = col * spriteWidth
                    val y = row * spriteHeight
                    val sprite = Bitmap.createBitmap(spriteSheet, x, y, spriteWidth, spriteHeight)
                    fireSprites.add(sprite)
                }
            }
        }*/
}