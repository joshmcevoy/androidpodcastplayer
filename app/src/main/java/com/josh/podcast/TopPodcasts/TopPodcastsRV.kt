package com.josh.podcast.TopPodcasts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.josh.podcast.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class TopPodcastsRV():  RecyclerView.Adapter<TopPodcastsRV.ViewHolder>() {

    var mList: MutableList<Podcast> = mutableListOf()
    var onCardClick: ((FullPodcastInfo) -> Unit)? = null

    inner class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val PodcastRank: TextView = ItemView.findViewById(R.id.tpPodcastRank)
        val PodcastImage: ImageView = ItemView.findViewById(R.id.tpPodcastImage)
        val PodcastName: TextView = ItemView.findViewById(R.id.tpPodcastName)

        fun bind(position: Int) {
            PodcastRank.text = "#%s".format(position + 1)
            PodcastName.text = mList[position].Name

            val imageAsURI = mList[position].ImageURL.toUri().buildUpon().scheme("https").build()
            PodcastImage.load(imageAsURI) {
                placeholder(R.drawable.loading_animation)
            }
        }

        init {
            ItemView.setOnClickListener {
                val Pod = mList[adapterPosition]

                GlobalScope.launch {
                    val Episodes = mutableListOf(Episode("Loading", "", "", "", ""))
                    onCardClick?.invoke(FullPodcastInfo(Pod, Episodes))
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cardview_toppodcasts, parent, false)

        return ViewHolder(view)
    }

    fun update(newList: MutableList<Podcast>) {
        mList = newList
        this.notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = mList.size

}
