package com.josh.podcast

import SearchRV
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.josh.podcast.Main.MainActivity
import com.josh.podcast.PodcastInfo.PodcastInfoView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class Search : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val Root = inflater.inflate(R.layout.fragment_search, null)

        val SearchResults: RecyclerView = Root.findViewById(R.id.searchRV)
        SearchResults.layoutManager = LinearLayoutManager(Root.context)

        val SearchAdapter = SearchRV()
        SearchResults.adapter = SearchAdapter

        var SearchBar: EditText = Root.findViewById(R.id.SearchBar)

        SearchBar.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                val imm: InputMethodManager = (activity as MainActivity).getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(requireView().getWindowToken(), 0)

                val PodCrawler = PodcastCrawler

                GlobalScope.launch {
                    val SearchResults = PodCrawler.Search(SearchBar.text.toString())

                    (activity as MainActivity).runOnUiThread {
                        SearchAdapter.update(SearchResults)
                    }
                }
            }
            false
        })

        SearchAdapter.onCardClick = { card ->
            //write data to shared preferences
            val sharedPref = (activity as MainActivity).getSharedPreferences("PRIV_INFO", Context.MODE_PRIVATE)
            val editor = sharedPref.edit()

            editor.putString("FullPodcastInfo", Gson().toJson(card))

            editor.commit()

            (activity as MainActivity).setCurrentFragment(R.id.flFragment, PodcastInfoView())
        }

        return Root
    }

}