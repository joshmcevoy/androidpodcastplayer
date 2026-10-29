package com.josh.podcast.TopPodcasts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.josh.podcast.Main.MainActivity
import com.josh.podcast.Podcast
import com.josh.podcast.PodcastCrawler
import com.josh.podcast.PodcastInfo.PodcastInfoView
import com.josh.podcast.R
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import kotlinx.coroutines.*
import android.content.Context
import com.google.gson.Gson

class TopPodcasts() : Fragment() {

    val client = HttpClient(CIO)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var Loaded: Boolean = false

        val Root: View = inflater.inflate(R.layout.fragment_top_podcasts, null)

        val TopPodcastList: RecyclerView = Root.findViewById(R.id.topPodcastsRV)
        TopPodcastList.layoutManager = GridLayoutManager(Root.context, 2)

        //set adapter and handle clicks
        val TopPodcastsAdapter = TopPodcastsRV()
        TopPodcastList.adapter = TopPodcastsAdapter

        TopPodcastsAdapter.onCardClick = { card ->
            if (Loaded) {
                //write data to shared preferences
                val sharedPref = (activity as MainActivity).getSharedPreferences("PRIV_INFO", Context.MODE_PRIVATE)
                val editor = sharedPref.edit()

                editor.putString("FullPodcastInfo", Gson().toJson(card))

                editor.commit()

                (activity as MainActivity).setCurrentFragment(R.id.flFragment, PodcastInfoView())
            }
        }

        GlobalScope.launch {
            val NewEntries = PodcastCrawler.GetTopPodcasts()

            (activity as MainActivity).runOnUiThread {
                TopPodcastsAdapter.update(NewEntries)
                Loaded = true
            }
        }

        return Root
    }



}