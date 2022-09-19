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

/**
 * Adapter class for the recipes list recycler View
 */
class RecipesRecyclerAdapter(
    var list: MutableList<RecipeModel>,
) : RecyclerView.Adapter<RecipesRecyclerAdapter.MyViewHolder>() {


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

    override fun onBindViewHolder(viewHolder: MyViewHolder, position: Int) {

        val currentRecipe = list[position]

        viewHolder.itemView.setOnClickListener {
            onItemClick?.invoke(currentRecipe)

        }

        viewHolder.bind(currentRecipe)
    }

    override fun getItemCount(): Int {
        return list.size
    }
}