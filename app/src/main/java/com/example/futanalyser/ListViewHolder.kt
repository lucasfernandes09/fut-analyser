package com.example.futanalyser

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.futanalyser.model.Match

class ListViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val title: TextView = itemView.findViewById(R.id.mtrl_list_item_text)
    private val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)

    companion object {
        fun create(parent: ViewGroup): ListViewHolder {
            return ListViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.layout_list, parent, false)
            )
        }
    }

    fun bind(item: Match, itemSelectedListener: (Boolean, Match) -> Unit) {
        with(itemView) {
            title.text = item.homeVsAway
            if (item.link == null)
                checkBox.visibility = View.INVISIBLE
            else {
                checkBox.visibility = View.VISIBLE
                checkBox.setOnCheckedChangeListener { _, isChecked -> itemSelectedListener(isChecked, item) }
            }
        }
    }
}
