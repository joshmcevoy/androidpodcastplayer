package com.josh.podcast.PodcastInfo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.josh.podcast.*
import com.josh.podcast.Main.MainActivity
import com.josh.podcast.Main.MainRVAdapter
import com.josh.podcast.Main.PodcastInfoRV
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PodcastInfoView : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var Root: View = inflater.inflate(R.layout.fragment_podcast_info_view, null)

        var ListView: RecyclerView = Root.findViewById(R.id.podcastInfoRV)
        ListView.layoutManager = LinearLayoutManager(Root.context)

        val RowAdapter = PodcastInfoRV()
        ListView.adapter = RowAdapter

        val sharedPref = (activity as MainActivity).getSharedPreferences("PRIV_INFO", Context.MODE_PRIVATE)

        val getFullPodcastInfo = sharedPref.getString("FullPodcastInfo", null)
        var Parsed: FullPodcastInfo? = null

        if (getFullPodcastInfo != null) {
            println(getFullPodcastInfo)
            Parsed = Gson().fromJson(getFullPodcastInfo, FullPodcastInfo::class.java)
            RowAdapter.FullPodcastInfoList = Parsed

            //load episodes
            GlobalScope.launch {
                val PodCrawler = PodcastCrawler
                val RSSFeed = PodCrawler.GetPodcastRSSFeed(Parsed.Podcast.Name.replace(" ", "+"), Parsed.Podcast.Author.replace(" ", "+"))
                val LoadedEpisodes = FullPodcastInfo(Parsed.Podcast, PodCrawler.InterpretRSSFeed(PodCrawler.ReadRSSFeed(RSSFeed)))

                (activity as MainActivity).runOnUiThread {
                    RowAdapter.Loaded = true
                    RowAdapter.update(LoadedEpisodes)
                }
            }
        }

        val Editor = sharedPref.edit()

        RowAdapter.onSubscribeClick = { pod ->

            var Subscriptions = sharedPref.getString("Subscriptions", null)
            var ParsedSubscriptions: SubscribedPodcasts? = null

            //check if subscriptions have been set
            if (Subscriptions != null) {
                ParsedSubscriptions = Gson().fromJson(Subscriptions, SubscribedPodcasts::class.java)

                //unsubscribe
                if (pod.subscribed) {
                    var NewSubscriptions: MutableList<Podcast> = mutableListOf()

                    for (Subscription in ParsedSubscriptions.SubList) {
                        if (Subscription != pod) {
                            NewSubscriptions.add(Subscription)
                        }
                    }

                    pod.subscribed = false
                    ParsedSubscriptions.SubList.add(pod)

                    Editor.putString("Subscriptions", Gson().toJson(SubscribedPodcasts(NewSubscriptions)))

                } else {
                    //subscribe
                    pod.subscribed = true
                    ParsedSubscriptions.SubList.add(pod)

                    Editor.putString("Subscriptions", Gson().toJson(ParsedSubscriptions))
                }

                RowAdapter.update()

            } else {
                //subscribe
                pod.subscribed = true
                Editor.putString("Subscriptions", Gson().toJson(SubscribedPodcasts(mutableListOf(pod))))
            }

            Editor.commit()
        }

        RowAdapter.onEpisodeClick = { fullData ->

            Editor.putString("playing", Gson().toJson(fullData))
            Editor.commit()

            val Intent = Intent(Root.context, Player::class.java)
            startActivity(Intent)

        }

        return Root
    }

}