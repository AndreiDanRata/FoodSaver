package com.example.finalyearproject.bindingadapters

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.finalyearproject.adapters.FoodListRecyclerAdapter
import com.example.finalyearproject.models.FoodItemModel

// RecyclerView
@BindingAdapter("setItems")
fun setItems(recyclerView: RecyclerView, list: List<FoodItemModel>?) {
    (recyclerView.adapter as FoodListRecyclerAdapter).setItems(list)
}
// RecyclerView