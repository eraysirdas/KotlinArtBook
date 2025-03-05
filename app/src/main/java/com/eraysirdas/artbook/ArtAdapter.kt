package com.eraysirdas.artbook

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.eraysirdas.artbook.databinding.RecycRowBinding

class ArtAdapter(private var artBookList : ArrayList<ArtModel>) : RecyclerView.Adapter<ArtAdapter.ViewHolder>() {

    class ViewHolder (val binding: RecycRowBinding) :RecyclerView.ViewHolder(binding.root) {


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RecycRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return artBookList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.rowtv.text= artBookList[position].artName

        holder.itemView.setOnClickListener{
            val intent = Intent(holder.itemView.context,UploadActivity::class.java)
            intent.putExtra("info","old")
            intent.putExtra("id",artBookList[position].id)
            holder.itemView.context.startActivity(intent)
        }
    }
}