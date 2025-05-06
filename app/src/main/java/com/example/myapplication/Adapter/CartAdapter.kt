package com.example.myapplication.Adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.example.myapplication.Helper.ChangeNumberItemsListener
import com.example.myapplication.Helper.ManagementCart
import com.example.myapplication.Model.ItemsModel
import com.example.myapplication.databinding.ViewholderCartBinding

class CartAdapter(
    private val listItemSelected: ArrayList<ItemsModel>,
    context: Context,
    var changeNumberItemsListener: ChangeNumberItemsListener? = null
) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    class ViewHolder(val binding: ViewholderCartBinding) : RecyclerView.ViewHolder(binding.root)

    private val managementCart = ManagementCart(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartAdapter.ViewHolder {
        val binding = ViewholderCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartAdapter.ViewHolder, position: Int) {
        // Check if the position is within the valid range of the list
        if (position >= 0 && position < listItemSelected.size) {
            val item = listItemSelected[position]
            holder.binding.titleCarttttxt.text = item.title
            holder.binding.feeEachItem.text = "₹${item.price}"
            holder.binding.totalEachItem.text = "₹${Math.round(item.numberInCart * item.price)}"
            holder.binding.numberItemCartTxt.text = item.numberInCart.toString()

            Glide.with(holder.itemView.context)
                .load(item.picUrl[0])
                .apply(RequestOptions().transform(CenterCrop()))
                .into(holder.binding.picCartt)

            // In onBindViewHolder
            holder.binding.removeCart.setOnClickListener {
                // Capture the current position and size
                val currentPosition = holder.adapterPosition
                val currentSize = listItemSelected.size

                if (currentPosition in listItemSelected.indices) {
                    val itemToRemove = listItemSelected[currentPosition]
                    managementCart.removeItem(itemToRemove, object : ChangeNumberItemsListener {
                        override fun onChanged() {
                            // Double-check position validity after async operation
                            if (currentPosition in listItemSelected.indices) {
                                listItemSelected.removeAt(currentPosition)
                                notifyItemRemoved(currentPosition)
                                notifyItemRangeChanged(currentPosition, listItemSelected.size)
                                changeNumberItemsListener?.onChanged()
                            } else {
                                Log.e("CartAdapter", "Position $currentPosition is invalid for removal. List size: $currentSize")
                            }
                        }
                    })
                } else {
                    Log.e("CartAdapter", "Invalid position: $currentPosition. List size: $currentSize")
                }
            }

            // Handle plus item click
            holder.binding.plusCartItem.setOnClickListener {
                // Validate list and position before performing operation
                if (listItemSelected.isNotEmpty() && position >= 0 && position < listItemSelected.size) {
                    managementCart.plusItem(listItemSelected, position, object : ChangeNumberItemsListener {
                        override fun onChanged() {
                            notifyDataSetChanged()
                            changeNumberItemsListener?.onChanged()
                        }
                    })
                } else {
                    // Handle error, maybe show a message or log
                    Log.e("CartAdapter", "Invalid position or empty list")
                }
            }

            // Handle minus item click
            holder.binding.minusCartItem.setOnClickListener {
                // Validate list and position before performing operation
                if (listItemSelected.isNotEmpty() && position >= 0 && position < listItemSelected.size) {
                    managementCart.minusItem(listItemSelected, position, object : ChangeNumberItemsListener {
                        override fun onChanged() {
                            notifyDataSetChanged()
                            changeNumberItemsListener?.onChanged()
                        }
                    })
                } else {
                    // Handle error, maybe show a message or log
                    Log.e("CartAdapter", "Invalid position or empty list")
                }
            }
        } else {
            // If the position is out of bounds, log an error or handle gracefully
            Log.e("CartAdapter", "Invalid position $position for item in list")
        }
    }

    override fun getItemCount(): Int = listItemSelected.size
}
