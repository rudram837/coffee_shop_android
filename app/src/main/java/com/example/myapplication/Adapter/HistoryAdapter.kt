package com.example.myapplication.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.myapplication.Model.OrderHistory
import com.example.myapplication.databinding.ViewholderHistoryBinding

class HistoryAdapter(private val historyList: List<OrderHistory>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ViewholderHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = historyList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = historyList.size

    inner class HistoryViewHolder(private val binding: ViewholderHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: OrderHistory) {
            // Set the title of the first item in the order
            binding.titleHistory.text = item.items[0].title // Set title of the first item

            // Set the price of the first item
            binding.priceHistory.text = "₹${item.items[0].price}" // Set price of the first item

            // Set the image using Glide (first image from picUrl list)
            if (item.items[0].picUrl.isNotEmpty()) {
                Glide.with(binding.root.context)
                    .load(item.items[0].picUrl[0]) // Load the first image URL
                    .apply(RequestOptions().centerCrop())
                    .into(binding.picHistory) // Set image into ImageView
            }

            // Set the total amount (from totalAmount in OrderHistory)
            binding.totalAmountHistory.text = "${item.totalAmount}"  // Binding the total amount to the view

            // Set the quantity (numberInCart)
            binding.totalQuantityHistory.text = "Qty: ${item.items[0].numberInCart}"  // Binding the quantity (numberInCart) to the view
        }
    }

}
