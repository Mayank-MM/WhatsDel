package com.example.whatsdel.ui.screens

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.whatsdel.data.entity.MessageEntity
import com.example.whatsdel.domain.repository.MessageRepository
import com.example.whatsdel.utils.MediaStorageHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

enum class MediaFilter(val label: String, val dbType: String?) {
    ALL("All", null),
    IMAGES("Images", "image"),
    VIDEOS("Videos", "video"),
    VOICE_NOTES("Voice Notes", "voice_note"),
    STICKERS("Stickers", "sticker");

    companion object {
        fun fromRoute(value: String?): MediaFilter {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: ALL
        }
    }
}

data class MediaUiState(
    val mediaMessages: List<MessageEntity> = emptyList(),
    val isLoading: Boolean = true,
    val selectedFilter: MediaFilter = MediaFilter.ALL
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MediaViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val initialFilter = MediaFilter.fromRoute(savedStateHandle.get<String>("filter"))

    private val _selectedFilter = MutableStateFlow(initialFilter)
    val selectedFilter: StateFlow<MediaFilter> = _selectedFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val uiState: StateFlow<MediaUiState> = combine(
        _selectedFilter,
        _searchQuery
    ) { filter, query ->
        Pair(filter, query)
    }.flatMapLatest { (filter, query) ->
        if (query.isNotBlank()) {
            messageRepository.searchMedia(query)
        } else if (filter.dbType != null) {
            messageRepository.observeMediaByType(filter.dbType)
        } else {
            messageRepository.observeAllMedia()
        }
    }.combine(_selectedFilter) { messages, filter ->
        MediaUiState(
            mediaMessages = messages,
            isLoading = false,
            selectedFilter = filter
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MediaUiState()
    )

    fun onFilterChanged(filter: MediaFilter) {
        _selectedFilter.value = filter
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun deleteMedia(message: MessageEntity, context: Context) {
        viewModelScope.launch {
            try {
                message.localMediaPath?.let { path ->
                    val file = File(path)
                    if (file.exists()) file.delete()
                }
                message.thumbnailPath?.let { path ->
                    val thumb = File(path)
                    if (thumb.exists()) thumb.delete()
                }
                messageRepository.deleteMessage(message)
                Toast.makeText(context, "Media file deleted", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to delete: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun deleteAllMedia(context: Context) {
        viewModelScope.launch {
            try {
                // Wipe app-private media directory
                val mediaDir = MediaStorageHelper.getWhatsDelMediaDirectory(context)
                if (mediaDir.exists() && mediaDir.isDirectory) {
                    mediaDir.listFiles()?.forEach { file ->
                        if (file.isFile) file.delete()
                    }
                }

                // Wipe thumbnails directory
                val thumbDir = File(context.filesDir, "thumbnails")
                if (thumbDir.exists() && thumbDir.isDirectory) {
                    thumbDir.listFiles()?.forEach { file ->
                        if (file.isFile) file.delete()
                    }
                }

                // Wipe all media records from database
                messageRepository.deleteAllMedia()

                Toast.makeText(context, "All backed-up media files deleted", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("MediaViewModel", "Error deleting all media", e)
                Toast.makeText(context, "Failed to delete all media: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
