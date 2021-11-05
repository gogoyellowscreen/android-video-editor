package com.example.vezdekodfinal.ui.editor

import android.app.ProgressDialog
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_MOVE
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.flowWithLifecycle
import com.example.vezdekodfinal.R
import com.example.vezdekodfinal.ui.application.NameToBitmap
import com.example.vezdekodfinal.ui.dpToPx
import com.example.vezdekodfinal.ui.windowHeight
import com.example.vezdekodfinal.ui.windowWidth
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.StyledPlayerView
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class VideoEditorStickersFtagment : Fragment() {

    interface Callbacks {
        fun onViewCreated()
    }

    companion object {
        private const val TMP_FILE_PATH = "tmp_file_path"

        fun newInstance(tmpFilePath: String) = VideoEditorStickersFtagment().apply {
            arguments = Bundle().apply { putString(TMP_FILE_PATH, tmpFilePath) }
        }
    }

    private lateinit var viewModel: VideoEditorStickersFtagmentViewModel
    private lateinit var closeButton: ImageView
    private lateinit var saveButton: TextView
    private lateinit var addedSticker: ImageView
    private lateinit var addedStickerTouchListener: StickerTouchListener
    private lateinit var sticker: Bitmap
    private lateinit var tmpFilePath: String
    private lateinit var exoPlayerView: StyledPlayerView
    private lateinit var exoPlayer: SimpleExoPlayer
    private var wasSaved = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.video_editor_stickers_ftagment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        addedSticker = view.findViewById(R.id.added_sticker)
        closeButton = view.findViewById(R.id.close_button)
        saveButton = view.findViewById(R.id.save_button)
        tmpFilePath = arguments?.getString(TMP_FILE_PATH) ?: return
        exoPlayer = SimpleExoPlayer.Builder(view.context).build()
        exoPlayerView = view.findViewById(R.id.exo_player_view)
        exoPlayerView.player = exoPlayer
    }

    override fun onResume() {
        super.onResume()

        wasSaved = false

        viewModel.init(requireActivity())

        val pathString = arguments?.getString(TMP_FILE_PATH) ?: return
        val uri = Uri.parse(pathString)
        // viewModel.init(uri, requireContext())
        val mediaItem = MediaItem.fromUri(uri)

        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()

        closeButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        saveButton.setOnClickListener {
            if (addedSticker.isVisible) {
                val height = windowHeight(requireActivity())
                val width = windowWidth(requireActivity())
                viewModel.saveVideoWithSticker(tmpFilePath, sticker,
                    (addedStickerTouchListener.prevX - addedSticker.width / 2f) / width,
                    (addedStickerTouchListener.prevY - addedSticker.height / 1.5f) / height)
            }
        }

        val progress = ProgressDialog(requireContext())
        progress.setTitle("Loading")
        val message = "Wait while loading... "
        progress.setMessage(message)
        progress.setCancelable(false)
        viewModel.isSaving.onEach {
            if (it && !wasSaved) {
                wasSaved = true
                exoPlayer.stop()
                progress.show()
            } else if (!it && wasSaved) {
                wasSaved = false
                val activity = requireActivity()
                progress.setOnDismissListener { activity.onBackPressed() }
                progress.dismiss()
            }
        }.flowWithLifecycle(lifecycle).launchIn(lifecycle.coroutineScope)

        if (!viewModel.openStickers) {
            exoPlayerView.post {
                viewModel.openStickers = true
                (requireActivity() as Callbacks).onViewCreated()
            }
        }
    }

    override fun onPause() {
        super.onPause()

        exoPlayer.stop()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(VideoEditorStickersFtagmentViewModel::class.java)
        // TODO: Use the ViewModel
    }

    fun onStickerChased(sticker: String) {
        this.sticker = NameToBitmap.getIfExists(sticker) ?: run {
            val bitmap = BitmapFactory.decodeStream(requireActivity().assets.open("stickers/$sticker"))
            NameToBitmap.put(sticker, bitmap)
            bitmap
        }
        addedSticker.setImageBitmap(this.sticker)
        addedStickerTouchListener = StickerTouchListener(addedSticker)
        addedSticker.setOnTouchListener(addedStickerTouchListener)
        addedSticker.visibility = View.VISIBLE
    }

    private inner class StickerTouchListener(val view: ImageView) : View.OnTouchListener {
        var prevX = windowWidth(requireActivity()) / 2f - view.width / 2f
        var prevY = windowHeight(requireActivity()) / 2f - view.height / 1.5f
        var isMoving = false

        private val velocityTracker = VelocityTracker.obtain()

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            velocityTracker.addMovement(event)

            when (event.actionMasked) {
                ACTION_MOVE -> {
                    if (isMoving) {
                        prevX = event.rawX
                        prevY = event.rawY

                        view.x = event.rawX - view.width / 2f
                        view.y = event.rawY - view.height / 1.5f

                        //view.translationZ = dpToPx(50, view.context).toFloat()
                    }
                    isMoving = true

                    return true
                }
                else -> {
                    isMoving = false
                }
            }
            return true
        }
    }
}