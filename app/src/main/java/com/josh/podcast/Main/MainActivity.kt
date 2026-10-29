package com.josh.podcast.Main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.activity.result.contract.ActivityResultContracts
import com.josh.podcast.*
import com.josh.podcast.PodcastInfo.PodcastInfoView
import com.josh.podcast.TopPodcasts.TopPodcasts
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setCurrentFragment(R.id.flFragment, MainFragment())
        setCurrentFragment(R.id.playerBar, PlayerBar())

        val NavBar: BottomNavigationView = findViewById(R.id.bottom_navigation)
        NavBar.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.subscriptions -> {
                    setCurrentFragment(R.id.flFragment, MainFragment())
                    true
                }
                R.id.topPodcasts -> {
                    setCurrentFragment(R.id.flFragment, TopPodcasts())
                    true
                }
                R.id.search -> {
                    setCurrentFragment(R.id.flFragment, Search())
                    true
                }
                else -> false
            }
        }
    }

    fun setCurrentFragment(from: Int, to: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(from, to)
            commit()
        }

}