package com.example.exoplayer

import android.content.Context
import android.content.pm.ActivityInfo
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.exoplayer.databinding.FragmentExoPlayerBinding
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import kotlinx.android.synthetic.main.fragment_exo_player.view.*

class ExoPlayerHelper(context: Context,
                      private val activity: FragmentActivity,
                      private val simpleExoPlayer: SimpleExoPlayer,
                      private var portraitView: PlayerView,
                      private var landscapeView: PlayerView) {

    private var isInFullScreen = false

    init {
        portraitView.player = simpleExoPlayer
        portraitView.requestFocus()

        val onFullscreenClickListener = View.OnClickListener {
            switchPlayer()
        }
        landscapeView.exo_fullscreen_icon.setImageDrawable(
                ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_fullscreen_close
                )
        )
        portraitView.exo_fullscreen_icon.setImageDrawable(
                ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_fullscreen_open
                )
        )
        portraitView.exo_fullscreen_icon.setOnClickListener(onFullscreenClickListener)
        landscapeView.exo_fullscreen_button.setOnClickListener(
                onFullscreenClickListener
        )
        portraitView.apply {
            btn_play_video.visibility = View.VISIBLE
            controller_container.visibility = View.GONE
            btn_play_video.setOnClickListener {
                controller_container.visibility = View.VISIBLE
                btn_play_video.visibility = View.GONE
                simpleExoPlayer.play()
            }
        }
    }

    fun prepare(url: String){
        portraitView.apply {
            this.player = simpleExoPlayer
            this.requestFocus()
        }

        MediaItem.fromUri(url).let {
            simpleExoPlayer.setMediaItem(it)
        }

        simpleExoPlayer.prepare()
    }

    private fun switchPlayer(){
        if (isInFullScreen) {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

            PlayerView.switchTargetView(simpleExoPlayer, landscapeView, portraitView)
            landscapeView.visibility = View.GONE

            portraitView.requestFocus()
            isInFullScreen = false
        } else {
            activity.window?.decorView?.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION

            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

            PlayerView.switchTargetView(simpleExoPlayer, portraitView, landscapeView)
            landscapeView.visibility = View.VISIBLE

            isInFullScreen = true
        }
    }

    fun release(){
        simpleExoPlayer.release()
    }

    fun pause() {
        simpleExoPlayer.pause()
        if (isInFullScreen) switchPlayer()
    }

}