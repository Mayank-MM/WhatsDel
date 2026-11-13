package com.example.whatsdel.ui.screens

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.whatsdel.data.entity.MessageEntity
import com.example.whatsdel.domain.repository.MessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class MediaViewerUiState(
    val message: MessageEntity? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isPlaying: Boolean = false,
    val playbackProgress: Float = 0f
)

@HiltViewModel
class MediaViewerViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val messageId: Long = savedStateHandle.get<Long>("messageId") ?: -1L

    private val _uiState = MutableStateFlow(MediaViewerUiState())
    val uiState: StateFlow<MediaViewerUiState> = _uiState.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null

    init {
        loadMessage()
    }

    private fun loadMessage() {
        viewModelScope.launch {
            try {
                val message = messageRepository.getMessageById(messageId)
                _uiState.value = _uiState.value.copy(
                    message = message,
                    isLoading = false,
                    error = if (message == null) "Message not found" else null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load message: ${e.message}"
                )
            }
        }
    }

    fun togglePlayback() {
        val msg = _uiState.value.message ?: return
        val mediaPath = msg.localMediaPath

        if (mediaPath == null || !File(mediaPath).exists()) {
            _uiState.value = _uiState.value.copy(error = "Audio file not found on device")
            return
        }

        if (_uiState.value.isPlaying) {
            pauseAudio()
        } else {
            playAudio(mediaPath)
        }
    }

    private fun playAudio(path: String) {
        try {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(path)
                    prepare()
                    setOnCompletionListener {
                        stopProgressTracker()
                        _uiState.value = _uiState.value.copy(isPlaying = false, playbackProgress = 0f)
                        releaseMediaPlayer()
                    }
                }
            } else if (_uiState.value.playbackProgress >= 1f) {
                mediaPlayer?.seekTo(0)
            }
            mediaPlayer?.start()
            _uiState.value = _uiState.value.copy(isPlaying = true, error = null)
            startProgressTracker()
        } catch (e: Exception) {
            Log.e("MediaViewerViewModel", "Error playing audio", e)
            releaseMediaPlayer()
            _uiState.value = _uiState.value.copy(isPlaying = false, error = "Cannot play audio file")
        }
    }

    private fun pauseAudio() {
        try {
            mediaPlayer?.pause()
            _uiState.value = _uiState.value.copy(isPlaying = false)
            stopProgressTracker()
        } catch (e: Exception) {
            Log.e("MediaViewerViewModel", "Error pausing audio", e)
            releaseMediaPlayer()
        }
    }

    private fun startProgressTracker() {
        stopProgressTracker()
        progressJob = viewModelScope.launch {
            while (_uiState.value.isPlaying) {
                val player = mediaPlayer ?: break
                if (player.duration > 0) {
                    val progress = player.currentPosition.toFloat() / player.duration.toFloat()
                    _uiState.value = _uiState.value.copy(playbackProgress = progress.coerceIn(0f, 1f))
                }
                delay(200)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun releaseMediaPlayer() {
        try {
            stopProgressTracker()
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            Log.e("MediaViewerViewModel", "Error releasing MediaPlayer", e)
            mediaPlayer = null
        }
    }

    fun deleteMediaFile(context: Context, onSuccess: () -> Unit) {
        val msg = _uiState.value.message ?: return
        viewModelScope.launch {
            try {
                releaseMediaPlayer()

                msg.localMediaPath?.let { path ->
                    val file = File(path)
                    if (file.exists()) file.delete()
                }

                msg.thumbnailPath?.let { path ->
                    val thumb = File(path)
                    if (thumb.exists()) thumb.delete()
                }

                messageRepository.deleteMessage(msg)

                Toast.makeText(context, "Media file deleted", Toast.LENGTH_SHORT).show()
                onSuccess()
            } catch (e: Exception) {
                Log.e("MediaViewerViewModel", "Error deleting media file", e)
                Toast.makeText(context, "Failed to delete: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        releaseMediaPlayer()
    }
}
