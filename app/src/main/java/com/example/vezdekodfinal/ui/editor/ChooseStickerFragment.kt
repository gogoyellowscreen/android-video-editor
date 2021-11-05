package com.example.vezdekodfinal.ui.editor

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vezdekodfinal.R
import com.example.vezdekodfinal.ui.application.NameToBitmap
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ChooseStickerFragment : BottomSheetDialogFragment() {

    interface Callbacks {
        fun onStickerChased(stickerPath: String)
    }

    companion object {
        fun newInstance() = ChooseStickerFragment()
    }

    private lateinit var stickersRecyclerView: RecyclerView
    private lateinit var assetManager: AssetManager

    private var callback = Runnable {}

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.chose_stricker_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        stickersRecyclerView = view.findViewById(R.id.stickers_recycler_view)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        assetManager = requireActivity().assets
        stickersRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        stickersRecyclerView.adapter = StickerAdapter(assetManager.list("stickers")?.toList() ?: emptyList())
    }

    override fun onDetach() {
        super.onDetach()

        callback.run()
    }

    private inner class StickerHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById<ImageView>(R.id.sticker_view).apply {
            setOnClickListener {
                dismiss()
                (requireActivity() as Callbacks).onStickerChased(stickerPath)
            }
        }

        var stickerPath: String = ""
            set(value) {
                field = value
                NameToBitmap.getIfExists(value)?.let {
                    imageView.setImageBitmap(it)
                } ?: run {
                    val bitmap = BitmapFactory.decodeStream(assetManager.open("stickers/$stickerPath"))
                    NameToBitmap.put(value, bitmap)
                    imageView.setImageBitmap(bitmap)
                }
            }
    }

    private inner class StickerAdapter(val stickers: List<String>) : RecyclerView.Adapter<StickerHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StickerHolder {
            val view = layoutInflater.inflate(R.layout.sticker_item, parent, false)
            return StickerHolder(view)
        }

        override fun onBindViewHolder(holder: StickerHolder, position: Int) {
            val stickerPath = stickers[position]
            holder.stickerPath = stickerPath
        }

        override fun getItemCount(): Int {
            return stickers.size
        }
    }
}
