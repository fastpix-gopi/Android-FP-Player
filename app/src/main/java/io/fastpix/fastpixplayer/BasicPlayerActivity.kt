package io.fastpix.fastpixplayer

//import com.fastpix.playermodule.LocalPlayer
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import io.fastpix.data.entity.CustomDataEntity
import io.fastpix.data.entity.CustomerDataEntity
import io.fastpix.data.entity.CustomerPlayerDataEntity
import io.fastpix.data.entity.CustomerVideoDataEntity
import io.fastpix.data.entity.CustomerViewDataEntity
import io.fastpix.fastpixplayer.databinding.ActivityBasicPlayerBinding
import io.fastpix.player.ContentList
import io.fastpix.player.DataPlayer
import java.util.UUID

/**
 * A minimal example of using Mux Player without any extra functionality
 */
class BasicPlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBasicPlayerBinding
    private val playerView get() = binding.player

    private var player: DataPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, true)

        binding = ActivityBasicPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()

        playSomething()
    }

    override fun onStop() {
        tearDownPlayer()

        super.onStop()
    }

    private fun tearDownPlayer() {
        playerView.player = null
        player?.release()
    }

    private fun playSomething() {
        val localPlayer = createPlayer(this)

        val mediaItem = ContentList.createCustomPlaybackUri(this,
            playbackId = "139f8137-e86a-4372-803d-4bca13b7a874", streamType = "live-stream",
            playbackToken = "eyJhbGciOiJSUzI1NiJ9.eyJraWQiOiI5Y2RjOTk0OC1mMGEzLTQzNjAtOWVlNS02MGZkYjcwZWMwZWMiLCJhdWQiOiJtZWRpYTpjYmJhZWJiMy1mZmJhLTRhZmYtOTlkZC1mOTBjZmE5OWQzNTkiLCJpc3MiOiJmYXN0cGl4LmlvIiwic3ViIjoiIiwiaWF0IjoxNzUwMjIyOTc4LCJleHAiOjE3NTAzMDkzNzh9.Jko5CHt2aswvDnGSFIEXlf97S5ADm-uR9X5LHibBpgoCiRrrr2rrAlXhg_JgMRjgFJ9G-XInsWpOpe_bw6uQkp1zVLyhlnwMhRCiJzM0_671mgJkudt3FQq0smQBq962qLG9LYpoUw0TEWsNUJzH7CzV8HjVGxHwJDiKBZPNsjK5VdK5q32u3-IReSVk0q7kAvAWvqX0bIRG096XPgGhSDvlT3n5ezTeLnOLsoCXistOl-b01zrqoyiPAfGP99a6hmuxhQTv5dME3dbD3CYXjttu0DbqDRzf72v6cfGxDS3p8xkNJHBhnv4MKT87tq_twDYRddYeeNicBFTCC2ozow"
        ).build()

        localPlayer.setMediaItem(mediaItem)
        localPlayer.prepare()
        localPlayer.playWhenReady = true

        this.playerView.player = localPlayer
        this.player = localPlayer
        /* muxStats = FastPixBaseMedia3Player(
             this,
             player,
             customerDataEntity,
             CustomOptions()
         )*/
//        localPlayer.setVideoSurfaceView(playerView)

    }

    @OptIn(UnstableApi::class)
    private fun createPlayer(context: Context): DataPlayer {
          val customerPlayerDataEntity = CustomerPlayerDataEntity()
          customerPlayerDataEntity.workspaceKey = "1081857932514590721"

          customerPlayerDataEntity.playerVersion = "0.1"
          customerPlayerDataEntity.subPropertyId = "NA"
          customerPlayerDataEntity.experimentName = "Test V1 "
          customerPlayerDataEntity.playerName = "Media3"

          val customerVideoDataEntity = CustomerVideoDataEntity()


          customerVideoDataEntity.videoId = "itemId"
          customerVideoDataEntity.videoTitle = "title"//!!.text.toString()
          customerVideoDataEntity.videoSourceUrl = "https://stream.fastpix.app/139f8137-e86a-4372-803d-4bca13b7a874.m3u8"

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

        val out: DataPlayer = DataPlayer.Builder(context)
            .pushMonitoringData(customerDataEntity
            )
            .configureExoPlayer {
                // Call ExoPlayer.Builder methods here
                setHandleAudioBecomingNoisy(true)
                setSeekBackIncrementMs(10_000)
                setSeekForwardIncrementMs(10_000)
            }
            .build()


        out.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                Log.e(TAG, "player error!", error)
                Toast.makeText(
                    this@BasicPlayerActivity,
                    "Playback error! ${error.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })


        return out
    }

    companion object {
        val TAG = BasicPlayerActivity::class.simpleName
    }
}
