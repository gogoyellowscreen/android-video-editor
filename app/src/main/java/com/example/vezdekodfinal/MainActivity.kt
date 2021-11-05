package com.example.vezdekodfinal

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.example.vezdekodfinal.ui.editor.ChooseStickerFragment
import com.example.vezdekodfinal.ui.editor.VideoEditorCropFragment
import com.example.vezdekodfinal.ui.editor.VideoEditorFragment
import com.example.vezdekodfinal.ui.editor.VideoEditorStickersFtagment
import com.example.vezdekodfinal.ui.main.MainFragment

private const val PICK_FROM_GALLERY = 228

class MainActivity : AppCompatActivity(), MainFragment.Callbacks, VideoEditorFragment.Callbacks,
 ChooseStickerFragment.Callbacks, VideoEditorStickersFtagment.Callbacks{

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        requestPermissions()
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance())
                    .commitNow()
        }
    }

    fun requestPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.INTERNET), PICK_FROM_GALLERY)
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.INTERNET), PICK_FROM_GALLERY)
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.INTERNET), PICK_FROM_GALLERY)
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.INTERNET), PICK_FROM_GALLERY)
        }
    }

    override fun onVideoChased(path: Uri) {
        val editorFragment = VideoEditorFragment.newInstance(path.toString())
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, editorFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onCropButtonClick(tmpFilePath: String, videoDuration: Long) {
        val cropFragment = VideoEditorCropFragment.newInstance(tmpFilePath, videoDuration)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, cropFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onStickersButtonClick(tmpFilePath: String) {
        val stickersFragment = VideoEditorStickersFtagment.newInstance(tmpFilePath)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, stickersFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onStickerChased(stickerPath: String) {
        (supportFragmentManager.findFragmentById(R.id.container) as? VideoEditorStickersFtagment)?.onStickerChased(stickerPath)
    }

    override fun onViewCreated() {
        val chooseStickerFragment = ChooseStickerFragment.newInstance()
        chooseStickerFragment.show(supportFragmentManager, "228 ETO SILA")
    }
}