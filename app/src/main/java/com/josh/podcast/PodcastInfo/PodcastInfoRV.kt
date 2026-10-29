package com.josh.podcast.Main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.josh.podcast.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PodcastInfoRV() :  RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var onCardClick: ((String) -> Unit)? = null
    var onSubscribeClick: ((Podcast) -> Unit)? = null
    var onEpisodeClick: ((FullPodcastInfo) -> Unit)? = null

    lateinit var FullPodcastInfoList: FullPodcastInfo

    var Loaded: Boolean = false

    inner class InfoViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val PodcastImage: ImageView = ItemView.findViewById(R.id.podInfoImage)
        val PodName: TextView = ItemView.findViewById(R.id.podInfoName)
        val PodProvider: TextView = ItemView.findViewById(R.id.podInfoProvider)
        val PodDescription: TextView = ItemView.findViewById(R.id.podInfoDescription)
        val Subscribe: Button = ItemView.findViewById(R.id.podInfoSubscribe)

        fun bind() {
            PodName.setText(FullPodcastInfoList.Podcast.Name)
            PodProvider.setText(FullPodcastInfoList.Podcast.Author)
            PodDescription.setText(FullPodcastInfoList.Podcast.Description)

            val imageAsURI = FullPodcastInfoList.Podcast.ImageURL.toUri().buildUpon().scheme("https").build()
            PodcastImage.load(imageAsURI) {
                placeholder(R.drawable.loading_animation)
            }

            if (FullPodcastInfoList.Podcast.subscribed) {
                Subscribe.setText("Unsubscribe")
            } else {
                Subscribe.setText("Subscribe")
            }
        }

        init {
            Subscribe.setOnClickListener {
                onSubscribeClick?.invoke(FullPodcastInfoList.Podcast)
            }
        }

    }

    inner class EpisodeViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val EpisodeName: TextView = ItemView.findViewById(R.id.podinfo_episodeName)
        val EpisodeReleaseDate: TextView = ItemView.findViewById(R.id.podinfo_episodeReleaseDate)
        val EpisodeRuntime: TextView = ItemView.findViewById(R.id.podinfo_episodeRuntime)
        val EpisodeDescription: TextView = ItemView.findViewById(R.id.podinfo_episodeDescription)

        fun bind(position: Int) {

            EpisodeName.setText(FullPodcastInfoList.Episodes[position - 1].Name)
            EpisodeReleaseDate.setText(FullPodcastInfoList.Episodes[position - 1].PubDate)
            EpisodeRuntime.setText(FullPodcastInfoList.Episodes[position - 1].Runtime)
            EpisodeDescription.setText(FullPodcastInfoList.Episodes[position - 1].Description)
        }


        init {
            ItemView.setOnClickListener {
                onEpisodeClick?.invoke(FullPodcastInfo(FullPodcastInfoList.Podcast, mutableListOf<Episode>(FullPodcastInfoList.Episodes.get(adapterPosition - 1))))
            }
        }
    }

    inner class Loading(ItemView: View) : RecyclerView.ViewHolder(ItemView) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        var returnView: View

        when (viewType) {
            0 -> {
                returnView = LayoutInflater.from(parent.context).inflate(R.layout.cardview_podinfo, parent, false)
                return InfoViewHolder(returnView)
            }
            else -> {
                /*
                if (viewType) {
                    println(Loaded)
                    returnView = LayoutInflater.from(parent.context).inflate(R.layout.cardview_loading, parent, false)
                    return Loading(returnView)
                }*/

                returnView = LayoutInflater.from(parent.context).inflate(R.layout.cardview_podinfo_episode, parent, false)
                return EpisodeViewHolder(returnView)
            }
        }

    }

    fun update(newList: FullPodcastInfo? = null) {
        if (newList != null) {
            FullPodcastInfoList = newList
        }
        this.notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (position) {
            0 -> (holder as InfoViewHolder).bind()
            else -> if (FullPodcastInfoList.Episodes.size > 1 && FullPodcastInfoList.Episodes.get(0).Name != "Loading") { (holder as EpisodeViewHolder).bind(position) }
        }
    }

    override fun getItemCount(): Int = FullPodcastInfoList.Episodes.size + 1

    override fun getItemViewType(position: Int): Int = position

}
