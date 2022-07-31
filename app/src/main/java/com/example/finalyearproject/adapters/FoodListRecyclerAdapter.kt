
package com.example.finalyearproject.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.finalyearproject.R
import com.example.finalyearproject.models.FoodItemModel
import com.google.firebase.database.DatabaseReference

// todo implement the other classes from the tutorial for recycler in a fragment  https://medium.com/inside-ppl-b7/recyclerview-inside-fragment-with-android-studio-680cbed59d84


 class FoodListRecyclerAdapter : RecyclerView.Adapter<FoodListRecyclerAdapter.ViewHolder>() {

    private val date = arrayOf(
        "d116df5",
        "36ffc75", "f5cfe78", "5b87628",
        "db8d14e", "9913dc4", "e120f96",
        "466251b"
    )

    private val name = arrayOf(
        "Kekayaan", "Teknologi",
        "Keluarga", "Bisnis",
        "Keluarga", "Hutang",
        "Teknologi", "Pidana"
    )
    private lateinit var database: DatabaseReference



    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var name: TextView
        var date: TextView


        init {
            name = itemView.findViewById(R.id.item_name_textView)
            date = itemView.findViewById(R.id.item_expiration_textView)


/*itemView.setOnClickListener {
                var position: Int = getAdapterPosition()
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

     override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {

         //for (el in database.child())
         viewHolder.name.text = name[i]
         viewHolder.date.text = date[i]

     }

     override fun getItemCount(): Int {
         return name.size
     }

     fun setItems(list: List<FoodItemModel>?) {
            //TODO
     }
 }

