package com.example.tubesmobdev.data.model

data class TopListItemData(
    val id       : String,
    val title    : String,
    val subtitle : String? = null,
    val coverUrl : String? = null,
    val count    : Int     = 0
)

enum class TopListType { Artist, Song }