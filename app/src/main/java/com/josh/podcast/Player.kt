package com.josh.podcast

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.media.AudioManager
import android.media.MediaPlayer
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.net.toUri
import coil.load
import com.google.gson.Gson
import com.josh.podcast.Main.MainActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.w3c.dom.Text
import java.util.*
import kotlin.concurrent.fixedRateTimer
import kotlin.concurrent.timerTask

class Player : AppCompatActivity() {

    var Playing: Boolean = false
    var mediaPlayer: MediaPlayer = MediaPlayer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        val sharedPref = getSharedPreferences("PRIV_INFO", Context.MODE_PRIVATE)
        val PodPlayingDataStr = sharedPref.getString("playing", null)

        var PodPlayingData = FullPodcastInfo(Podcast("Nothing playing", "", "", "", ""), mutableListOf(Episode("", "", "", "", "")))

        if (PodPlayingDataStr != null) {
            PodPlayingData = Gson().fromJson(PodPlayingDataStr, FullPodcastInfo::class.java)
        }

        val EpisodeImage: ImageView = findViewById(R.id.playerImage)
        val EpisodeName: TextView = findViewById(R.id.playerEpName)
        val PodcastName: TextView = findViewById(R.id.playerPodName)
        val EpCurrent: TextView = findViewById(R.id.playerCurrentTime)
        val EpDuration: TextView = findViewById(R.id.playerEpLen)
        val PlayButton: ImageView = findViewById(R.id.playerPlayButton)
        val SeekBar: SeekBar = findViewById(R.id.playerSeekBar)

        var EpRuntime = PodPlayingData.Episodes.get(0).Runtime
        EpDuration.setText(EpRuntime)

        if (PodPlayingData.Episodes.get(0).Runtime != null) {
            EpRuntime = GetSecondsFromHHMMSS(PodPlayingData.Episodes.get(0).Runtime!!).toString()
            SeekBar.max = EpRuntime.toInt()
        }

        if (PodPlayingData.Podcast.ImageURL == "") {
            EpisodeImage.setImageResource(R.drawable.notfound)
        } else {
            val imageAsURI = PodPlayingData.Podcast.ImageURL.toUri().buildUpon().scheme("https").build()
            EpisodeImage.load(imageAsURI) {
                placeholder(R.drawable.loading_animation)
            }
        }

        EpisodeName.setText(PodPlayingData.Episodes.get(0).Name)
        PodcastName.setText(PodPlayingData.Podcast.Name)
        EpDuration.setText(PodPlayingData.Episodes.get(0).Runtime)

        fixedRateTimer(name = "podcastTimer",
            initialDelay = 0, period = 1000,daemon = true) {
            runOnUiThread {

                val CurrentPos = Math.floor((mediaPlayer.currentPosition / 1000).toDouble())

                if (EpRuntime != null) {
                    SeekBar.setProgress(Math.floor((CurrentPos / (EpRuntime.toInt())) * 100).toInt())
                }
                EpCurrent.setText(CurrentPos.toString().dropLast(2))
            }
        }

        PlayButton.setOnClickListener {
            if (!Playing) {
                //play audio
                Playing = true
                PlayButton.setImageResource(android.R.drawable.ic_media_pause)

                var playURL = PodPlayingData.Episodes.get(0).URL

                if (playURL != null) {
                    GlobalScope.launch {
                        Play(playURL)
                    }
                }
            } else {
                //pause audio
                Playing = false
                PlayButton.setImageResource(android.R.drawable.ic_media_play)

                mediaPlayer.pause()
            }
        }

    }

    fun GetSecondsFromHHMMSS(input: String): Int {

        val SplitChar = input.split("")

        var Total = 0
        var multiVal = 1

        var Count = 0

        for (i in SplitChar.size - 1 downTo 0) {
            if (isNumeric(SplitChar[i])) {
                Total += SplitChar[i].toInt() * multiVal

                when (Count) {
                    0 -> multiVal = 10
                    1 -> multiVal = 60
                    2 -> multiVal = 600
                    3 -> multiVal = 6000
                    4 -> multiVal = 60000
                    5 -> multiVal = 600000
                }

                Count++
            }
        }

        return Total
    }

    fun isNumeric(toCheck: String): Boolean {
        return toCheck.toDoubleOrNull() != null
    }

    fun Play(URL: String) {
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)

        try {
            mediaPlayer.setDataSource(URL)
            mediaPlayer.prepare()
            mediaPlayer.seekTo(mediaPlayer.currentPosition)
            mediaPlayer.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}