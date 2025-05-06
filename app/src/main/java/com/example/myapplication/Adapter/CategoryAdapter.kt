package com.example.myapplication.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Model.CategoryModel
import com.example.myapplication.R
import com.example.myapplication.databinding.ViewholderCategoryBinding

class CategoryAdapter(val items: MutableList<CategoryModel>): RecyclerView.Adapter<CategoryAdapter.Viewholder>() {

    private lateinit var context: Context
    private var selectedPosition=-1
    private var laseSelectedPosition=-1

    inner class Viewholder(val binding: ViewholderCategoryBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryAdapter.Viewholder {
        context=parent.context
        val binding = ViewholderCategoryBinding.inflate(LayoutInflater.from(context), parent, false)
        return Viewholder(binding)
    }

    override fun onBindViewHolder(holder: CategoryAdapter.Viewholder, position: Int) {
        val item = items[position]
        holder.binding.titleCat.text = item.title

        holder.binding.root.setOnClickListener {
            laseSelectedPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(laseSelectedPosition)
            notifyItemChanged(selectedPosition)
        }

        if (selectedPosition == position) {
            holder.binding.titleCat.setBackgroundResource(R.drawable.orange_bg)
        } else {
            holder.binding.titleCat.setBackgroundResource(R.drawable.edittext_bg)
        }
    }

    override fun getItemCount(): Int = items.size
}