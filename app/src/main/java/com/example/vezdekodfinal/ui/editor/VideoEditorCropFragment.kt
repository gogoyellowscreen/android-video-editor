package com.example.vezdekodfinal.ui.editor

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.vezdekodfinal.R
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.StyledPlayerView

class VideoEditorCropFragment : Fragment() {

    companion object {
        fun newInstance() = VideoEditorCropFragment()
    }

    private lateinit var viewModel: VideoEditorCropViewModel
    private lateinit var timelineEditorView: VideoEditorRangeSelectorView
    private lateinit var exoPlayer: SimpleExoPlayer
    private lateinit var exoPlayerView: StyledPlayerView

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
        exoPlayerView.player = exoPlayer
    }

    override fun onPause() {
        super.onPause()
        exoPlayer.stop()
    }

    override fun onResume() {
        super.onResume()

        timelineEditorView.dragHelper = VideoEditorRangeSelectorController(requireContext(), timelineEditorView)
        timelineEditorView.dragHelper?.setListener(viewModel.rangeSelectorListener)
        timelineEditorView.attach(requireContext(), viewModel, lifecycle)

        exoPlayer.play()
    }
}