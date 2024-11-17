package com.example.fallingobjectgame

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fallingobjectgame.gameView.GameView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val mGameView = GameView(this, null)

        mGameView?.setGameConfig {
            showOverlayBox = false
            showLineSeparator = true
            lineSeparatorStartColor = Color.parseColor("#838D9C")
            lineSeparatorEndColor = Color.parseColor("#101317")
            initialPlayerLives = 1
            coinsNeededForSpeedIncrease = 8
            enableSmoothMovement = true
            enableLaneChangeRotation = false
            isSoundMute = false
            initialGameSpeed = 10f
            incrementFactor = 0.15f
            coinSize = Pair(0.7f, 0.7f)
            ObstaclesSize = Pair(0.7f, 0.7f)
        }
        findViewById<FrameLayout>(R.id.gameView).addView(mGameView)
        findViewById<Button>(R.id.Left).setOnClickListener {
            mGameView.moveCharacterToLeft()
        }
        findViewById<Button>(R.id.Right).setOnClickListener {
            mGameView.moveCharacterToRight()
        }
        mGameView.startGame()
    }
}