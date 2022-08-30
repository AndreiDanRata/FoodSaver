package com.example.finalyearproject.adapters

import android.app.PendingIntent.getActivity
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.finalyearproject.MainActivity
import com.example.finalyearproject.R
import com.example.finalyearproject.databinding.RecipesRowLayoutBinding
import com.example.finalyearproject.models.FoodItemModel
import com.example.finalyearproject.models.RecipeModel
import com.example.finalyearproject.ui.foodlist.FoodListFragment
import com.example.finalyearproject.ui.instructions.InstructionsActivity
import com.google.firebase.database.DatabaseReference
import kotlinx.android.synthetic.main.foodlist_row_layout.view.*
import java.security.AccessController.getContext

class RecipesRecyclerAdapter(
    var list: MutableList<RecipeModel>,
) : RecyclerView.Adapter<RecipesRecyclerAdapter.MyViewHolder>() {


    /*inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var rowView = itemView
        var title: TextView = rowView.findViewById(R.id.item_name_textView)
        var sumarry: TextView = rowView.findViewById(R.id.item_expiration_textView)


    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val v = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.foodlist_row_layout, viewGroup, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {


        val recipeModel = list[position]

        viewHolder.title.text = recipeModel.title
        viewHolder.sumarry.text = recipeModel.summary
        viewHolder.image.text = recipeModel.image
        viewHolder.aggregatelikes.text = recipeModel.aggregateLikes

    }

    override fun getItemCount(): Int {
        return list.size
    }*/

    //private var recipes = emptyList<RecipeModel>()


    var onItemClick: ((RecipeModel) -> Unit)? = null

    class MyViewHolder(private val binding: RecipesRowLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(result: RecipeModel) {
            binding.result = result
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): MyViewHolder {
                var layoutInflater = LayoutInflater.from(parent.context)
                val binding = RecipesRowLayoutBinding.inflate(layoutInflater, parent, false)
                return MyViewHolder(binding)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val currentRecipe = list[position]

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(currentRecipe)

        }

        holder.bind(currentRecipe)
    }

    override fun getItemCount(): Int {
        return list.size
    }
}