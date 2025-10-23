package com.example.wirelesslocationstud.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.wirelesslocationstud.data.local.entity.MapCellEntity
import com.example.wirelesslocationstud.databinding.ItemCoordinateTableRowBinding

class CoordinateTableAdapter(
    private val onEditClick: (MapCellEntity) -> Unit,
    private val onDeleteClick: (MapCellEntity) -> Unit
) : ListAdapter<MapCellEntity, CoordinateTableAdapter.CoordinateViewHolder>(CoordinateDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoordinateViewHolder {
        val binding = ItemCoordinateTableRowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CoordinateViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CoordinateViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CoordinateViewHolder(
        private val binding: ItemCoordinateTableRowBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(cell: MapCellEntity) {
            binding.textX.text = cell.x.toString()
            binding.textY.text = cell.y.toString()
            binding.textStrength1.text = cell.strength1.toString()
            binding.textStrength2.text = cell.strength2.toString()
            binding.textStrength3.text = cell.strength3.toString()

            // Show checkmark icon only for custom/form entries
            binding.iconSource.visibility = if (cell.isCustom) {
                android.view.View.VISIBLE
            } else {
                android.view.View.GONE
            }

            binding.buttonEdit.setOnClickListener {
                onEditClick(cell)
            }

            binding.buttonDelete.setOnClickListener {
                onDeleteClick(cell)
            }
        }
    }

    class CoordinateDiffCallback : DiffUtil.ItemCallback<MapCellEntity>() {
        override fun areItemsTheSame(oldItem: MapCellEntity, newItem: MapCellEntity): Boolean {
            return oldItem.x == newItem.x && oldItem.y == newItem.y
        }

        override fun areContentsTheSame(oldItem: MapCellEntity, newItem: MapCellEntity): Boolean {
            return oldItem == newItem
        }
    }
}
