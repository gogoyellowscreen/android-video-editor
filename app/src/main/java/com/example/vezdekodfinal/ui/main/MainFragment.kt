package com.example.vezdekodfinal.ui.main

import android.R.attr
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.vezdekodfinal.R


private const val VIDEO_CHOOSER = 1

class MainFragment : Fragment() {

    interface Callbacks {
        fun onVideoChased(path: Uri)
    }

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var backNavButton: ImageView
    private lateinit var videoChooserButton: ImageView
    private lateinit var callbacks: Callbacks

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        backNavButton = view.findViewById(R.id.back_nav_button)
        videoChooserButton = view.findViewById(R.id.video_chooser)
    }

    override fun onStart() {
        super.onStart()

        callbacks = requireActivity() as Callbacks
        backNavButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        videoChooserButton.setOnClickListener {
            val intent = Intent()
            intent.type = "video/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(intent, "Select Video"),
                VIDEO_CHOOSER
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            resultCode != Activity.RESULT_OK -> return

            requestCode == VIDEO_CHOOSER && data != null -> {
                val path = data.data ?: return
                // MEDIA GALLERY
                callbacks.onVideoChased(path)
            }
        }
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        // TODO: Use the ViewModel
    }

}