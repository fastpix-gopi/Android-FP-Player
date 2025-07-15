package io.fastpix.fastpixplayer

import android.os.Bundle
import android.view.MenuItem
import androidx.media3.common.MediaItem
import io.fastpix.player.ContentList
import io.fastpix.player.PlaybackResolution
import io.fastpix.player.RenditionOrder

class MediaPlaybackConfig {

    var maxRes: PlaybackResolution? = null
    var minRes: PlaybackResolution? = null
    var fixRes: PlaybackResolution? = null
    var renditionOrder: RenditionOrder? = null
    var playbackToken: String? = null
    var playbackId: String? = null
    var configurablePlayerTitel: String? = null
    var customDomain: String? = null
    var streamType: String? = null

    private fun mediaItemBuilder(): MediaItem.Builder {
        return ContentList.createCustomPlaybackUri(
            playbackId = playbackIdOrDefault(),
            minResolution = minRes,
            maxResolution = maxRes,
            resolution = fixRes,
            renditionOrder = renditionOrder,
            playbackToken = playbackToken?.ifEmpty { null },
            customDomain = customDomain?.ifEmpty { null },
            streamType = streamTypeDefault()
            )
    }

    fun playbackIdOrDefault(): String {
        return playbackId?.ifEmpty { DEFAULT_PLAYBACK_ID } ?: DEFAULT_PLAYBACK_ID
    }
    fun playerTitel(): String {
        return configurablePlayerTitel?.ifEmpty { DEFAULT_PLAYBACK_TITEL } ?: DEFAULT_PLAYBACK_TITEL
    }
    fun streamTypeDefault(): String {
        return streamType?.ifEmpty { STREAM_TYPE } ?: STREAM_TYPE
    }

    fun createMediaItem(): MediaItem {
        return mediaItemBuilder().build()
    }

    fun saveInstanceState(state: Bundle) {
        state.putInt("maxRes", maxRes?.ordinal ?: -1)
        state.putInt("minRes", minRes?.ordinal ?: -1)
        state.putInt("fixRes", fixRes?.ordinal ?: -1)
        state.putInt("renditionOrder", renditionOrder?.ordinal ?: -1)
        state.putString("playbackToken", playbackToken)
        state.putString("customDomain", customDomain)
        state.putString("playbackId", playbackId)
        state.putString("streamType", streamType)
        state.putString("configurablePlayerTitel", configurablePlayerTitel)
    }

    fun restoreInstanceState(state: Bundle) {
        maxRes = state.getInt("maxRes", -1)
            .takeIf { it >= 0 }?.let { PlaybackResolution.entries[it] }
        minRes = state.getInt("minRes", -1)
            .takeIf { it >= 0 }?.let { PlaybackResolution.entries[it] }
        fixRes = state.getInt("fixRes", -1)
            .takeIf { it >= 0 }?.let { PlaybackResolution.entries[it] }
        renditionOrder = state.getInt("renditionOrder", -1)
            .takeIf { it >= 0 }?.let { RenditionOrder.entries[it] }

        playbackToken = state.getString("playbackToken", null)
        playbackId = state.getString("playbackId", playbackIdOrDefault())
        customDomain = state.getString("customDomain", null)
        streamType = state.getString("streamType", streamTypeDefault())
    }

    fun handleMenuClick(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.player_menu_2160 -> {
                fixRes = PlaybackResolution.FOUR_K_2160
                true
            }

            R.id.player_menu_1440 -> {
                fixRes = PlaybackResolution.QHD_1440
                true
            }

            R.id.player_menu_1080 -> {
                fixRes = PlaybackResolution.FHD_1080
                true
            }

            R.id.player_menu_720 -> {
                fixRes = PlaybackResolution.HD_720
                true
            }

            R.id.player_menu_540 -> {
                fixRes = PlaybackResolution.LD_540
                true
            }

            R.id.player_menu_480 -> {
                fixRes = PlaybackResolution.LD_480
                true
            }

            R.id.player_menu_unspecified -> {
                fixRes = null
                true
            }

            R.id.player_menu_min_2160 -> {
                minRes = PlaybackResolution.FOUR_K_2160
                true
            }

            R.id.player_menu_min_1440 -> {
                minRes = PlaybackResolution.QHD_1440
                true
            }

            R.id.player_menu_min_1080 -> {
                minRes = PlaybackResolution.FHD_1080
                true
            }

            R.id.player_menu_min_720 -> {
                minRes = PlaybackResolution.HD_720
                true
            }

            R.id.player_menu_min_540 -> {
                minRes = PlaybackResolution.LD_540
                true
            }

            R.id.player_menu_min_480 -> {
                minRes = PlaybackResolution.LD_480
                true
            }

            R.id.player_menu_min_unspecified -> {
                minRes = null
                true
            }

            R.id.player_menu_max_2160 -> {
                maxRes = PlaybackResolution.FOUR_K_2160
                true
            }

            R.id.player_menu_max_1440 -> {
                maxRes = PlaybackResolution.QHD_1440
                true
            }

            R.id.player_menu_max_1080 -> {
                maxRes = PlaybackResolution.FHD_1080
                true
            }

            R.id.player_menu_max_720 -> {
                maxRes = PlaybackResolution.HD_720
                true
            }

            R.id.player_menu_max_unspecified -> {
                maxRes = null
                true
            }

            R.id.player_menu_rendntion_unspecified -> {
                renditionOrder = null
                true
            }

            R.id.player_menu_descending -> {
                renditionOrder = RenditionOrder.Descending
                true
            }

            R.id.player_menu_ascending -> {
                renditionOrder = RenditionOrder.Ascending
                true
            }

            else -> false
        }
    }

    companion object {
        const val DEFAULT_PLAYBACK_TITEL = ""
        const val DEFAULT_PLAYBACK_ID = "6859b77a-a0bd-473f-85fe-3f9d8de88155"
        const val STREAM_TYPE = "on-demand"
    }
}
