package io.fastpix.player

import android.net.Uri
import android.util.Log
import androidx.media3.common.MediaItem

object ContentList {
    private const val DEFAULT_DOMAIN = "stream.fastpix.io"
    private const val STREAM_TYPE = "on-demand"

    fun createCustomPlaybackUri(
        playbackId: String,
        maxResolution: PlaybackResolution? = null,
        minResolution: PlaybackResolution? = null,
        resolution: PlaybackResolution? = null,
        renditionOrder: RenditionOrder? = null,
        customDomain: String? = DEFAULT_DOMAIN,
        streamType: String? = STREAM_TYPE,
        playbackToken: String? = null
    ): MediaItem.Builder {
        return MediaItem.Builder()
            .setUri(
                createPlaybackUrl(
                    playbackId = playbackId,
                    customDomain = customDomain ?: DEFAULT_DOMAIN,
                    streamType = streamType ?: STREAM_TYPE,
                    maxResolution = maxResolution,
                    minResolution = minResolution,
                    resolution = resolution,
                    renditionOrder = renditionOrder,
                    playbackToken = playbackToken,
                )
            )
    }

    private fun createPlaybackUrl(
        playbackId: String,
        customDomain: String = DEFAULT_DOMAIN,
        streamType: String? = STREAM_TYPE,
        maxResolution: PlaybackResolution? = null,
        minResolution: PlaybackResolution? = null,
        resolution: PlaybackResolution? = null,
        renditionOrder: RenditionOrder? = null,
        playbackToken: String? = null,
    ): String {
        if (streamType !in listOf("on-demand", "live-stream")) {
            return "streamType no matched"
        }
        val base = Uri.Builder()
            .scheme("https")
            .authority(customDomain)
            .appendPath("$playbackId.m3u8")
            .apply {
                minResolution?.let {
                    appendQueryParameter(
                        "minResolution",
                        resolutionValue(it)
                    )
                }
                maxResolution?.let {
                    appendQueryParameter(
                        "maxResolution",
                        resolutionValue(it)
                    )
                }
                resolution?.let { appendQueryParameter("resolution", resolutionValue(it)) }
                renditionOrder?.takeIf { it != RenditionOrder.Default }
                    ?.let { appendQueryParameter("renditionOrder", resolutionValue(it)) }
                playbackToken?.let { appendQueryParameter("token", it) }
            }
            .build()

        Log.e("base", base.toString())
        return base.toString()
    }

    private fun resolutionValue(renditionOrder: RenditionOrder): String {
        return when (renditionOrder) {
            RenditionOrder.Descending -> "desc"
            RenditionOrder.Ascending -> "asc"
            else -> "" // should be avoided by createPlaybackUrl
        }
    }

    private fun resolutionValue(playbackResolution: PlaybackResolution): String {
        return when (playbackResolution) {
            PlaybackResolution.LD_480 -> "480p"
            PlaybackResolution.LD_540 -> "540p"
            PlaybackResolution.HD_720 -> "720p"
            PlaybackResolution.FHD_1080 -> "1080p"
            PlaybackResolution.QHD_1440 -> "1440p"
            PlaybackResolution.FOUR_K_2160 -> "2160p"
        }
    }
}


enum class PlaybackResolution {
    LD_480,
    LD_540,
    HD_720,
    FHD_1080,
    QHD_1440,
    FOUR_K_2160,
}

/**
 * The order of preference for adaptive streaming.
 */
enum class RenditionOrder {
    /**
     * The highest-resolution renditions will be chosen first, adjusting downward if needed. This
     * setting emphasizes video quality, but may lead to more interruptions on unfavorable networks
     */
    Descending,
    Ascending,

    /**
     * The default rendition order will be used, which may be optimized for delivery
     */
    Default,
}
