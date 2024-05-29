package com.homemedics.app.ui.custom

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.CountDownTimer
import android.os.Environment
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.Chronometer
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.fatron.network_module.utils.TinyDB
import com.homemedics.app.R
import com.homemedics.app.databinding.FragmentAddVoicenoteBinding
import com.homemedics.app.ui.fragment.doctorconsultation.BookConsultationDetailsFragment
import com.homemedics.app.utils.getSafe
import com.homemedics.app.utils.setVisible
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

class CustomAudio(context: Context) : AlertDialog.Builder(context) {
    lateinit var dialogViewBinding: FragmentAddVoicenoteBinding

    private var dialogSaveButton: Button? = null
    var elapsedMillis = 0L
    private lateinit var meter: Chronometer
    private var player: MediaPlayer? = null
    private var recorder: MediaRecorder? = null
    private var length = 0
    private lateinit var animBlink: Animation
    private var file: File? = null
    private var absolutePath: String = ""
    lateinit var tvVoiceNote: View
    private var absolutsPathafter: String = ""

    var onSaveFile: ((item: File, elapsedMillis: Long) -> Unit)? = null

    var fileName: String = ""

    var voiceNote: String = ""

    var cancel: String = ""

    var url: String = ""


    var isPlayOnly: Boolean = false

    var title: String = ""
        set(value) {
            field = value
            setTitle(value)
        }

    var positiveButtonText: String = ""
        set(value) {
            field = value
            if (isPlayOnly.not())
                setPositiveButton(positiveButtonText) { _, _ -> }
        }
    var negativeButtonText: String = ""
        set(value) {
            field = value
            setNegativeButton(negativeButtonText) { _, _ ->
                absolutePath = ""
                // absolutePath = absolutsPathafter
                //    TinyDB.instance.putString("absolutepath",absolutsPathafter)
                if (isPlayOnly) {
                    stopPlaying()

                }
                stopRecording()
                deleteAudioFile()
                if (::tvVoiceNote.isInitialized)
                    tvVoiceNote.isClickable = true
            }
        }

    init {
        create()
    }

    override fun create(): AlertDialog {
        dialogViewBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.fragment_add_voicenote,
            null,
            false
        )
        setView(dialogViewBinding.root)
        setCancelable(false)
        animBlink = AnimationUtils.loadAnimation(
            context,
            R.anim.blink
        )

        dialogViewBinding.IvPlay.tag = R.string.play
        dialogViewBinding.apply {
            meter = tvTimer
            IvVoiceImage.setOnClickListener {
                file = createVoiceFile(context)
                meter.base = SystemClock.elapsedRealtime()
                absolutePath = file?.absolutePath.getSafe()
                startRecording()
                val timer = object : CountDownTimer(3 * 60 * 1000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {}

                    override fun onFinish() {
                        rlPlayLayout.setVisible(true)
                        flPlayLayout.setVisible(false)
                        elapsedMillis = SystemClock.elapsedRealtime() - meter.base
                        stopRecording()
                        validate()
                    }
                }
                timer.start()
                flPlayLayout.setVisible(true)
                IvVoiceImage.setVisible(false)
                validate()
            }

            IvVoicePause.setOnClickListener {//flPlayLayout
                rlPlayLayout.setVisible(true)
                flPlayLayout.setVisible(false)
                elapsedMillis = SystemClock.elapsedRealtime() - meter.base
                stopRecording()
                validate()
            }
            tvCancel.setOnClickListener {//flPlayLayout
                IvVoiceImage.setVisible(true)
                elapsedMillis = SystemClock.elapsedRealtime() - meter.base
                stopRecording()
                deleteAudioFile()
                flPlayLayout.setVisible(false)
                validate()
            }
            IvPlayDelete.setOnClickListener { //rlPlayLayout
                IvVoiceImage.setVisible(true)
                rlPlayLayout.setVisible(false)
                stopPlaying()
                deleteAudioFile()
                validate()
            }
            IvPlay.setOnClickListener {//rlPlayLayout
                if (IvPlay.tag == R.string.play)
                    startPlaying()
                else
                    stopPlaying()

            }

        }
        val alertDialog = super.create()
        alertDialog.setOnShowListener {
            dialogSaveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            dialogSaveButton?.setOnClickListener {
                if (dialogViewBinding.flPlayLayout.isVisible) {
                    elapsedMillis = SystemClock.elapsedRealtime() - meter.base
                    stopRecording()
                    if (elapsedMillis == 0L) {
                        deleteAudioFile()
                        dialogViewBinding.IvVoiceImage.setVisible(true)
                    } else
                        dialogViewBinding.rlPlayLayout.setVisible(true)
                    dialogViewBinding.flPlayLayout.setVisible(false)
                } else {
                    alertDialog.dismiss()
                    file?.let { it1 -> onSaveFile?.invoke(it1, elapsedMillis) }
//                    saveFile()
                }
            }
            dialogSaveButton?.isEnabled = false
        }

        return alertDialog

    }


    fun startRecording() {
        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }
        recorder.apply {

            this?.setAudioSource(MediaRecorder.AudioSource.MIC)
            this?.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT)
            this?.setOutputFile(absolutePath)
            this?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            try {
                this?.prepare()
                this?.start()
                CoroutineScope(Dispatchers.Main).launch {
                    delay(1000L)

                    meter.start()
                    meter.startAnimation(animBlink)
                    dialogViewBinding.IvVoicePause.startAnimation(animBlink)


                }

            } catch (e: IOException) {
                Log.e("MainActivity", "prepare() failed")
            }


        }
    }

    fun stopRecording() {
        meter.stop()
        try {
            if (recorder != null) {
                recorder?.setOnErrorListener(null);
                recorder?.setOnInfoListener(null);
                recorder?.setPreviewDisplay(null);
                recorder?.stop()
                recorder?.release()
                recorder = null
            }
        } catch (e: IllegalStateException) {
            // TODO: handle exception
            Log.i("Exception", Log.getStackTraceString(e));
        } catch (e: RuntimeException) {
            // TODO: handle exception
            Log.i("Exception", Log.getStackTraceString(e));
        } catch (e: Exception) {
            // TODO: handle exception
            Log.i("Exception", Log.getStackTraceString(e));
        }
    }
//its an extra function was a replacement for startplaying
  /*  fun startPlaying() {
        file?.absolutePath?.let {
            absolutsPathafter = it
        }

        player = MediaPlayer().apply {
            try {
                dialogViewBinding.IvPlay.tag = R.string.pause
                dialogViewBinding.IvPlay.setImageResource(R.drawable.ic_pause)


                if (!absolutePath.isNullOrEmpty()) {
                    setDataSource(absolutePath)
                } else if (!absolutsPathafter.isNullOrEmpty()){
                    setDataSource(absolutsPathafter)
                }else{
                    setDataSource(Url(url).toString())
                }


                prepare()
                seekTo(length)
                start()
            } catch (e: IOException) {
                Log.e("MainActivity", "prepare() failed")
            }
        }
        player?.setOnCompletionListener {
            dialogViewBinding.IvPlay.tag = R.string.play
            dialogViewBinding.IvPlay.setImageResource(R.drawable.ic_play_arrow)
            length = 0
            player?.release()
            player = null

        }

    }*/
  fun startPlaying() {
      file?.absolutePath?.let {
          absolutsPathafter = it
      }
      player = MediaPlayer().apply {
          try {
              dialogViewBinding.IvPlay.tag = R.string.pause
              dialogViewBinding.IvPlay.setImageResource(R.drawable.ic_pause)

                  // setDataSource(absolutePath)
              if (!absolutePath.isNullOrEmpty()) {
                  setDataSource(absolutePath)
              } else {
                  setDataSource(absolutsPathafter)
              }
              prepare()
              seekTo(length)
              start()
          } catch (e: IOException) {
              Log.e("MainActivity", "prepare() failed")
          }
      }
      player?.setOnCompletionListener {
          dialogViewBinding.IvPlay.tag = R.string.play
          dialogViewBinding.IvPlay.setImageResource(R.drawable.ic_play_arrow)
          length = 0
          player?.release()
          player = null

      }

  }

    fun stopPlaying() {
        dialogViewBinding.IvPlay.tag = R.string.play
        dialogViewBinding.IvPlay.setImageResource(R.drawable.ic_play_arrow)
        player?.pause()
        length = player?.currentPosition.getSafe()

    }

    private fun validate() {
        dialogViewBinding.apply {
            dialogSaveButton?.isEnabled = !this.IvVoiceImage.isVisible

        }
    }

    @Throws(IOException::class)
    fun createVoiceFile(context: Context): File {
        val timeStamp = System.currentTimeMillis().toString()
        val storageDir: File = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)!!
        val fileName = "${timeStamp}.mp3"
        val file = File(storageDir, fileName)
        file.parentFile.mkdirs()
        file.createNewFile()
        return file
    }

    private fun deleteAudioFile() {
        if (file?.exists().getSafe()) {
            file?.delete()
        }
    }

    fun onPause() {
        voicePause()
        stopPlaying()
        stopRecording()
        isPlayOnly = true
    }

    fun voicePause() {
        dialogViewBinding.rlPlayLayout.setVisible(true)
        dialogViewBinding.flPlayLayout.setVisible(false)
        elapsedMillis = SystemClock.elapsedRealtime() - meter.base
        stopRecording()
        validate()
    }

    override fun show(): AlertDialog {
        absolutePath = fileName

        val dialog = super.show()

        dialogViewBinding.tvCancel.text = cancel
        dialogViewBinding.tvVoiceNote.text = voiceNote
        dialogViewBinding.apply {
            if (isPlayOnly) {
                rlPlayLayout.setVisible(true)
                tvVoiceNote.setVisible(false)
                IvPlayDelete.setVisible(false)
                IvVoiceImage.setVisible(false)
            }
        }
        return dialog
    }
}