package com.example.exoplayer

import android.Manifest
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.*
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.transition.Slide
import android.transition.TransitionManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import com.example.exoplayer.databinding.FragmentFullScreenBinding
import com.example.exoplayer.databinding.PopupWindowGoodJobBinding
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerControlView
import java.io.File

class FullScreenVideoFragment: Fragment() {
    lateinit var binding: FragmentFullScreenBinding
    lateinit var simpleExoPlayer: SimpleExoPlayer
    private var videoId = 0
    var myDownload: Long = 0L

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentFullScreenBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val position = requireArguments().getLong("position")
        videoId = requireArguments().getInt("videoId")
        binding.root.systemUiVisibility =
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_FULLSCREEN

        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        simpleExoPlayer = SimpleExoPlayer.Builder(requireContext()).build()
        simpleExoPlayer.also { binding.pvWorkout.player = it }
        simpleExoPlayer.setMediaItem(MediaItem.fromUri(
                //"http://html5videoformatconverter.com/data/images/happyfit2.mp4"))
                "https://storage.googleapis.com/exoplayer-test-media-0/play.mp3 "))
        simpleExoPlayer.prepare()
        simpleExoPlayer.seekTo(position)
        simpleExoPlayer.play()
        simpleExoPlayer.addListener(eventListener)
        binding.pvWorkout.setControllerVisibilityListener(visibility)

        //binding.ivCloseFullScreen - too
        binding.ivBack.setOnClickListener {
            val bundle = bundleOf("position" to simpleExoPlayer.currentPosition,
                    "videoId" to videoId)
            findNavController().navigate(R.id.exoPlayerFragment,bundle)
            simpleExoPlayer.stop()
        }
        binding.ivDownload.setOnClickListener{
        //downloadVideo("https://storage.googleapis.com/exoplayer-test-media-0/play.mp3")
       var request = DownloadManager.Request(
               Uri.parse("https://storage.googleapis.com/exoplayer-test-media-0/play.mp3"))
               .setTitle("File test")
               .setDescription("first")
               .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
               .setAllowedOverMetered(true)
            var dm = activity?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            myDownload = dm.enqueue(request)
        }

        var br = object: BroadcastReceiver(){
            override fun onReceive(context: Context?, intent: Intent?) {
            var id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if(id == myDownload){
                Toast.makeText(requireContext(), "Good",Toast.LENGTH_LONG).show()
            }
        }
    }
        LocalBroadcastManager.getInstance(requireContext())
                .registerReceiver(br, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    private fun statusMessage(url: String, directory: File, status: Int): String? {
        var msg = ""
        msg = when (status) {
            DownloadManager.STATUS_FAILED -> "Download has been failed, please try again"
            DownloadManager.STATUS_PAUSED -> "Paused"
            DownloadManager.STATUS_PENDING -> "Pending"
            DownloadManager.STATUS_RUNNING -> "Downloading..."
            DownloadManager.STATUS_SUCCESSFUL -> "Image downloaded successfully in $directory" + File.separator + url.substring(
                    url.lastIndexOf("/") + 1
            )
            else -> "There's nothing to download"
        }
        return msg
    }
    private val eventListener = object : Player.EventListener{
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            if(playbackState == Player.STATE_ENDED){
                val bindingPopupGoodJob = PopupWindowGoodJobBinding.inflate(layoutInflater)
                val popupWindow = PopupWindow(bindingPopupGoodJob.root,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT)
                bindingPopupGoodJob.root.systemUiVisibility =
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                                View.SYSTEM_UI_FLAG_FULLSCREEN
                popupWindow.showAsDropDown(binding.root)
                popupWindow.isFocusable = true
                popupWindow.update()
                bindingPopupGoodJob.tvSaveProof.setOnClickListener {
                    findNavController().navigate(R.id.action_fullScreenFragment_to_photoFragment)
                    popupWindow.dismiss()
                }
                bindingPopupGoodJob.root.setOnClickListener{
                    popupWindow.dismiss()
                }
            }
        }
    }
    private val visibility = PlayerControlView.VisibilityListener { visibility ->
        if (visibility == View.GONE){
            binding.clTopWorkoutVideo.visibility = View.GONE
        }
        else{
            binding.clTopWorkoutVideo.visibility = View.VISIBLE
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