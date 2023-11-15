package com.mdfurqan.audioplayer.presentation.viewmodel

import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.mdfurqan.audioplayer.data.local.model.Audio
import com.mdfurqan.audioplayer.data.local.repository.AudioRepository
import com.mdfurqan.audioplayer.player.service.AudioPlayerServiceHandler
import com.mdfurqan.audioplayer.player.service.AudioPlayerState
import com.mdfurqan.audioplayer.player.service.PlayerEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private val dummyAudio: Audio = Audio(
    "".toUri(), "", 0L, "", "", 0, ""
)

@HiltViewModel
class AudioViewModel @Inject constructor(
    private val serviceHandler: AudioPlayerServiceHandler,
    private val repository: AudioRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private var duration by savedStateHandle.saveable { mutableLongStateOf(0L) }
    var progress by savedStateHandle.saveable { mutableFloatStateOf(0f) }
    private var progressString by savedStateHandle.saveable { mutableStateOf("00:00") }
    var isPlaying by savedStateHandle.saveable { mutableStateOf(false) }
    var currentSelectedAudio by savedStateHandle.saveable { mutableStateOf(dummyAudio) }
    var audioList by savedStateHandle.saveable { mutableStateOf(listOf<Audio>()) }

    private val _uiState: MutableStateFlow<UIState> = MutableStateFlow(UIState.Initial)
    val uiState: StateFlow<UIState> = _uiState.asStateFlow()


    init {
        loadAudioData()
    }

    init {
        viewModelScope.launch {
            serviceHandler.audioState.collectLatest { mediaState ->
                when (mediaState) {
                    AudioPlayerState.Initial -> _uiState.value = UIState.Initial
                    is AudioPlayerState.Buffering -> calculateProgressValue(mediaState.progress)
                    is AudioPlayerState.Playing -> isPlaying = mediaState.isPlaying
                    is AudioPlayerState.Progress -> calculateProgressValue(mediaState.progress)
                    is AudioPlayerState.CurrentPlaying -> {
                        currentSelectedAudio = audioList[mediaState.mediaItemIndex]
                    }

                    is AudioPlayerState.Ready -> {
                        duration = mediaState.duration
                        _uiState.value = UIState.Ready
                    }
                }


            }
        }
    }

    private fun loadAudioData() {
        viewModelScope.launch {
            val audio = repository.getAudioData()
            audioList = audio
            setMediaItems()
        }
    }

    private fun setMediaItems() {
        audioList.map { audio ->
            MediaItem.Builder().setUri(audio.uri).setMediaMetadata(
                MediaMetadata.Builder().setAlbumArtist(audio.artist)
                    .setDisplayTitle(audio.title).setSubtitle(audio.displayName).build()
            ).build()
        }.also {
            serviceHandler.setMediaItemList(it)
        }
    }

    private fun calculateProgressValue(currentProgress: Long) {
        progress =
            if (currentProgress > 0) ((currentProgress.toFloat() / duration.toFloat()) * 100f)
            else 0f

        progressString = formatDuration(currentProgress)
    }

    private fun formatDuration(duration: Long): String {
        val minute = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
        val second = (minute) - minute * TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES)

        return String().format("%02d:%02d", minute, second)
    }


    override fun onCleared() {
        viewModelScope.launch {
            serviceHandler.onPlayerEvents(PlayerEvent.Stop)
        }
        super.onCleared()
    }

    fun onUIEvents(uiEvents: UIEvents) = viewModelScope.launch {

        when (uiEvents) {
            UIEvents.Backward -> serviceHandler.onPlayerEvents(PlayerEvent.Backward)
            UIEvents.Forward -> serviceHandler.onPlayerEvents(PlayerEvent.Forward)
            UIEvents.SeekToNext -> serviceHandler.onPlayerEvents(PlayerEvent.SeekToNext)

            is UIEvents.PlayPause -> {
                serviceHandler.onPlayerEvents(
                    PlayerEvent.PlayPause
                )
            }

            is UIEvents.SeekTo -> {
                serviceHandler.onPlayerEvents(
                    PlayerEvent.SeekTo,
                    seekPosition = ((duration * uiEvents.position) / 100f).toLong()
                )
            }

            is UIEvents.SelectedAudioChanges -> {
                serviceHandler.onPlayerEvents(
                    PlayerEvent.SelectedAudioChange, selectedAudioIndex = uiEvents.index
                )
            }

            is UIEvents.UpdateProgress -> {
                serviceHandler.onPlayerEvents(
                    PlayerEvent.UpdateProgress(
                        uiEvents.newProgress
                    )
                )
                progress = uiEvents.newProgress
            }
        }

    }
}

sealed class UIEvents {
    object PlayPause : UIEvents()
    data class SelectedAudioChanges(val index: Int) : UIEvents()
    data class SeekTo(val position: Float) : UIEvents()
    data class UpdateProgress(val newProgress: Float) : UIEvents()
    object SeekToNext : UIEvents()
    object Forward : UIEvents()
    object Backward : UIEvents()
}

sealed class UIState {
    object Initial : UIState()
    object Ready : UIState()
}