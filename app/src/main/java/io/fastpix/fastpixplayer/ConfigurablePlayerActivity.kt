package io.fastpix.fastpixplayer

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import io.fastpix.data.entity.CustomDataEntity
import io.fastpix.data.entity.CustomerDataEntity
import io.fastpix.data.entity.CustomerPlayerDataEntity
import io.fastpix.data.entity.CustomerVideoDataEntity
import io.fastpix.data.entity.CustomerViewDataEntity
import io.fastpix.data.entity.VideoDataEntity
import io.fastpix.fastpixplayer.databinding.ActivityConfigurablePlayerBinding
import io.fastpix.fastpixplayer.databinding.NumericParamEntryBinding
import io.fastpix.fastpixplayer.databinding.TextParamEntryBinding
import io.fastpix.player.DataPlayer
import java.util.UUID


/**
 * A configurable example that uses the normal media3 player UI to play a video in the foreground from
 * Mux Video, using a Playback ID
 *
 * You can configure the Activity via the UI
 */
class ConfigurablePlayerActivity : AppCompatActivity() {

  private lateinit var binding: ActivityConfigurablePlayerBinding
  private val playerView get() = binding.player

  private val playbackParamsHelper = PlaybackParamsHelper()

  private var player: DataPlayer? = null

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
      playbackParamsHelper.restoreInstanceState(savedInstanceState)
    }

    binding.configurablePlayerPlaybackId.hint = playbackParamsHelper.playbackIdOrDefault()
    binding.configurablePlayerPlaybackId.onClear = { playbackParamsHelper.playbackId = null }
    binding.configurablePlayerCustomDomain.onClear = { playbackParamsHelper.customDomain = null }
    binding.configurablePlayerPlaybackToken.onClear = { playbackParamsHelper.playbackToken = null }
    binding.configurablePlayerStreamType.hint = playbackParamsHelper.streamTypeDefault()
    binding.configurablePlayerStreamType.onClear = { playbackParamsHelper.streamType = null }

    binding.configurablePlayerUpdateMediaItem.setOnClickListener {
      playbackParamsHelper.playbackId = binding.configurablePlayerPlaybackId.entry
      playbackParamsHelper.playbackToken = binding.configurablePlayerPlaybackToken.entry
      playbackParamsHelper.customDomain = binding.configurablePlayerCustomDomain.entry
      playbackParamsHelper.streamType = binding.configurablePlayerStreamType.entry

      maybePlayMediaItem(playbackParamsHelper.createMediaItem(this))
    }
  }

  override fun onStart() {
    super.onStart()

    val mediaItem = playbackParamsHelper.createMediaItem(this)
    maybePlayMediaItem(mediaItem)
  }

  override fun onStop() {
    tearDownPlayer()

    super.onStop()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    playbackParamsHelper.saveInstanceState(outState)

    super.onSaveInstanceState(outState)
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.basic_player_menu, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    val helperHandled = playbackParamsHelper.handleMenuClick(item)
    if (helperHandled) {
      val newMediaItem = playbackParamsHelper.createMediaItem(this)
      maybePlayMediaItem(newMediaItem)
      return true
    } else {
      return super.onOptionsItemSelected(item)
    }
  }

  private fun maybePlayMediaItem(mediaItem: MediaItem) {
    val item = mediaItem.buildUpon().setMediaMetadata(createMediaMetadata()).build()
    if (item != player?.currentMediaItem) {
      playSomething(item)
    }
  }

  private fun createMediaMetadata(): MediaMetadata {
    return MediaMetadata.Builder()
      .setTitle("Mux Player Example")
      .build()
  }

  private fun tearDownPlayer() {
    playerView.player = null
    player?.release()
  }

  private fun playSomething(mediaItem: MediaItem) {
    val player = createPlayer(this)
    player.setMediaItem(mediaItem)
    player.prepare()
    player.playWhenReady = true

    this.playerView.player = player
    this.player = player
  }

  @OptIn(UnstableApi::class)
  private fun createPlayer(context: Context): DataPlayer {
    val customerPlayerDataEntity = CustomerPlayerDataEntity()
    customerPlayerDataEntity.workspaceKey = "1082179305773531137"

    customerPlayerDataEntity.playerVersion = "0.1"
    customerPlayerDataEntity.subPropertyId = "NA"
    customerPlayerDataEntity.experimentName = "Test V1 "
    customerPlayerDataEntity.playerName = "Media3"


    val customerVideoDataEntity = CustomerVideoDataEntity()

    customerVideoDataEntity.videoId = "itemId"
    customerVideoDataEntity.videoTitle = "title "+playbackParamsHelper.playbackIdOrDefault()//!!.text.toString()
    customerVideoDataEntity.videoSourceUrl = "https://test.m3u8"

    val customerViewDataEntity = CustomerViewDataEntity()
    customerViewDataEntity.viewSessionId = UUID.randomUUID().toString()

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
        setSeekBackIncrementMs(10_000)
        setSeekForwardIncrementMs(10_000)
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
  val entry: String? get() {
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
  val entry: Double? get() {
    val text =
      binding.numericParamEntryIn.text?.trim()?.ifEmpty { null }?.toString()?.toDoubleOrNull()
    return text
  }
}
