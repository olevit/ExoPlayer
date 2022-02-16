package com.example.exoplayer

import android.app.DownloadManager
import android.content.Context
import android.content.pm.ActivityInfo
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.transition.Slide
import android.transition.TransitionManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.exoplayer.databinding.PopupWindowBinding
import com.example.exoplayer.databinding.PopupWindowGoodJobBinding
import com.example.exoplayer.databinding.VideoBinding
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerControlView
import kotlinx.coroutines.*


class VideoFragment:Fragment() {

    lateinit var binding: VideoBinding
    lateinit var simpleExoPlayer: SimpleExoPlayer
    private var videoId = 0
    private var position = 0L
    private var durationVideo = 0
    var favorite = false
    private var videoInfo = "https://storage.googleapis.com/exoplayer-test-media-0/play.mp3"
    lateinit var job: Job
    lateinit var bindingPopupWindowGoodJob: PopupWindowGoodJobBinding
    lateinit var bindingPopupWindow: PopupWindowBinding


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = VideoBinding.inflate(layoutInflater)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding.ivInfo.setOnClickListener {
            val bindingPopup = PopupWindowBinding.inflate(layoutInflater)
            val popupWindow = PopupWindow(bindingPopup.root,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT)
            val slideIn = Slide()
            slideIn.slideEdge = Gravity.END
            popupWindow.enterTransition = slideIn
            val slideOut = Slide()
            slideOut.slideEdge = Gravity.END
            popupWindow.exitTransition = slideOut
            TransitionManager.beginDelayedTransition(binding.root)
            popupWindow.showAtLocation(binding.root, Gravity.END, 0, 0)
            popupWindow.isFocusable = true
            popupWindow.update()
            val simpleExoPlayerPopup = SimpleExoPlayer.Builder(requireContext()).build()
            bindingPopup.pvVideo.player = simpleExoPlayerPopup
            simpleExoPlayerPopup.setMediaItem(MediaItem.fromUri(videoInfo))
            simpleExoPlayerPopup.prepare()
            val closePopupWindow = View.OnClickListener {
                popupWindow.dismiss()
                simpleExoPlayerPopup.stop()
            }
            bindingPopup.ivOpenFullScreen.setOnClickListener {
                val bundle = bundleOf("videoMP4" to videoInfo, "position"
                        to simpleExoPlayerPopup.currentPosition)
                        findNavController().navigate(R.id.fullScreenInfoPopupFragment, bundle)
                        simpleExoPlayerPopup.stop()
            }
            bindingPopup.pvVideo.setControllerVisibilityListener { visibility ->
                if (visibility == View.GONE) {
                    bindingPopup.ivOpenFullScreen.visibility = View.GONE
                } else {
                    bindingPopup.ivOpenFullScreen.visibility = View.VISIBLE
                }
            }
                bindingPopup.ivClose.setOnClickListener(closePopupWindow)
                bindingPopup.ivInfoClose.setOnClickListener(closePopupWindow)
        }

        /*if(favorite){
            binding.tv.text = "true"
        }
        else{
            binding.tv.text =" false"
        }*/
        binding.tv.text = "0%"
        binding.tv.setOnClickListener {
            if(favorite) {
                binding.tv.text = "false"
                favorite = false
            }else {
                favorite = true
                binding.tv.text = "true"
            }
        }
        binding.tvCountPeople.setOnClickListener{
            downloadImage("http://html5videoformatconverter.com/data/images/happyfit2.mp4")
        }
        simpleExoPlayer = SimpleExoPlayer.Builder(requireContext()).build()
        binding.player.player = simpleExoPlayer
        simpleExoPlayer.setMediaItem(MediaItem.fromUri(
                //"http://html5videoformatconverter.com/data/images/happyfit2.mp4"))
                "https://storage.googleapis.com/exoplayer-test-media-0/play.mp3 "))
        simpleExoPlayer.prepare()

        binding.ivStartVideo.setOnClickListener {
            binding.clImageWorkout.visibility = View.GONE
            binding.clPlayerVideo.visibility = View.VISIBLE
            if(arguments != null){
                position = requireArguments().getLong("position")
                videoId = requireArguments().getInt("videoId")
            }
            binding.pbWorkoutProgress.max = durationVideo
            binding.pbWorkoutProgress.progress = position.toInt()
            simpleExoPlayer.seekTo(position)
            simpleExoPlayer.play()
            coroutineScopeForProgressBar()
        }
        binding.imageOpenFullScreen.setOnClickListener {
            val bundle = bundleOf("videoId" to videoId, "position"
                    to simpleExoPlayer.currentPosition)
            findNavController().navigate(R.id.fullScreenFragment, bundle)
            simpleExoPlayer.stop()
        }
        simpleExoPlayer.addListener(eventListener)
        binding.player.setControllerVisibilityListener(visibility)
    }
    private fun downloadImage(url: String) {
        val downloadManager = activity?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadUri = Uri.parse(url)
        val request = DownloadManager.Request(downloadUri).apply {
            setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI
                    or DownloadManager.Request.NETWORK_MOBILE)
                    .setAllowedOverRoaming(false)
                    .setTitle(" ")
                    .setDescription(" ")
        }

        val downloadId = downloadManager.enqueue(request)
        val query = DownloadManager.Query().setFilterById(downloadId)
        var downloading = true
        while (downloading) {
            val cursor: Cursor = downloadManager.query(query)
            cursor.moveToFirst()
            if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                    == DownloadManager.STATUS_SUCCESSFUL) {
                binding.tv.text = "finish"
                downloading = false
            }
            cursor.close()
        }
    }

    private fun coroutineScopeForProgressBar(){
            job = GlobalScope.launch(Dispatchers.Main){
                for (i in position.toInt()..durationVideo){
                    delay(500L)
                    binding.pbWorkoutProgress.max = durationVideo
                    binding.pbWorkoutProgress.progress =
                        simpleExoPlayer.currentPosition.toInt()
                    binding.tv.text = "${String.format("%.0f",
                            simpleExoPlayer.currentPosition.toFloat() / durationVideo * 100)}" + "%"
                }
            }
    }
    private val visibility = PlayerControlView.VisibilityListener { visibility ->
        if (visibility == View.GONE){
            binding.clUnderVideo.visibility = View.GONE
        }
        else{
            binding.clUnderVideo.visibility = View.VISIBLE
        }
    }
    private val eventListener = object : Player.EventListener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            durationVideo = simpleExoPlayer.duration.toInt()
            if (playbackState == Player.STATE_ENDED) {
                job.cancel()
                binding.tv.text = "101%"
                bindingPopupWindowGoodJob = PopupWindowGoodJobBinding.inflate(layoutInflater)
                val popupWindow = PopupWindow(bindingPopupWindowGoodJob.root,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT)
                    popupWindow.showAsDropDown(binding.root)
                    popupWindow.isFocusable = true
                    popupWindow.update()
                    bindingPopupWindowGoodJob.tvSaveProof.setOnClickListener {
                        findNavController().navigate(R.id.photoFragment)
                        popupWindow.dismiss()
                    }
                    bindingPopupWindowGoodJob.root.setOnClickListener{
                        popupWindow.dismiss()
                    }
            }
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

    override fun onDestroyView() {
        super.onDestroyView()
        bindingPopupWindowGoodJob.root.visibility = View.GONE
        bindingPopupWindow.root.visibility = View.GONE
    }
}



