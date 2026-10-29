package com.josh.podcast.Main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.josh.podcast.FullPodcastInfo
import com.josh.podcast.Podcast
import com.josh.podcast.R

class MainRVAdapter() :  RecyclerView.Adapter<MainRVAdapter.ViewHolder>() {

    var onCardClick: ((Podcast) -> Unit)? = null

    var Subscriptions: MutableList<Podcast> = mutableListOf()

    inner class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val PodcastImage: ImageView = ItemView.findViewById(R.id.podcastimagemain)
        val PodcastName: TextView = ItemView.findViewById(R.id.podcastnamemain)
        val PodcastAuthor: TextView = ItemView.findViewById(R.id.podcastauthormain)

        fun bind(pos: Int) {
            PodcastName.text = Subscriptions.get(pos).Name
            PodcastAuthor.text = Subscriptions.get(pos).Author

            val imageAsURI = Subscriptions.get(pos).ImageURL.toUri().buildUpon().scheme("https").build()
            PodcastImage.load(imageAsURI) {
                placeholder(R.drawable.loading_animation)
            }
        }

        init {
            ItemView.setOnClickListener {
                onCardClick?.invoke(Subscriptions.get(adapterPosition))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cardview_main, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = Subscriptions.size

}
