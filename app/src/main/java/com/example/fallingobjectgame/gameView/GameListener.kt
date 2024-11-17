package com.example.fallingobjectgame.gameView

import com.example.fallingobjectgame.gameView.states.GameStatus

interface GameListener {
    fun onGameStateChange(currentState: GameStatus, totalScore: Int, hasUserQuit: Boolean)
}

