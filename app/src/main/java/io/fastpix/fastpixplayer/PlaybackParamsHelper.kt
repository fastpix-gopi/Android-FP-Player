package io.fastpix.fastpixplayer

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import androidx.media3.common.MediaItem
import io.fastpix.player.ContentList
import io.fastpix.player.PlaybackResolution
import io.fastpix.player.RenditionOrder

/**
 * Helper class for the example Activities that handles setting playback params and stuff
 */
class PlaybackParamsHelper {

    var maxRes: PlaybackResolution? = null
    var minRes: PlaybackResolution? = null
    var renditionOrder: RenditionOrder? = null
    var playbackToken: String? = null
    var playbackId: String? = null
    var customDomain: String? = null
    var streamType: String? = null

    fun createMediaItemBuilder(context: Context): MediaItem.Builder {
        return ContentList.createCustomPlaybackUri(
            context = context,
            playbackId = playbackIdOrDefault(),
            minResolution = minRes,
            maxResolution = maxRes,
            renditionOrder = renditionOrder,
            playbackToken = playbackToken?.ifEmpty { null },
            customDomain = customDomain?.ifEmpty { null },
            streamType = streamTypeDefault()


            )
    }

    fun playbackIdOrDefault(): String {
        return playbackId?.ifEmpty { DEFAULT_PLAYBACK_ID } ?: DEFAULT_PLAYBACK_ID
    }
    fun streamTypeDefault(): String {
        return streamType?.ifEmpty { STREAM_TYPE } ?: STREAM_TYPE
    }

    fun createMediaItem(context: Context): MediaItem {
        return createMediaItemBuilder(context).build()
    }

    fun saveInstanceState(state: Bundle) {
        state.putInt("PlaybackParamsHelper.maxRes", maxRes?.ordinal ?: -1)
        state.putInt("PlaybackParamsHelper.minRes", minRes?.ordinal ?: -1)
        state.putInt("PlaybackParamsHelper.renditionOrder", renditionOrder?.ordinal ?: -1)
        state.putString("PlaybackParamsHelper.playbackToken", playbackToken)
        state.putString("PlaybackParamsHelper.customDomain", customDomain)
        state.putString("PlaybackParamsHelper.playbackId", playbackId)
    }

    fun restoreInstanceState(state: Bundle) {
        maxRes = state.getInt("PlaybackParamsHelper.maxRes", -1)
            .takeIf { it >= 0 }?.let { PlaybackResolution.entries[it] }
        minRes = state.getInt("PlaybackParamsHelper.minRes", -1)
            .takeIf { it >= 0 }?.let { PlaybackResolution.entries[it] }
        renditionOrder = state.getInt("PlaybackParamsHelper.renditionOrder", -1)
            .takeIf { it >= 0 }?.let { RenditionOrder.entries[it] }

        playbackToken = state.getString("PlaybackParamsHelper.playbackToken", null)
        playbackId = state.getString("PlaybackParamsHelper.playbackId", null)
        customDomain = state.getString("PlaybackParamsHelper.customDomain", null)
    }

    fun handleMenuClick(item: MenuItem): Boolean {
        return when (item.itemId) {
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

            R.id.player_menu_default -> {
                renditionOrder = RenditionOrder.Default
                true
            }

            else -> false
        }
    }

    companion object {
        const val DEFAULT_PLAYBACK_ID = "139f8137-e86a-4372-803d-4bca13b7a874"
        const val STREAM_TYPE = "on-demand"
    }
}
