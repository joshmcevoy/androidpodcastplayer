package com.josh.podcast.Main

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.josh.podcast.*
import com.josh.podcast.PodcastInfo.PodcastInfoView

class MainFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        var Root: View = inflater.inflate(R.layout.fragment_main, null)

        val MainList: RecyclerView = Root.findViewById(R.id.mainListView)
        MainList.layoutManager = LinearLayoutManager(Root.context)

        val sharedPref = (activity as MainActivity).getSharedPreferences("PRIV_INFO", Context.MODE_PRIVATE)

        val RowAdapter = MainRVAdapter()

        val subscribedPodcasts = sharedPref.getString("Subscriptions", null)
        if (subscribedPodcasts != null) {
            val Parsed: SubscribedPodcasts = Gson().fromJson(subscribedPodcasts, SubscribedPodcasts::class.java)
            RowAdapter.Subscriptions = Parsed.SubList
        }

        MainList.adapter = RowAdapter

        RowAdapter.onCardClick = { card ->
            //write data to shared preferences
            val sharedPref = (activity as MainActivity).getSharedPreferences("PRIV_INFO", Context.MODE_PRIVATE)
            val editor = sharedPref.edit()


            editor.putString("FullPodcastInfo", Gson().toJson(FullPodcastInfo(card, mutableListOf(
                Episode("Loading", "", "", "", "")
            ))))

            editor.commit()

            (activity as MainActivity).setCurrentFragment(R.id.flFragment, PodcastInfoView())
        }

        return Root
    }

}