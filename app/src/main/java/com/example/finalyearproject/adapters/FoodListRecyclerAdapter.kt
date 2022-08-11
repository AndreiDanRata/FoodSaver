package com.example.finalyearproject.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.finalyearproject.R
import com.example.finalyearproject.models.FoodItemModel
import com.example.finalyearproject.ui.foodlist.FoodListFragment
import com.google.firebase.database.DatabaseReference
import kotlinx.android.synthetic.main.foodlist_row_layout.view.*

// todo implement the other classes from the tutorial for recycler in a fragment  https://medium.com/inside-ppl-b7/recyclerview-inside-fragment-with-android-studio-680cbed59d84


class FoodListRecyclerAdapter(
    var list: MutableList<FoodItemModel>,
    val database: DatabaseReference,
    val fragment: FoodListFragment,
    val userFirebaseUID: String
) : RecyclerView.Adapter<FoodListRecyclerAdapter.ViewHolder>() {

    var onItemClick: ((FoodItemModel, Int) -> Unit)? = null

    var onItemLongClick: ((FoodItemModel, Int) -> Unit)? = null

    lateinit var delete_btn: Button

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var rowView = itemView
        var name: TextView
        var date: TextView


        init {
            name = rowView.findViewById(R.id.item_name_textView)
            date = rowView.findViewById(R.id.item_expiration_textView)


        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val v = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.foodlist_row_layout, viewGroup, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        val foodItemModel = list[position]

        viewHolder.name.text = foodItemModel.itemName
        viewHolder.date.text = foodItemModel.itemExpirationDate
        viewHolder.rowView.tag =
            foodItemModel.key   //TAG NEEDED TO DELETE THE DATA FROM THE DATABASE

        //DONATIONS
        if(foodItemModel.toDonate == true) {
            viewHolder.rowView.setBackgroundColor(ContextCompat.getColor(fragment.requireContext(), R.color.green))
        } else {
            viewHolder.rowView.setBackgroundColor(ContextCompat.getColor(fragment.requireContext(), R.color.white))
        }

        //deletes selected row after the recyclerview is refreshed
        delete_btn = viewHolder.rowView.findViewById(R.id.delete_button)
        delete_btn.setOnClickListener {
            viewHolder.rowView.delete_button.setBackgroundColor(Color.RED)//TODO CHANGE IT TO AN X SIGN AND MAKE IT VISIBLE FOR 3 SECONDS
            val key = viewHolder.rowView.tag
            database.child("foodItems").child(userFirebaseUID).child(key.toString()).removeValue()
            list.removeAt(position)
            notifyItemRemoved(position)
        }


        viewHolder.rowView.setOnClickListener {   //short click for bottomsheet
            onItemClick?.invoke(foodItemModel, position)

        }

        viewHolder.rowView.setOnLongClickListener {   //long click for donations
             onItemLongClick?.invoke(foodItemModel, position)

            return@setOnLongClickListener true

        }


    }

    public fun updateItem(updateIndex: Int, item: FoodItemModel) {
        list[updateIndex] = item
        this.notifyItemChanged(updateIndex)
        //Log.d("NEW_ELEMENT",list[updateIndex].toString())
    }
    public fun addItem(item:FoodItemModel) {
        list.add(item)
        this.notifyItemInserted(list.size-1)
    }

    override fun getItemCount(): Int {
        return list.size
    }


}
