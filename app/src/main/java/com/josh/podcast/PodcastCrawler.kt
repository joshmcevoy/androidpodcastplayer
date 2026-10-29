package com.josh.podcast

import android.content.Context
import android.util.Xml
import com.google.gson.JsonElement
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.*
import org.json.JSONObject
import org.json.JSONTokener
import org.xmlpull.v1.XmlPullParser
import org.kobjects.ktxml.*
import org.xmlpull.v1.XmlPullParserFactory

object PodcastCrawler {

    val client = HttpClient(CIO)

    suspend fun GetTopPodcasts(): MutableList<Podcast> {
        val URLString = "https://listen-api.listennotes.com/api/v2/best_podcasts?region=au&sort=listen_score&safe_mode=0"
        val Response = client.get(URLString) { headers { append("X-ListenAPI-Key", "c4e2134ca28c4c1c97ff08976d19ac73") } }.bodyAsText()

        return InterpretAPIRequest(Response)
    }

    //searches the iTunes store API and returns the RSS feed for a podcast
     suspend fun GetPodcastRSSFeed(name: String, provider: String): String {
         val SearchURL = "https://itunes.apple.com/search?term=%s+%s&entity=podcast".format(name, provider)
         val Response = client.get(SearchURL).bodyAsText()

         val ResponseJSON = JSONTokener(Response).nextValue() as JSONObject
         val FeedURL = ResponseJSON.getJSONArray("results").getJSONObject(0).getString("feedUrl")

         return FeedURL
    }

    //takes in an RSS feed URL and returns the JSON
    suspend fun ReadRSSFeed(feedURL: String): String {
        return client.get(feedURL).bodyAsText()
    }


    suspend fun Search(query: String): MutableList<Podcast> {
        val SearchURL = "https://listen-api.listennotes.com/api/v2/search?q=%s&type=podcast&language=English".format(query.replace(" ", "%20"))
        val Response = client.get(SearchURL) { headers { append("X-ListenAPI-Key", "LISTENNOTES_API_KEY") } }.bodyAsText() // insert listennotes api key

        val ReturnArr = mutableListOf<Podcast>()
        val Object = JSONTokener(Response).nextValue() as JSONObject

        val podcasts = Object.getJSONArray("results")

        for (i in 0 until podcasts.length()) {
            val Name = podcasts.getJSONObject(i).getString("title_original")
            val Author = podcasts.getJSONObject(i).getString("publisher_original")
            val RSS = ""
            val Description = podcasts.getJSONObject(i).getString("description_original")
            val ImageURL = podcasts.getJSONObject(i).getString("image")

            val NewObject: Podcast = Podcast(Name, Author, RSS, Description, ImageURL)
            ReturnArr.add(NewObject)
        }

        return ReturnArr
    }

    //takes in the string of an API request and converts it to an array of Podcast objects
    suspend fun InterpretAPIRequest(api: String): MutableList<Podcast> {
        val ReturnArr = mutableListOf<Podcast>()
        val Object = JSONTokener(api).nextValue() as JSONObject

        val podcasts = Object.getJSONArray("podcasts")

        for (i in 0 until podcasts.length()) {
            val Name = podcasts.getJSONObject(i).getString("title")
            val Author = podcasts.getJSONObject(i).getString("publisher")
            val RSS = ""
            val Description = podcasts.getJSONObject(i).getString("description")
            val ImageURL = podcasts.getJSONObject(i).getString("image")

            val NewObject: Podcast = Podcast(Name, Author, RSS, Description, ImageURL)
            ReturnArr.add(NewObject)
        }

        return ReturnArr
    }

    //takes in rss feed as a string and returns a mutable list of episode objects
    //I originally wrote this using the XMLPullParser library, but I found it to be
    //far too complex and unreliable so I re-wrote it just using Regex. I've left the
    //original XMLPullParser code to document my first attempt.
    fun InterpretRSSFeed(FeedStr: String): List<Episode> {

        val entries = mutableListOf<Episode>()

        //Second attempt using Regex
        val ItemRegex = "<item>[\\s\\S]*?<\\/item>".toRegex()
        val TitleRegex = "<title>[\\s\\S]*?<\\/title>".toRegex()
        val iTunesSummaryRegex = "<itunes:summary>[\\s\\S]*?<\\/itunes:summary>".toRegex()
        val DescriptionRegex = "<description>[\\s\\S]*?<\\/description>".toRegex()
        val RuntimeRegex = "<itunes:duration>[\\s\\S]*?<\\/itunes:duration>".toRegex()
        val PubDateRegex = "<pubDate>[\\s\\S]*?<\\/pubDate>".toRegex()
        val EnclosureRegex = "<enclosure[\\s\\S]*?\\/>".toRegex()
        val EnclosureRuntimeRegex = "length=\".*?\\\"".toRegex()
        val EnclosureURLRegex = "url=\".*?\\\"".toRegex()

        ItemRegex.findAll(FeedStr).forEach { itemMatch ->

            val Title = TitleRegex.find(itemMatch.value)?.value?.drop(7)?.dropLast(8)
            var Description = iTunesSummaryRegex.find(itemMatch.value)?.value?.drop(16)?.dropLast(17)
            var Runtime = RuntimeRegex.find(itemMatch.value)?.value?.drop(17)?.dropLast(18)
            val PubDate = PubDateRegex.find(itemMatch.value)?.value?.drop(9)?.dropLast(10)
            val URL = EnclosureRegex.find(itemMatch.value)?.value?.let { EnclosureURLRegex.find(it)?.value }?.drop(5)?.dropLast(1)

            //prefer the itunes runtime & summary, but if they return null fall back to the standard entries.
            if (Runtime == null) Runtime = EnclosureRegex.find(itemMatch.value)?.value?.let { EnclosureRuntimeRegex.find(it)?.value }?.drop(7)?.dropLast(1)
            if (Description == null) Description = DescriptionRegex.find(itemMatch.value)?.value?.drop(13)?.dropLast(15)


            entries.add(Episode(Title, Description, Runtime, PubDate, URL))
        }

        return entries

        //First attempt using XMLPullParser
        /*
        SampleFeed = SampleFeed.replace("&", "&amp;")
        SampleFeed = SampleFeed.replace("?","&#63;")


        val parser: XmlPullParser = XmlPullParserFactory.newInstance().newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(SampleFeed.byteInputStream(), null)
        parser.nextTag()

        for (entry in readFeed(parser)) {
            println(entry)
        }
         */
    }

    //First attempt using XMLPullParser
    /*
    private fun readFeed(parser: XmlPullParser): List<Episode> {
        val entries = mutableListOf<Episode>()

        while (true) {
            parser.next()

            if (parser.name == "item") {
                break
            }
        }

        var NewPod = Episode("", "", "", "", "")

        var Title: String? = null
        var Description: String? = null
        var Runtime: String? = null
        var PubDate: String? = null
        var URL: String? = null

        while (parser.eventType != XmlPullParser.END_DOCUMENT) {

            if (parser.eventType == XmlPullParser.START_TAG) {

                when (parser.name) {
                    "title" -> {
                        entries.add(Episode(Title, Description, Runtime, PubDate, URL))

                        parser.next()
                        Title = parser.text
                    }
                    "description" -> {
                        parser.next()
                        Description = parser.text
                    }
                    "pubDate" -> {
                        parser.next()
                        PubDate = parser.text
                    }
                    "enclosure" -> {
                        Runtime = parser.getAttributeValue(null, "length")
                        URL = parser.getAttributeValue(null, "url")

                        parser.next()
                    }
                }

            }

            parser.next()
        }
        return entries
    }*/

}

