package com.example.vezdekodfinal.ui.editor

import android.app.ProgressDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.flowWithLifecycle
import com.example.vezdekodfinal.R
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.StyledPlayerView
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


class VideoEditorCropFragment : Fragment() {

    companion object {
        private const val TMP_FILE_PATH = "tmp_file_path"
        private const val VIDEO_DURATION = "DURATION"

        fun newInstance(tmpFilePath: String, videoDuration: Long) = VideoEditorCropFragment().apply {
            arguments = Bundle().apply {
                putString(TMP_FILE_PATH, tmpFilePath)
                putLong(VIDEO_DURATION, videoDuration)
            }
        }
    }

    private lateinit var viewModel: VideoEditorCropViewModel
    private lateinit var timelineEditorView: VideoEditorRangeSelectorViewAbs
    private lateinit var closeButton: ImageView
    private lateinit var saveButton: TextView
    //private lateinit var cropView: VideoEditorCropVideoViewAbs
    private lateinit var exoPlayer: SimpleExoPlayer
    private lateinit var exoPlayerView: StyledPlayerView
    private var wasLoaded = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.video_editor_crop_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(VideoEditorCropViewModel::class.java)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        timelineEditorView = view.findViewById(R.id.editor_timeline)
        exoPlayer = SimpleExoPlayer.Builder(view.context).build()
        exoPlayerView = view.findViewById(R.id.exo_player_view)
        closeButton = view.findViewById(R.id.close_button)
        saveButton = view.findViewById(R.id.save_button)
        // cropView = view.findViewById(R.id.crop_view)

        exoPlayerView.player = exoPlayer
    }

    override fun onPause() {
        super.onPause()
        exoPlayer.stop()
    }

    override fun onResume() {
        super.onResume()

        wasLoaded = false

        timelineEditorView.dragHelper = VideoEditorRangeSelectorController(
            requireContext(),
            timelineEditorView
        )
        timelineEditorView.dragHelper?.setListener(viewModel.rangeSelectorListener)
        timelineEditorView.attach(requireContext(), viewModel, lifecycle)

        /*cropView.controller = VideoEditorCropVideoControllerImpl(cropView).apply {
            setListener(viewModel.cropVideoListener)
        }
        cropView.attach(requireContext(), viewModel, lifecycle)

        cropView.apply {
            xLeft = exoPlayerView.left
            xRight = exoPlayerView.right
            yTop = exoPlayerView.top
            yBottom = exoPlayerView.bottom
            visibility = View.VISIBLE
        }*/

        val pathString = arguments?.getString(TMP_FILE_PATH) ?: return
        val uri = Uri.parse(pathString)
        // viewModel.init(uri, requireContext())
        val mediaItem = MediaItem.fromUri(uri)

        val duration = arguments?.getLong(VIDEO_DURATION) ?: return

        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()

        viewModel.init(pathString, duration, requireContext())  // TODO: check it!

        timelineEditorView.post {
            timelineEditorView.apply {
                thumbnail = viewModel.extractSingleThumbnail(thumbnailWidth.toInt(), thumbnailHeight.toInt())
            }
        }

        closeButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        saveButton.setOnClickListener {
            viewModel.applyChanges()
        }

        viewModel.startPositionMs.onEach {
            Log.d("XYU", "$it")
            exoPlayer.stop()
            exoPlayer.setMediaItem(mediaItem, it)
            exoPlayer.prepare()
            exoPlayer.play()
        }.flowWithLifecycle(lifecycle).launchIn(lifecycle.coroutineScope)

        viewModel.endPositionMs.onEach {
            exoPlayer.stop()
            exoPlayer.setMediaItem(mediaItem, viewModel.startPositionMs.value)
            exoPlayer.prepare()
            exoPlayer.play()
        }.flowWithLifecycle(lifecycle).launchIn(lifecycle.coroutineScope)

        val progress = ProgressDialog(requireContext())
        progress.setTitle("Loading")
        val message = "Wait while loading... "
        progress.setMessage(message)
        progress.setCancelable(false)
        viewModel.isApplyingChanges.onEach {
            if (it) {
                wasLoaded = true
                exoPlayer.stop()
                progress.show()
            } else if (wasLoaded) {
                progress.dismiss()
                viewModel.afterApplyingChanges()
                requireActivity().onBackPressed()
            }
        }.flowWithLifecycle(lifecycle).launchIn(lifecycle.coroutineScope)
    }
}