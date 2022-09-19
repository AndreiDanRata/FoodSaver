package com.example.finalyearproject.adapters

import android.os.CountDownTimer
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

/**
 * Adapter class for the food list recycler View
 */
class FoodListRecyclerAdapter(
    var list: MutableList<FoodItemModel>,
    private val database: DatabaseReference,
    val fragment: FoodListFragment,
    private val userFirebaseUID: String
) : RecyclerView.Adapter<FoodListRecyclerAdapter.ViewHolder>() {

    var onItemClick: ((FoodItemModel, Int) -> Unit)? = null

    var onItemLongClick: ((FoodItemModel, Int) -> Unit)? = null

    private var dismissed: Boolean = true

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var rowView = itemView
        var name: TextView = rowView.findViewById(R.id.item_name_textView)
        var date: TextView = rowView.findViewById(R.id.item_expiration_textView)
        var deleteBtn: Button = rowView.findViewById(R.id.delete_button)
        val undoButton: Button = rowView.findViewById(R.id.undo_button)

        init {
            deleteBtn.setOnClickListener {

                rowView.item_normal.visibility = View.GONE
                rowView.item_deleted.visibility = View.VISIBLE


                val timer = object: CountDownTimer(4000, 990) {
                    override fun onTick(millisUntilFinished: Long) {
                    }

                    override fun onFinish() {
                        if(dismissed) {
                            var positionOnDelete = adapterPosition
                            val key = rowView.tag
                            database.child("foodItems").child(userFirebaseUID).child(key.toString()).removeValue()
                            rowView.item_normal.visibility = View.VISIBLE
                            rowView.item_deleted.visibility = View.GONE
                            Log.d("TEEEEEEEEST", positionOnDelete.toString())
                            list.removeAt(positionOnDelete)
                            notifyItemRemoved(positionOnDelete)
                            //notifyItemRangeChanged(positionOnDelete,list.size)
                            notifyDataSetChanged()

                        } else {
                            rowView.item_normal.visibility = View.VISIBLE
                            rowView.item_deleted.visibility = View.GONE
                        }
                    }
                }
                timer.start()
            }

            undoButton.setOnClickListener {
                dismissed = false
                rowView.item_normal.visibility = View.VISIBLE
                rowView.item_deleted.visibility = View.GONE
            }
        }



    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val v = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.foodlist_row_layout, viewGroup, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        val foodItemModel = list[viewHolder.adapterPosition]

        viewHolder.name.text = foodItemModel.itemName
        viewHolder.rowView.tag = foodItemModel.key   //TAG NEEDED TO DELETE THE DATA FROM THE DATABASE
        if(foodItemModel.itemExpirationDate == "11/11/2000") {
            viewHolder.date.text = "Tap to add"
        }else {
            viewHolder.date.text = foodItemModel.itemExpirationDate
        }


        //DONATIONS
        if(foodItemModel.toDonate) {
            viewHolder.rowView.setBackgroundColor(ContextCompat.getColor(fragment.requireContext(), R.color.toDonate))
        } else {
            viewHolder.rowView.setBackgroundColor(ContextCompat.getColor(fragment.requireContext(), R.color.white))
        }

        viewHolder.rowView.setOnClickListener {   //short click for bottomsheet
            onItemClick?.invoke(foodItemModel, viewHolder.adapterPosition)

        }

        viewHolder.rowView.setOnLongClickListener {   //long click for donations
            onItemLongClick?.invoke(foodItemModel, viewHolder.adapterPosition)

            return@setOnLongClickListener true
        }
    }

    fun updateItem(updateIndex: Int, item: FoodItemModel) {
        list[updateIndex] = item
        this.notifyItemChanged(updateIndex)
        //Log.d("NEW_ELEMENT",list[updateIndex].toString())
    }
    fun addItem(item:FoodItemModel) {
        list.add(item)
        this.notifyItemInserted(list.size-1)
    }

    override fun getItemCount(): Int {
        return list.size
    }


}
