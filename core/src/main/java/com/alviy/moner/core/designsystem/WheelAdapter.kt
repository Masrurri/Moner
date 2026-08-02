package com.alviy.moner.core.designsystem

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.recyclerview.widget.RecyclerView
import com.alviy.moner.core.R
import com.alviy.moner.core.databinding.ItemWheelBinding
import com.alviy.moner.core.common.ext.getDrawable

class WheelAdapter(private val items: List<String>) : RecyclerView.Adapter<WheelAdapter.WheelViewHolder>() {

    private var selectedIndex = 0

    inner class WheelViewHolder(val binding: ItemWheelBinding) : RecyclerView.ViewHolder(binding.root) {
        fun setSelectedItem(isSelected: Boolean) = with(binding.tvItemWheel) {
            if (isSelected) {
               setTextColor(Color.BLACK)
                textSize = 24F
            } else {
                setTextColor(Color.GRAY)
                textSize = 18F
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WheelViewHolder {
        val binding = ItemWheelBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WheelViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WheelViewHolder, position: Int) {
        holder.binding.tvItemWheel.text = items[position]
        holder.setSelectedItem(position == selectedIndex)
    }

    override fun getItemCount(): Int = items.size

    /** Call this from DialogFragment when center item changes */
    fun setSelectedItem(index: Int) {
        val previousIndex = selectedIndex
        selectedIndex = index
        if (previousIndex != selectedIndex) {
            notifyItemChanged(previousIndex)
            notifyItemChanged(selectedIndex)
        }
    }
}