package com.example.futanalyser

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.futanalyser.model.Match

class ListAdapter(
    private val items: List<Match>,
    private val itemSelectedListener: (Boolean, Match) -> Unit
) : RecyclerView.Adapter<ListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ListViewHolder.create(parent)

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        holder.bind(items[position], itemSelectedListener)
    }

    override fun getItemCount() = items.size
}
