package dev.matyaqubov.internal_externalstorages.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dev.matyaqubov.internal_externalstorages.R

class PhotoExternalAdapter(var context: Context, var items:ArrayList<Uri>): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]

        if (holder is ViewHolder){
            val iv_image = holder.iv_image
            Glide.with(iv_image.context).load(item).into(iv_image)
            //iv_image.setImageURI(item)
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