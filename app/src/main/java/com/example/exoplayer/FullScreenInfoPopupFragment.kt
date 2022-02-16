package com.example.exoplayer

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.example.exoplayer.databinding.FragmentFullScreenInfoPopupBinding
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerControlView

class FullScreenInfoPopupFragment : Fragment(){
    lateinit var binding: FragmentFullScreenInfoPopupBinding
    lateinit var simpleExoPlayer: SimpleExoPlayer

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentFullScreenInfoPopupBinding.inflate(layoutInflater)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val position = requireArguments().getLong("position")
        val videoMP4 = requireArguments().getString("videoMP4")!!
        binding.root.systemUiVisibility =
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_FULLSCREEN
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        simpleExoPlayer = SimpleExoPlayer.Builder(requireContext()).build()
        binding.pvWorkout.player = simpleExoPlayer
        simpleExoPlayer.setMediaItem(MediaItem.fromUri(videoMP4))
        simpleExoPlayer.prepare()
        simpleExoPlayer.seekTo(position)
        simpleExoPlayer.play()
        binding.pvWorkout.setControllerVisibilityListener(visibility)
        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
            simpleExoPlayer.stop()
        }
    }
    private val visibility = PlayerControlView.VisibilityListener { visibility ->
        if (visibility == View.GONE){
            binding.ivBack.visibility = View.GONE
        }
        else{
            binding.ivBack.visibility = View.VISIBLE
        }
    }

    override fun onPause() {
        super.onPause()
        simpleExoPlayer.pause()
    }
    override fun onStop() {
        super.onStop()
        simpleExoPlayer.stop()
    }
}
