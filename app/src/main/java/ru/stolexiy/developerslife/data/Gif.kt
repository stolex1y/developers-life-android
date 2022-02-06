package ru.stolexiy.developerslife.data

import ru.stolexiy.developerslife.R

enum class GifCategory(val nameId: Int) {
    LATEST(nameId = R.string.tab_title_latest),
    TOP(nameId = R.string.tab_title_top),
    HOT(nameId = R.string.tab_title_hot)
}

data class Gif(
    val id: Int,
    val description: String,
    val gifURL: String
)

data class GifPage(val result: List<Gif>)
