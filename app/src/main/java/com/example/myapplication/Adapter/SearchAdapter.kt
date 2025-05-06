package com.example.myapplication.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.Model.CoffeeItem
import com.example.myapplication.R

class SearchAdapter(private var coffeeList: ArrayList<CoffeeItem>) :
    RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.search_txt_title)
        val price: TextView = view.findViewById(R.id.search_txt_price) // Added price TextView
        val image: ImageView = view.findViewById(R.id.search_img)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.viewholder_search, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val coffee = coffeeList[position]
        holder.title.text = coffee.title
        holder.price.text = "₹${coffee.price}" // Binding price

        // Load the first image from the list
        if (coffee.picUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context).load(coffee.picUrl[0]).into(holder.image)
        } else {
            holder.image.setImageResource(R.drawable.coffee) // Default image if no URL
        }
    }

    override fun getItemCount(): Int = coffeeList.size

    fun updateList(newList: ArrayList<CoffeeItem>) {
        coffeeList = newList
        notifyDataSetChanged()
    }
}
