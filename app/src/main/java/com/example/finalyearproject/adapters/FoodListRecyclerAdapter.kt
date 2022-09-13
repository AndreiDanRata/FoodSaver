package com.example.finalyearproject.adapters

import android.os.CountDownTimer
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


class FoodListRecyclerAdapter(
    var list: MutableList<FoodItemModel>,
    private val database: DatabaseReference,
    val fragment: FoodListFragment,
    private val userFirebaseUID: String
) : RecyclerView.Adapter<FoodListRecyclerAdapter.ViewHolder>() {

    var onItemClick: ((FoodItemModel, Int) -> Unit)? = null

    var onItemLongClick: ((FoodItemModel, Int) -> Unit)? = null

    private lateinit var delete_btn: Button
    private var dismissed: Boolean = true

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var rowView = itemView
        var name: TextView = rowView.findViewById(R.id.item_name_textView)
        var date: TextView = rowView.findViewById(R.id.item_expiration_textView)


    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val v = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.foodlist_row_layout, viewGroup, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        val foodItemModel = list[position]

        viewHolder.name.text = foodItemModel.itemName
        viewHolder.rowView.tag = foodItemModel.key   //TAG NEEDED TO DELETE THE DATA FROM THE DATABASE
        if(foodItemModel.itemExpirationDate == "11/11/2000") {
            viewHolder.date.text = "Tap to add"
        }else {
            viewHolder.date.text = foodItemModel.itemExpirationDate
        }


        //DONATIONS
        if(foodItemModel.toDonate) {
            viewHolder.rowView.setBackgroundColor(ContextCompat.getColor(fragment.requireContext(), R.color.green))
        } else {
            viewHolder.rowView.setBackgroundColor(ContextCompat.getColor(fragment.requireContext(), R.color.white))
        }

        //deletes selected row after the recyclerview is refreshed
        delete_btn = viewHolder.rowView.findViewById(R.id.delete_button)
        delete_btn.setOnClickListener {

            viewHolder.rowView.item_normal.visibility = View.GONE
            viewHolder.rowView.item_deleted.visibility = View.VISIBLE


            /*val initialTime = System.currentTimeMillis()
            var now = System.currentTimeMillis()
            while (now - initialTime < 4000) {
                now = System.currentTimeMillis()
            }*/
            val timer = object: CountDownTimer(5000, 4000) {
                override fun onTick(millisUntilFinished: Long) {
                }

                override fun onFinish() {
                    if(dismissed) {
                        val key = viewHolder.rowView.tag
                        database.child("foodItems").child(userFirebaseUID).child(key.toString()).removeValue()
                        list.removeAt(position)
                        notifyItemRemoved(position)
                        notifyItemRangeChanged(position,list.size)
                    } else {
                        viewHolder.rowView.item_normal.visibility = View.VISIBLE
                        viewHolder.rowView.item_deleted.visibility = View.GONE
                    }
                }
            }
            timer.start()








           /* val key = viewHolder.rowView.tag
            database.child("foodItems").child(userFirebaseUID).child(key.toString()).removeValue()
            list.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position,list.size)
            //Error has to do something with the animations of the delete button*/
        }

        val undoButton: Button = viewHolder.rowView.findViewById(R.id.undo_button)
        undoButton.setOnClickListener {
            dismissed = false
            viewHolder.rowView.item_normal.visibility = View.VISIBLE
            viewHolder.rowView.item_deleted.visibility = View.GONE
        }


        viewHolder.rowView.setOnClickListener {   //short click for bottomsheet
            onItemClick?.invoke(foodItemModel, position)

        }

        viewHolder.rowView.setOnLongClickListener {   //long click for donations
             onItemLongClick?.invoke(foodItemModel, position)

            return@setOnLongClickListener true

        }


    }

   /* fun onItemRemove(viewHolder: RecyclerView.ViewHolder, recyclerView: RecyclerView) {

        dismissed = true
        val snackbar = Snackbar
            .make(recyclerView, "ITEM REMOVED", Snackbar.LENGTH_LONG)
            .setAction("UNDO") {
                dismissed = false
            }
            .addCallback(object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    super.onDismissed(transientBottomBar, event);
                    if(dismissed) {
                        val key = viewHolder.rowView.tag
                        database.child("foodItems").child(userFirebaseUID).child(key.toString()).removeValue()
                        list.removeAt(position)
                        notifyItemRemoved(position)
                        notifyItemRangeChanged(position,list.size)
                    }
                }
            }).show()
    }*/

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
