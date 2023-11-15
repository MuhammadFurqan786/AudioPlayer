package com.mdfurqan.audioplayer

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.mdfurqan.audioplayer.player.service.AudioPlayerService
import com.mdfurqan.audioplayer.presentation.components.HomeScreen
import com.mdfurqan.audioplayer.presentation.viewmodel.AudioViewModel
import com.mdfurqan.audioplayer.presentation.viewmodel.UIEvents
import com.mdfurqan.audioplayer.ui.theme.AudioPlayerTheme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: AudioViewModel by viewModels()
    private var isServiceRunning = false

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AudioPlayerTheme {
                // A surface container using the 'background' color from the theme
                val permissionState = rememberPermissionState(
                    permission = Manifest.permission.READ_EXTERNAL_STORAGE
                )
                val lifecycleOwner = LocalLifecycleOwner.current
                DisposableEffect(key1 = lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            permissionState.launchPermissionRequest()
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeScreen(
                        progress = viewModel.progress,
                        onProgress = { viewModel.onUIEvents(UIEvents.SeekTo(it)) },
                        isAudioPlaying = viewModel.isPlaying,
                        audiList = viewModel.audioList,
                        currentPlayingAudio = viewModel.currentSelectedAudio,
                        onStart = {
                            viewModel.onUIEvents(UIEvents.PlayPause)
                        },
                        onItemClick = {
                            viewModel.onUIEvents(UIEvents.SelectedAudioChanges(it))
                            startService()
                        },
                        onNext = {
                            viewModel.onUIEvents(UIEvents.SeekToNext)
                        }
                    )
                }
            }
        }
    }

    private fun startService() {
        if (!isServiceRunning) {
            val intent = Intent(this, AudioPlayerService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            isServiceRunning = true
        }
    }
}
