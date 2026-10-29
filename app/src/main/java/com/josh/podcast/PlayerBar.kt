package com.josh.podcast

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.gson.Gson
import com.josh.podcast.Main.MainActivity


class PlayerBar : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        var Root: View = inflater.inflate(R.layout.fragment_player_bar, null)

        val sharedPref = (activity as MainActivity).getSharedPreferences("PRIV_INFO", Context.MODE_PRIVATE)
        val PodPlayingDataStr = sharedPref.getString("playing", null)

        var PodPlayingData = FullPodcastInfo(Podcast("Nothing playing", "", "", "", ""), mutableListOf(Episode("", "", "", "", "")))

        if (PodPlayingDataStr != null) {
            PodPlayingData = Gson().fromJson(PodPlayingDataStr, FullPodcastInfo::class.java)
        }

        var PlayingText: TextView = Root.findViewById(R.id.playerBarPlayingText)
        var PlayingImage: ImageView = Root.findViewById(R.id.playerBarImage)
        var PlayButton: ImageView = Root.findViewById(R.id.playerBarPlay)

        PlayingText.setText(PodPlayingData.Episodes.get(0).Name)

        if (PodPlayingData.Podcast.ImageURL == "") {
            PlayingImage.setImageResource(R.drawable.notfound)
        } else {
            val imageAsURI = PodPlayingData.Podcast.ImageURL.toUri().buildUpon().scheme("https").build()
            PlayingImage.load(imageAsURI) {
                placeholder(R.drawable.loading_animation)
            }
        }

        PlayingText.setOnClickListener {
            val Intent = Intent(Root.context, Player::class.java)

            startActivity(Intent)
        }

        return Root
    }

}