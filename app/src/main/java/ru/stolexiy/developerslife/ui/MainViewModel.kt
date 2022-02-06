package ru.stolexiy.developerslife.ui

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.stolexiy.developerslife.R
import ru.stolexiy.developerslife.data.Gif
import ru.stolexiy.developerslife.data.GifCategory
import ru.stolexiy.developerslife.data.GifRepository
import ru.stolexiy.developerslife.util.NetworkConnection
import java.net.UnknownHostException

private const val TAG = "MainViewModel"

class MainViewModel(
    private val repository: GifRepository
): ViewModel() {

    private var loading: Boolean = false

    var uiState by mutableStateOf(MainViewState())
        private set

    init {
        Log.d(TAG, "Init")
        nextGif()
    }

    companion object {
        fun provideFactory(
            repository: GifRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(repository) as T
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
    }

    fun changeSelectedGifCategory(category: GifCategory) {
        Log.d(TAG, "change category to ${category.name}")
        uiState = uiState.copy(selectedGifCategory = category)
        updateCurrentGif()
    }

    private fun loadNextList(): Job {
        Log.i(TAG, "load next page ${uiState.getCurrentCategoryList().nextPage}")
        return viewModelScope.launch {
            try {
                uiState.getCurrentCategoryList().list.addAll(
                    repository.getCategoryGifPage(
                        uiState.selectedGifCategory,
                        uiState.getCurrentCategoryList().nextPage
                    )
                )
                uiState.getCurrentCategoryList().incPage()
                Log.i(TAG, "loaded page, ${uiState.getCurrentCategoryList().list.size} gifs")
            } catch (e: UnknownHostException) {
                uiState = uiState.copy(errorMessageId = R.string.network_error)
                Log.i(TAG, "UnknownHostException ${e.localizedMessage}")
                Log.d(TAG, e.stackTraceToString())
            } catch (e: Exception) {
                uiState = uiState.copy(errorMessageId = R.string.internal_app_error)
                Log.i(TAG, "Exception ${e.localizedMessage}")
                Log.d(TAG, e.stackTraceToString())
            }
        }
    }

    fun nextGif() {
        Log.d(TAG, "get next gif, current ${uiState.getCurrentCategoryList().currentGif}")
        uiState = uiState.copy(loading = true, errorMessageId = null)
        if (uiState.getCurrentCategoryList().hasNextGif()) {
            uiState.getCurrentCategoryList().nextGif()
            uiState = uiState.copy(
                loading = false,
                currentGif = uiState.getCurrentCategoryList().currentGif()
            )
        } else if (!loading) {
            viewModelScope.launch {
                loading = true
                val job = loadNextList()
                job.invokeOnCompletion {
                    uiState.getCurrentCategoryList().nextGif()
                    uiState = uiState.copy(loading = false, currentGif = uiState.getCurrentCategoryList().currentGif())
                    loading = false
                }
            }
        }
        Log.d(TAG, "got next gif, current ${uiState.currentGif}")
    }

    fun prevGif() {
        Log.d(TAG, "get prev gif, current ${uiState.getCurrentCategoryList().currentGif}")
        if (uiState.getCurrentCategoryList().hasPrevGif()) {
            uiState.getCurrentCategoryList().prevGif()
            updateCurrentGif()
        }
    }

    fun updateCurrentGif() {
        val current = uiState.getCurrentCategoryList().currentGif()
        if (current == null)
            nextGif()
        else
            uiState = uiState.copy(currentGif = current)
    }
}

data class MainViewState(
    private val gifs: MutableList<GifList> = mutableListOf(),
    val selectedGifCategory: GifCategory = GifCategory.LATEST,
    val errorMessageId: Int? = null,
    val loading: Boolean = false,
    val currentGif: Gif? = null,
    val isError: Boolean = errorMessageId != null
) {
    val hasPrevGif: Boolean
        get() = getCurrentCategoryList().hasPrevGif()

    init {
        if (gifs.size == 0) {
            GifCategory.values().forEach {
                gifs += GifList(category = it)
            }
        }
    }

    fun isOnline(context: Context) = NetworkConnection.isOnline(context)

    fun getCurrentCategoryList(): GifList = getListByCategory(selectedGifCategory)

    private fun getListByCategory(category: GifCategory) = gifs[category.ordinal]

    class GifList(
        val category: GifCategory,
        val list: MutableList<Gif> = mutableListOf(),
    ) {
        var nextPage: Int = 0
            private set

        var currentGif: Int = -1
            private set

        fun incPage() = nextPage++

        fun nextGif() {
            if (hasNextGif()) currentGif++
        }

        fun prevGif() {
            if (hasPrevGif()) currentGif--
        }

        fun currentGif(): Gif? {
            return list.getOrNull(currentGif)
        }

        fun hasNextGif() = currentGif < list.size - 1
        fun hasPrevGif() = currentGif > 0
    }
}