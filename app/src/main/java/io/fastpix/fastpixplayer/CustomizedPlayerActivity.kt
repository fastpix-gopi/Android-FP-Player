package io.fastpix.fastpixplayer

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import com.mux.player.MuxPlayer
import com.mux.stats.sdk.core.model.CustomData
import com.mux.stats.sdk.core.model.CustomerData
import com.mux.stats.sdk.core.model.CustomerVideoData
import com.mux.stats.sdk.core.model.CustomerViewData
import io.fastpix.data.entity.CustomDataEntity
import io.fastpix.data.entity.CustomerDataEntity
import io.fastpix.data.entity.CustomerPlayerDataEntity
import io.fastpix.data.entity.CustomerVideoDataEntity
import io.fastpix.data.entity.CustomerViewDataEntity
import io.fastpix.fastpixplayer.databinding.ActivityConfigurablePlayerBinding
import io.fastpix.fastpixplayer.databinding.NumericParamEntryBinding
import io.fastpix.fastpixplayer.databinding.TextParamEntryBinding
import io.fastpix.player.DataPlayer
import java.util.UUID

class ConfigurablePlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityConfigurablePlayerBinding
    private val playerView get() = binding.player
    private val playerViewMux get() = binding.playerMux
    private val mediaPlaybackConfig = MediaPlaybackConfig()
    private var player: DataPlayer? = null
    private var playbackPosition: Long = 0
    private var playWhenReady = true
    private var player2: MuxPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfigurablePlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
                        or WindowInsetsCompat.Type.displayCutout()
            )
            v.updatePadding(
                top = bars.top,
                bottom = bars.bottom,
            )
            WindowInsetsCompat.CONSUMED
        }

        if (savedInstanceState != null) {
            mediaPlaybackConfig.restoreInstanceState(savedInstanceState)
        }

        binding.configurablePlayerPlaybackId.hint = mediaPlaybackConfig.playbackIdOrDefault()
        binding.configurablePlayerTitel.onClear =
            { mediaPlaybackConfig.configurablePlayerTitel = null }
        binding.configurablePlayerPlaybackId.onClear = { mediaPlaybackConfig.playbackId = null }
        binding.configurablePlayerCustomDomain.onClear =
            { mediaPlaybackConfig.customDomain = null }
        binding.configurablePlayerPlaybackToken.onClear =
            { mediaPlaybackConfig.playbackToken = null }
        binding.configurablePlayerStreamType.hint = mediaPlaybackConfig.streamTypeDefault()
        binding.configurablePlayerStreamType.onClear = { mediaPlaybackConfig.streamType = null }

        binding.configurablePlayerUpdateMediaItem.setOnClickListener {
            mediaPlaybackConfig.playbackId = binding.configurablePlayerPlaybackId.entry
            mediaPlaybackConfig.configurablePlayerTitel = binding.configurablePlayerTitel.entry
            mediaPlaybackConfig.playbackToken = binding.configurablePlayerPlaybackToken.entry
            mediaPlaybackConfig.customDomain = binding.configurablePlayerCustomDomain.entry
            mediaPlaybackConfig.streamType = binding.configurablePlayerStreamType.entry
            playbackPosition = 0
            playWhenReady = true

            maybePlayMediaItem(mediaPlaybackConfig.createMediaItem())
        }
    }

    override fun onStart() {
        super.onStart()

        val mediaItem = mediaPlaybackConfig.createMediaItem()
        maybePlayMediaItem(mediaItem)
    }

    override fun onResume() {
        super.onResume()
        if (player == null && player2 == null) {
            val mediaItem = mediaPlaybackConfig.createMediaItem()
            maybePlayMediaItem(mediaItem)
        }
    }

    override fun onStop() {
        tearDownPlayer()

        super.onStop()
    }

    override fun onPause() {
        super.onPause()
        tearDownPlayer()

    }

    override fun onSaveInstanceState(outState: Bundle) {
        mediaPlaybackConfig.saveInstanceState(outState)

        super.onSaveInstanceState(outState)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.basic_player_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val helperHandled = mediaPlaybackConfig.handleMenuClick(item)
        if (helperHandled) {
            return true
        } else {
            return super.onOptionsItemSelected(item)
        }
    }

    private fun maybePlayMediaItem(mediaItem: MediaItem) {

        if (player != null && player2 != null) {
            player!!.playWhenReady = false
            player2!!.playWhenReady = false
        }

        val item = mediaItem.buildUpon().setMediaMetadata(createMediaMetadata()).build()

        Log.e("logurl", "${player?.currentMediaItem}  ${item} ${player2?.currentMediaItem}")
        if (item != player?.currentMediaItem) {// &&  item != player2?.currentMediaItem) {
            playSomething(item)
        }
    }

    private fun createMediaMetadata(): MediaMetadata {
        return MediaMetadata.Builder()
            .setTitle("Mux Player Example")
            .build()
    }

    private fun tearDownPlayer() {
        if (player != null && player2 != null) {
            playbackPosition = player!!.currentPosition
            playbackPosition = player2!!.currentPosition
            playWhenReady = player!!.playWhenReady
            playWhenReady = player2!!.playWhenReady
            playerView.player = null
            playerViewMux.player = null
            player?.release()
            player2?.release()

            player = null
            player2 = null
        }
    }

    private fun playSomething(mediaItem: MediaItem) {
        Log.e("mediaItem.mediaId", mediaItem.mediaId)
        binding.urlMediaItem.setTextIsSelectable(true)

        binding.urlMediaItem.text = mediaItem.localConfiguration?.uri.toString()
        val player = createPlayer(this, mediaItem)
        val player2 = createMuxPlayer(this, mediaItem)

        player.setMediaItem(mediaItem)
        player2.setMediaItem(mediaItem)
        player.prepare()
        player2.prepare()
        player.playWhenReady = playWhenReady
        player2.playWhenReady = playWhenReady
        player.seekTo(playbackPosition)
        player2.seekTo(playbackPosition)
        this.playerView.player = player
        this.playerViewMux.player = player2
        this.player = player
        this.player2 = player2
    }


    private fun createPlayer(context: Context, mediaItem: MediaItem): DataPlayer {
        val customerPlayerDataEntity = CustomerPlayerDataEntity()
        customerPlayerDataEntity.workspaceKey = "1082179305773531137"
        customerPlayerDataEntity.playerName = "Fastpix Player"
        customerPlayerDataEntity.playerVersion = "1.0.0"
        val customerViewDataEntity = CustomerViewDataEntity()
        customerViewDataEntity.viewSessionId = UUID.randomUUID().toString()

        val customerVideoDataEntity = CustomerVideoDataEntity()

        customerVideoDataEntity.videoId = mediaPlaybackConfig.playbackIdOrDefault()

        customerVideoDataEntity.videoTitle =
            mediaPlaybackConfig.configurablePlayerTitel//!!.text.toString()
        customerVideoDataEntity.videoSourceUrl = mediaItem.localConfiguration?.uri.toString()

        customerVideoDataEntity.videoStreamType = mediaPlaybackConfig.streamTypeDefault()
        customerVideoDataEntity.videoContentType = mediaItem.mediaMetadata.mediaType.toString()


        val customDataEntity = CustomDataEntity()
        customDataEntity.customData1 = "itemType"
        customDataEntity.customData2 = "itemModel"


        val customerDataEntity = CustomerDataEntity(
            customerPlayerDataEntity,
            customerVideoDataEntity,
            customerViewDataEntity
        )
        customerDataEntity.setCustomData(customDataEntity)

        val out: DataPlayer = DataPlayer.Builder(context).pushMonitoringData(customerDataEntity)
            .configureExoPlayer {
                setHandleAudioBecomingNoisy(true)
            }.build()

        out.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                Log.e(TAG, "player error!", error)
                Toast.makeText(
                    this@ConfigurablePlayerActivity,
                    "Playback error! ${error.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })

        return out
    }

    private fun createMuxPlayer(context: Context, mediaItem: MediaItem): MuxPlayer {
        val out: MuxPlayer = MuxPlayer.Builder(context).setMuxDataEnv("nva5voramljt3al5pi4fp6b6g")
            .addMonitoringData(
                CustomerData().apply {
                    customerViewData = CustomerViewData().apply {
                        viewSessionId = UUID.randomUUID().toString()
                    }
                    customerVideoData = CustomerVideoData().apply {
                        videoId = mediaPlaybackConfig.playbackIdOrDefault()
                        videoTitle = mediaPlaybackConfig.configurablePlayerTitel
                        videoSourceUrl = mediaItem.localConfiguration?.uri.toString()
                        videoStreamType = mediaPlaybackConfig.streamTypeDefault()
                        videoContentType = mediaItem.mediaMetadata.mediaType.toString()
                    }

                    customData = CustomData().apply {
                        customData1 = "itemType"
                        customData2 = "itemModel"
                    }
                }
            ).build()
        return out
    }

    companion object {
        val TAG = ConfigurablePlayerActivity::class.simpleName
    }
}

class TextParamEntryView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: TextParamEntryBinding = TextParamEntryBinding.inflate(
        LayoutInflater.from(context),
        this,
        true
    )

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.TextParamEntryView, 0, 0).apply {
            try {
                hint = getString(R.styleable.TextParamEntryView_hint)
            } finally {
                recycle()
            }
        }
        context.theme.obtainStyledAttributes(attrs, R.styleable.ParamEntry, 0, 0).apply {
            try {
                title = getString(R.styleable.ParamEntry_title)
            } finally {
                recycle()
            }
        }
        binding.textParamEntryClear.setOnClickListener {
            binding.textParamEntryIn.text = null
            onClear?.invoke()
        }
    }

    var title: CharSequence? = null
        set(value) {
            binding.textParamEntryLbl.text = value
            field = value
        }
    var hint: CharSequence? = null
        set(value) {
            binding.textParamEntryIn.hint = value
            field = value
        }

    var onClear: (() -> Unit)? = null
    val entry: String?
        get() {
            val text = binding.textParamEntryIn.text?.trim()?.ifEmpty { null }?.toString()
            return text
        }
}

class NumericParamEntryView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: NumericParamEntryBinding = NumericParamEntryBinding.inflate(
        LayoutInflater.from(context),
        this,
        true
    )

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.NumericParamEntryView, 0, 0).apply {
            try {
                hint = getFloat(R.styleable.NumericParamEntryView_hint_num, Float.NaN)
                    .toDouble()
                    .takeIf { !it.isNaN() }
            } finally {
                recycle()
            }
        }
        context.theme.obtainStyledAttributes(attrs, R.styleable.ParamEntry, 0, 0).apply {
            try {
                title = getString(R.styleable.ParamEntry_title)
            } finally {
                recycle()
            }
        }

        binding.numericParamEntryClear.setOnClickListener {
            binding.numericParamEntryIn.text = null
            onClear?.invoke()
        }
    }

    var title: CharSequence? = null
        set(value) {
            binding.numericParamEntryLbl.text = value
            field = value
        }
    var hint: Double? = null
        set(value) {
            binding.numericParamEntryIn.hint = value?.toString()
            field = value
        }

    var onClear: (() -> Unit)? = null
    val entry: Double?
        get() {
            val text =
                binding.numericParamEntryIn.text?.trim()?.ifEmpty { null }?.toString()
                    ?.toDoubleOrNull()
            return text
        }
}
