package dev.matyaqubov.internal_externalstorages.adapter

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import dev.matyaqubov.internal_externalstorages.R

class PhotoInternalAdapter(var context: Context, var items:ArrayList<Bitmap>): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]

        if (holder is ViewHolder){
            val iv_image = holder.iv_image

            iv_image.setImageBitmap(item)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ViewHolder(var view: View):RecyclerView.ViewHolder(view){
        var iv_image: ImageView

        init {
            iv_image = view.findViewById(R.id.imageView)
        }
    }
}