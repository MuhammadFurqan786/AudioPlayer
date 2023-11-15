package com.mdfurqan.audioplayer.data.local.repository

import com.mdfurqan.audioplayer.data.local.ContentResolverHelper
import com.mdfurqan.audioplayer.data.local.model.Audio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AudioRepository @Inject constructor(
    private val contentResolverHelper: ContentResolverHelper
) {

    suspend fun getAudioData(): List<Audio> = withContext(Dispatchers.IO) {
        contentResolverHelper.getAudioData()
    }

}