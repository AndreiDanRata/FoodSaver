package com.example.finalyearproject.ui.foodlist


import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.finalyearproject.R
import kotlinx.android.synthetic.main.fragment_food_list.*
import kotlinx.android.synthetic.main.fragment_food_list.view.*
import java.util.*
import com.example.finalyearproject.MainActivity as ComExampleFinalyearprojectMainActivity


class FoodListFragment : Fragment() {



    override fun onCreate(savedInstanceState: Bundle?) {
        // call the super class onCreate to complete the creation of activity like
        // the view hierarchy
        super.onCreate(savedInstanceState)



    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_food_list, container, false)


        /*TODO Change the date format*/
        val dateTextView: TextView = view.findViewById(R.id.displayDate_textView)
        val sdf = SimpleDateFormat("yyyy.MM.dd G") /*"yyyy.MM.dd G 'at' HH:mm:ss z"*/
        val currentDateandTime = sdf.format(Date())
        dateTextView.text = currentDateandTime

        view.food_recyclerview.layoutManager = LinearLayoutManager(com.example.finalyearproject.MainActivity) //TODO RecyclerView in foodlist
        // fragment                                                                                            //TODO Make card dissaper 3 seconds after clicked(also include cancel if clicked again)
        view.food_recyclerview.adapter = MainAdapter()

        return view
    }

}