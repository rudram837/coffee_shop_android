package com.example.myapplication.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.Activity.DetailActivity
import com.example.myapplication.Model.ItemsModel
import com.example.myapplication.databinding.ViewholderBrewBinding

class BrewAdapter(private val items: MutableList<ItemsModel>) : RecyclerView.Adapter<BrewAdapter.Viewholder>() {

    private var context: Context? = null

    class Viewholder(val binding: ViewholderBrewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        context = parent.context
        val binding = ViewholderBrewBinding.inflate(LayoutInflater.from(context), parent, false)
        return Viewholder(binding)
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        val currentItem = items[position]
        holder.binding.titleTxtbrew.text = currentItem.title
        holder.binding.priceTxtbrew.text = "₹${currentItem.price}"
        holder.binding.ratingBarbrew.rating = currentItem.rating.toFloat()
        holder.binding.extraTxtbrew.text = currentItem.extra

        Glide.with(holder.itemView.context)
            .load(currentItem.picUrl[0])
            .into(holder.binding.picbrew)

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, DetailActivity::class.java)
            intent.putExtra("object", items[position])
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = items.size
}
