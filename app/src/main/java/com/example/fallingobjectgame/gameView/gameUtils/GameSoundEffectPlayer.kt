package com.example.fallingobjectgame.gameView.gameUtils


import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.example.fallingobjectgame.R


class GameSoundEffectPlayer(context: Context?) {
    private val mSoundPool: SoundPool
    private val coinReceive: Int
    private val coinExplosion: Int

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        mSoundPool = SoundPool.Builder()
            .setAudioAttributes(audioAttributes)
            .setMaxStreams(3)
            .build()

        coinReceive = mSoundPool.load(context, R.raw.music_coin, 2)
        coinExplosion = mSoundPool.load(context, R.raw.explosion, 2)
    }

    fun playCongratulationSound() {
        mSoundPool.play(coinReceive, 1f, 1f, 5, 0, 1f)
    }

    fun playExplosionSound() {
        mSoundPool.play(coinExplosion, 1f, 1f, 5, 0, 1f)
    }


    fun releaseSound() {
        mSoundPool.release()
    }
}