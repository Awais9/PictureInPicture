package com.awais.pictureinpicture

import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Rational
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.awais.pictureinpicture.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    inner class VideoPipReceiver : BroadcastReceiver() {
        override fun onReceive(contect: Context?, intent: Intent?) {
            playStopVideo()
        }
    }

    private val isPipSupported by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
        } else {
            false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setViewView()
    }

    private fun setViewView() {
        binding.apply {
            playBtn.setOnClickListener {
                playStopVideo()
            }
        }
    }

    private fun playStopVideo() {
        binding.apply {
            if (playBtn.text.toString().equals("Start Video", true)) {
                videoView.setVideoURI(Uri.parse("android.resource://$packageName/${R.raw.sample2}"))
                videoView.start()
                playBtn.text = "Stop Video"
            } else {
                videoView.stopPlayback()
                playBtn.text = "Start Video"
            }
        }
    }

    private fun updatedPiPParams(): PictureInPictureParams? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PictureInPictureParams.Builder()
                .setSourceRectHint(binding.videoView.clipBounds)
                .setAspectRatio(Rational(16, 9))
                .setActions(
                    listOf(
                        RemoteAction(
                            Icon.createWithResource(this, R.drawable.ic_stop_icon),
                            "Stop Video", "Stop Video",
                            PendingIntent.getBroadcast(
                                applicationContext, 0,
                                Intent(applicationContext, VideoPipReceiver::class.java),
                                PendingIntent.FLAG_IMMUTABLE
                            )
                        )
                    )
                )
                .build()
        } else null
    }

    override fun onResume() {
        super.onResume()
        binding.playBtn.visibility = View.VISIBLE
    }

    override fun onPause() {
        super.onPause()
        binding.playBtn.visibility = View.GONE
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (isPipSupported) {
            updatedPiPParams()?.let { params ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    enterPictureInPictureMode(params)
                }
            }
        }
    }

}