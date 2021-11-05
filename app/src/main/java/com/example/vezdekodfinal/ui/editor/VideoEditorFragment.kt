package com.example.vezdekodfinal.ui.editor

import android.annotation.SuppressLint
import android.content.res.AssetManager
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.telecom.Call
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import com.example.vezdekodfinal.R
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.StyledPlayerView

class VideoEditorFragment : Fragment() {

    interface Callbacks {
        fun onCropButtonClick(tmpFilePath: String, videoDuration: Long)
        fun onStickersButtonClick(tmpFilePath: String)
        fun onEffectsButtonClick(tmpFilePath: String)
    }

    companion object {
        private const val PATH = "path"

        fun newInstance(path: String) = VideoEditorFragment().apply {
            arguments = Bundle().apply {
                putString(PATH, path)
            }
        }
    }

    private lateinit var viewModel: VideoEditorViewModel
    private lateinit var exoPlayer: SimpleExoPlayer
    private lateinit var exoPlayerView: StyledPlayerView
    private lateinit var closeButton: ImageView
    private lateinit var saveButton: TextView
    private lateinit var cropButton: ImageView
    private lateinit var stickersButton: ImageView
    private lateinit var effectsButton: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.video_editor_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        closeButton = view.findViewById(R.id.close_button)
        saveButton = view.findViewById(R.id.save_button)
        cropButton = view.findViewById(R.id.times_button)
        stickersButton = view.findViewById(R.id.stickers_button)
        effectsButton = view.findViewById(R.id.effects_button)
        exoPlayer = SimpleExoPlayer.Builder(view.context).build()
        exoPlayerView = view.findViewById(R.id.exo_player_view)
        exoPlayerView.player = exoPlayer
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()

        val mediaItem = MediaItem.fromUri(viewModel.tmpFile.toUri())
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()

        closeButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        saveButton.setOnClickListener {
            viewModel.saveVideo()
        }

        cropButton.setOnClickListener {
            (requireActivity() as Callbacks).onCropButtonClick(viewModel.tmpFile.path, exoPlayer.contentDuration)
        }

        stickersButton.setOnClickListener {
            (requireActivity() as Callbacks).onStickersButtonClick(viewModel.tmpFile.path)
        }

        effectsButton.setOnClickListener {
            (requireActivity() as Callbacks).onEffectsButtonClick(viewModel.tmpFile.path)
        }
    }

    override fun onPause() {
        super.onPause()
        exoPlayer.stop()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(VideoEditorViewModel::class.java)

        val pathString = arguments?.getString(PATH)
        val uri = Uri.parse(pathString)
        viewModel.init(uri, requireContext())
        // TODO: Use the ViewModel
    }

}