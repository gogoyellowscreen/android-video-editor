package com.example.vezdekodfinal.ui.editor

import android.annotation.SuppressLint
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.vezdekodfinal.R
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.StyledPlayerView

class VideoEditorFragment : Fragment() {

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.video_editor_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        closeButton = view.findViewById(R.id.close_button)
        saveButton = view.findViewById(R.id.save_button)
        exoPlayer = SimpleExoPlayer.Builder(view.context).build()
        exoPlayerView = view.findViewById(R.id.exo_player_view)
        exoPlayerView.player = exoPlayer
    }

    @SuppressLint("SetTextI18n")
    override fun onStart() {
        super.onStart()

        val pathString = arguments?.getString(PATH)
        val uri = Uri.parse(pathString)
        viewModel.init(uri, requireContext())
        val mediaItem = MediaItem.fromUri(uri)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()

        closeButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        saveButton.setOnClickListener {
            viewModel.saveVideo()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.stop()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(VideoEditorViewModel::class.java)
        // TODO: Use the ViewModel
    }

}