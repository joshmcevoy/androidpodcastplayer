import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.josh.podcast.*
import com.josh.podcast.TopPodcasts.TopPodcastsRV
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SearchRV():  RecyclerView.Adapter<SearchRV.ViewHolder>() {

    var Results: MutableList<Podcast> = mutableListOf()
    var onCardClick: ((FullPodcastInfo) -> Unit)? = null

    inner class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {

        val PodcastImage: ImageView = ItemView.findViewById(R.id.podcastimagemain)
        val PodcastName: TextView = ItemView.findViewById(R.id.podcastnamemain)
        val PodcastAuthor: TextView = ItemView.findViewById(R.id.podcastauthormain)

        fun bind(pos: Int) {
            PodcastName.text = Results.get(pos).Name
            PodcastAuthor.text = Results.get(pos).Author

            val imageAsURI = Results.get(pos).ImageURL.toUri().buildUpon().scheme("https").build()
            PodcastImage.load(imageAsURI) {
                placeholder(R.drawable.loading_animation)
            }
        }

        init {
            ItemView.setOnClickListener {
                val Pod = Results[adapterPosition]

                GlobalScope.launch {
                    val Episodes = mutableListOf(Episode("Loading", "", "", "", ""))
                    onCardClick?.invoke(FullPodcastInfo(Pod, Episodes))
                }

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

    fun update(newList: MutableList<Podcast>) {
        Results = newList
        this.notifyDataSetChanged()
    }

    override fun getItemCount(): Int = Results.size
}