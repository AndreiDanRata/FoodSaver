
package com.example.finalyearproject.adapters

import android.graphics.Color
import android.graphics.Color.green
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.example.finalyearproject.R
import com.example.finalyearproject.models.FoodItemModel
import com.google.firebase.database.DatabaseReference
import kotlinx.android.synthetic.main.foodlist_row_layout.view.*
import kotlinx.coroutines.delay

// todo implement the other classes from the tutorial for recycler in a fragment  https://medium.com/inside-ppl-b7/recyclerview-inside-fragment-with-android-studio-680cbed59d84


 class FoodListRecyclerAdapter(val list: MutableList<FoodItemModel>, val database : DatabaseReference ) : RecyclerView.Adapter<FoodListRecyclerAdapter.ViewHolder>() {


     lateinit var delete_btn: Button

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var rowView =  itemView
        var name: TextView
        var date: TextView


        init {
            name = rowView.findViewById(R.id.item_name_textView)
            date = rowView.findViewById(R.id.item_expiration_textView)






            //details page + option to edit itemName + itemExpirationDate
           /* rowView.setOnClickListener {
                var position: Int = adapterPosition
                val context = itemView.context
                val intent = Intent(context, DetailPertanyaan::class.java).apply {
                    putExtra("NUMBER", position)
                    putExtra("CODE", itemKode.text)
                    putExtra("CATEGORY", itemKategori.text)
                    putExtra("CONTENT", itemIsi.text)
                }
                context.startActivity(intent)
            }*/
        }
    }

     override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
         val v = LayoutInflater.from(viewGroup.context)
                 .inflate(R.layout.foodlist_row_layout, viewGroup, false)
         return ViewHolder(v)
     }

     override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

         viewHolder.name.text = list[position].itemName
         viewHolder.date.text = list[position].itemExpirationDate
         viewHolder.rowView.tag = list[position].key   //TAG NEEDED TO DELETE THE DATA FROM THE DATABASE


         //deletes selected row after the recyclerview is refreshed
         delete_btn = viewHolder.rowView.findViewById(R.id.delete_button)
         delete_btn.setOnClickListener{
             viewHolder.rowView.delete_button.setBackgroundColor(Color.RED)//TODO CHANGE IT TO AN X SIGN AND MAKE IT VISIBLE FOR 3 SECONDS
             val key = viewHolder.rowView.tag
             database.child("foodItems").child(key.toString()).removeValue()
             list.removeAt(position)
             notifyItemRemoved(position)
         }



     }

     override fun getItemCount(): Int {
         return list.size
     }


     fun setItems(list: List<FoodItemModel>?) {
            //TODO
     }
 }
