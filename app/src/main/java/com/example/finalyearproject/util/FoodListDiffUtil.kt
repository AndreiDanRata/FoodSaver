package com.example.finalyearproject.util

import com.example.finalyearproject.models.FoodItemModel
import androidx.recyclerview.widget.DiffUtil


class  FoodListDiffUtil(
    private val oldList: List<FoodItemModel>,
    private val newList: List<FoodItemModel>
): DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] === newList[newItemPosition]
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

}