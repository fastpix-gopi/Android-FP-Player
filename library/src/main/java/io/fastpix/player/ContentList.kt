package io.fastpix.player

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import androidx.media3.common.MediaItem

object ContentList {

    private const val DEFAULT_DOMAIN = "stream.fastpix.app"
    private const val STREAM_TYPE = "on-demand"

    fun contentPlaybackId(
        context: Context,
        playbackId: String,
        maxResolution: PlaybackResolution? = null,
        minResolution: PlaybackResolution? = null,
        renditionOrder: RenditionOrder? = null,
        customDomain: String? = DEFAULT_DOMAIN,
        streamType: String? = STREAM_TYPE,
        playbackToken: String? = null
    ): MediaItem = createCustomPlaybackUri(
        context,
        playbackId,
        maxResolution,
        minResolution,
        renditionOrder,
        customDomain,
        streamType,
        playbackToken
    ).build()

    @JvmStatic
    @JvmOverloads
    fun createCustomPlaybackUri(
        context: Context,
        playbackId: String,
        maxResolution: PlaybackResolution? = null,
        minResolution: PlaybackResolution? = null,
        renditionOrder: RenditionOrder? = null,
        customDomain: String? = DEFAULT_DOMAIN,
        streamType: String? = STREAM_TYPE,
        playbackToken: String? = null
    ): MediaItem.Builder {
        return MediaItem.Builder()
            .setUri(
                createPlaybackUrl(
                    context = context,
                    playbackId = playbackId,
                    customDomain = customDomain ?: DEFAULT_DOMAIN,
                    streamType = streamType ?: STREAM_TYPE,
                    maxResolution = maxResolution,
                    minResolution = minResolution,
                    renditionOrder = renditionOrder,
                    playbackToken = playbackToken,
                )
            )
    }

    private fun createPlaybackUrl(
        context: Context,
        playbackId: String,
        customDomain: String = DEFAULT_DOMAIN,
        streamType: String? = STREAM_TYPE,
        maxResolution: PlaybackResolution? = null,
        minResolution: PlaybackResolution? = null,
        renditionOrder: RenditionOrder? = null,
        playbackToken: String? = null,
    ): String {
        if (streamType !in listOf("on-demand", "live-stream")) {
            return "streamType no matched"
        }
        if (streamType.equals("live-stream")) {
            val button = TextView(context).apply {
                text = "Live"
                textSize = 16f // smaller text size
                setPadding(20, 10, 20, 10) // smaller padding
//                setBackgroundColor(Color.RED) // background color
                setTextColor(Color.RED) // text color
            }

            val params = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = 10
                topMargin = 10 // optional: add top margin
                gravity = Gravity.TOP or Gravity.START
            }

            (context as Activity).addContentView(button, params)

        }
        val base = Uri.Builder()
            .scheme("https")
            .authority(customDomain)
            .appendPath("$playbackId.m3u8")
            .apply {
                minResolution?.let { appendQueryParameter("minResolution", resolutionValue(it)) }
                maxResolution?.let { appendQueryParameter("maxResolution", resolutionValue(it)) }
                renditionOrder?.takeIf { it != RenditionOrder.Default }
                    ?.let { appendQueryParameter("renditionOrder", resolutionValue(it)) }
                playbackToken?.let { appendQueryParameter("token", it) }
            }
            .build()


//    base.appendQueryParameter("redundantStreams", "true");

        return base.toString()
    }

    private fun resolutionValue(renditionOrder: RenditionOrder): String {
        return when (renditionOrder) {
            RenditionOrder.Descending -> "desc"
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

    /**
     * The default rendition order will be used, which may be optimized for delivery
     */
    Default,
}
