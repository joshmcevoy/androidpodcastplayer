package com.josh.podcast

data class Podcast(val Name: String,
                   val Author: String,
                   val RSS: String,
                   val Description: String,
                   val ImageURL: String,
                   var subscribed: Boolean = false)
