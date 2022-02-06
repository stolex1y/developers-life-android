package ru.stolexiy.developerslife.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GifRepository(
    private val gifService: GifService = GifService.create(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend fun getRandomGif(): Gif = withContext(ioDispatcher) { gifService.getRandomGif() }
    suspend fun getCategoryGifPage(category: GifCategory, page: Int): List<Gif> {
        return withContext(ioDispatcher) {
            when (category) {
                GifCategory.LATEST -> getLatestGifPage(page)
                GifCategory.HOT -> getHotGifPage(page)
                GifCategory.TOP -> getTopGifPage(page)
                else -> throw UnsupportedOperationException("Unsupported gif category: $category")
            }
        }
    }

    private suspend fun getLatestGifPage(page: Int): List<Gif> {
        return withContext(ioDispatcher) {
            gifService.getLatestGifPage(page).result
        }
    }

    private suspend fun getHotGifPage(page: Int): List<Gif> {
        return withContext(ioDispatcher) {
            gifService.getHotGifPage(page).result
        }
    }

    private suspend fun getTopGifPage(page: Int): List<Gif> {
        return withContext(ioDispatcher) {
            gifService.getTopGifPage(page).result
        }
    }

}