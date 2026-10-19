package com.alviy.moner.core.designsystem.adapter

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alviy.moner.core.R
import com.alviy.moner.core.databinding.ItemWheelBinding

/** Items of [com.alviy.moner.core.designsystem.dialog.WheelDialog]; the centered item is emphasised. */
class WheelAdapter(private val items: List<String>) : RecyclerView.Adapter<WheelAdapter.ViewHolder>() {

    private var selectedIndex = 0

    class ViewHolder(val binding: ItemWheelBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: String, isSelected: Boolean) = with(binding.tvItemWheel) {
            text = item
            val colorRes = if (isSelected) R.color.color_wheel_item_selected else R.color.color_wheel_item_unselected
            val sizeRes = if (isSelected) R.dimen.text_24 else R.dimen.text_18
            setTextColor(context.getColor(colorRes))
            setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(sizeRes))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemWheelBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position == selectedIndex)
    }

    override fun getItemCount(): Int = items.size

    /** Call when the snapped (center) item changes. */
    fun setSelectedItem(index: Int) {
        val previousIndex = selectedIndex
        selectedIndex = index
        if (previousIndex != selectedIndex) {
            notifyItemChanged(previousIndex)
            notifyItemChanged(selectedIndex)
        }
    }
}
