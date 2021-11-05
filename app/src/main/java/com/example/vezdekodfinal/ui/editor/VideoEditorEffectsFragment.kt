package com.example.vezdekodfinal.ui.editor

import android.app.ProgressDialog
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.lifecycle.*
import com.example.vezdekodfinal.R
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.StyledPlayerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.File

class VideoEditorEffectsFragment : Fragment() {

    companion object {
        private const val TMP_FILE_PATH = "tmp_file_path"

        fun newInstance(tmpFilePath: String) = VideoEditorEffectsFragment().apply {
            arguments = Bundle().apply { putString(TMP_FILE_PATH, tmpFilePath) }
        }
    }

    private lateinit var viewModel: VideoEditorEffectsViewModel
    private lateinit var closeButton: ImageView
    private lateinit var saveButton: TextView
    private lateinit var tmpSessionFile: File
    private lateinit var exoPlayerView: StyledPlayerView
    private lateinit var exoPlayer: SimpleExoPlayer
    private lateinit var speedBar: SeekBar
    private lateinit var tmpEffectsFile: File
    private var wasSaved = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.video_editor_effects_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        closeButton = view.findViewById(R.id.close_button)
        saveButton = view.findViewById(R.id.save_button)
        speedBar = view.findViewById(R.id.speed_bar)
        tmpSessionFile = arguments?.getString(TMP_FILE_PATH)?.let { File(it) } ?: return
        tmpEffectsFile = File("${tmpSessionFile.path.substringBeforeLast('/')}/tmp_${tmpSessionFile.path.substringAfterLast('/')}").apply {
            createNewFile()
        }
        tmpSessionFile.inputStream().use { ins ->
            tmpEffectsFile.outputStream().use { ous ->
                ins.copyTo(ous)
            }
        }
        exoPlayer = SimpleExoPlayer.Builder(view.context).build()
        exoPlayerView = view.findViewById(R.id.exo_player_view)
        exoPlayerView.player = exoPlayer
    }

    override fun onResume() {
        super.onResume()

        wasSaved = false

        val mediaItem = MediaItem.fromUri(tmpEffectsFile.path)

        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()

        closeButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        saveButton.setOnClickListener {
            viewModel.saveChanges()
            Toast.makeText(requireContext(), "Effect saved", LENGTH_SHORT).show()
        }

        speedBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // ignore
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                exoPlayer.stop()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                viewModel.updateTmpEffectsFile(speedBar.progress / 100f)
            }
        })

        val progress = ProgressDialog(requireContext())
        progress.setTitle("Loading")
        val message = "Wait while loading... "
        progress.setMessage(message)
        progress.setCancelable(false)
        progress.setOnDismissListener {
            exoPlayer.setMediaItem(MediaItem.fromUri(tmpEffectsFile.path))
            exoPlayer.prepare()
            exoPlayer.play()
        }
        viewModel.isSaving.asLiveData().observe(this) {
            if (it && !wasSaved) {
                wasSaved = true
                exoPlayer.stop()
                progress.show()
            } else if (!it && wasSaved) {
                wasSaved = false
                requireActivity().runOnUiThread {
                    progress.dismiss()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()

        exoPlayer.stop()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(VideoEditorEffectsViewModel::class.java)
        viewModel.init(tmpSessionFile, tmpEffectsFile)
    }

}