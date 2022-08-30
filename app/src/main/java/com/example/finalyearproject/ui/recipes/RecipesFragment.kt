package com.example.finalyearproject.ui.recipes

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.finalyearproject.MainActivity
import com.example.finalyearproject.R
import com.example.finalyearproject.adapters.RecipesRecyclerAdapter
import com.example.finalyearproject.barcodescanner.BarcodeScannerActivity
import com.example.finalyearproject.models.RecipeModel
import com.example.finalyearproject.models.FoodItemModel
import com.example.finalyearproject.ui.instructions.InstructionsActivity
import com.example.finalyearproject.util.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_food_list.view.*
import kotlinx.android.synthetic.main.fragment_recipes.view.*
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import kotlin.collections.ArrayList


class RecipesFragment : Fragment() {

    private var database: DatabaseReference = Firebase.database("https://finalyearproject-3d868-default-rtdb.europe-west1.firebasedatabase.app").reference
    private var userFirebaseUID: String = FirebaseAuth.getInstance().currentUser!!.uid
    private var foodList: MutableList<FoodItemModel> = ArrayList<FoodItemModel>()

    private var recipeList: MutableList<RecipeModel> = ArrayList<RecipeModel>()
    private var recipeIds:  MutableList<Int> = ArrayList<Int>()

    private lateinit var mView: View
    private val mmAdapter by lazy { RecipesRecyclerAdapter(recipeList) } //add here the recipe list


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_recipes,container,false)

        readDatabase()

        redirectToInstructions()
        return mView
    }

    /**
     * TAKE THE ITEMS CLOSE TO EXPIRE AND CREATE AN ARRAY => RANDOMLY CHOOSE A FEW OF THE ITEMS AND PROVIDE RECIPES
     *
     */
    private fun getRecipes() {

        var counter = 0
        var ingredients : String = ""

       loop@ for(item in foodList) {

           if( counter > 5 || counter > foodList.size) break@loop

           ingredients += item.itemName.replace(" ","")  + ",+"
           counter ++
        }

        ingredients = ingredients.dropLast(2)

        var urlIngredients= Constants.SEARCH_BY_INGREDIENTS_BASE_URL + ingredients + "&number=2&ranking=1&ignorePantry=true"
        //Log.d("API_RESPONSE_GET_RECIPES", urlIngredients)

        fetchAPIData(urlIngredients).start()
        Thread.sleep(5000) //TODO LOAD DATA WHEN STARTING THE APP https://stackoverflow.com/questions/71635342/recyclerview-loading-data-from-web-api-how-to-prevent-that-the-recyclerview-is

        Log.d("API_RESPONSE_GET_RECIPES_DATA_RECYCLERVIEW",recipeList.first().title+"xxxxxxx"+recipeList.size)
        setupRecyclerView()
    }

    /**
     *  Returns multiple querry results RecipeModels from SpoonecularAPI
     *
     *  Takes a string as an input for the url + int value(1 or 2):
     *  1 for Search Recipes by Ingredients :
     *  api example: https://api.spoonacular.com/recipes/findByIngredients?apiKey=def03f97eb664436ac3a62c793e582a7&ingredients=apples,+flour,+sugar&number=5&ranking=1&ignorePantry=true
     *
     *  2 for Get Recipe Information(this one has recipe web link):
     *  api example: https://api.spoonacular.com/recipes/673463/information?apiKey=def03f97eb664436ac3a62c793e582a7
     *
     */
    private fun fetchAPIData(urlString: String): Thread {
        return Thread {

            try {

                val url =URL(urlString)
                val connection = url.openConnection() as HttpsURLConnection
                Log.d("API_RESPONSE_GET_RECIPES_CONNECTION_RESPONSE_CODE", connection.responseCode.toString())
                if (connection.responseCode == 200) {
                    val inputSystem = connection.inputStream
                    val inputStreamReader = InputStreamReader(inputSystem, "UTF-8")

                    val typeToken = object : TypeToken<List<RecipeModel>>() {}.type
                    val recipeModel = Gson().fromJson<List<RecipeModel>>(inputStreamReader, typeToken)

                    for(recipe in recipeModel) {
                        recipeIds.add(recipe.id)
                    }


                    inputStreamReader.close()
                    inputSystem.close()

                } else {
                    Log.d("API_RESPONSE_GET_RECIPES_NO_CODE_API", "")

                }

                for(id in recipeIds){

                    var urlRecipe= Constants.SEARCH_BY_ID_BASE_URL + id + "/information?includeNutrition=false&apiKey=def03f97eb664436ac3a62c793e582a7"
                    val url2 =URL(urlRecipe)

                    val connection = url2.openConnection() as HttpsURLConnection

                    if (connection.responseCode == 200) {
                        val inputSystem = connection.inputStream
                        val inputStreamReader = InputStreamReader(inputSystem, "UTF-8")

                        var recipeModel = Gson().fromJson(inputStreamReader, RecipeModel::class.java)

                        recipeList.add(recipeModel) //TO PARSE HTML TEXT Jsoup.parse(recipeApiRequest.summary).text()
                        //Log.d("API_RESPONSE_GET_RECIPES",   Jsoup.parse(recipeApiRequest.summary).text())

                        inputStreamReader.close()
                        inputSystem.close()
                    }
                }



            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun readDatabase() {

        database.child("foodItems").child(userFirebaseUID).get().addOnSuccessListener {
            //Log.d("list json", it.toString())
            for (el in it.children) {
                val name = el.child("itemName").value.toString()

                foodList.add(FoodItemModel(name, "", "",false))
            }
            getRecipes()

        }.addOnFailureListener {
            Log.e("firebase", "Error getting data", it)
        }
    }

    private fun setupRecyclerView() {
        mView.recipes_recyclerview.adapter = mmAdapter
        mView.recipes_recyclerview.layoutManager = LinearLayoutManager(activity)
        mView.recipes_recyclerview.hasFixedSize()



    }

    private fun redirectToInstructions(){
        mmAdapter.onItemClick = { currentRecipe ->
            val url = currentRecipe.sourceUrl

            var bundle = Bundle()
            bundle.putString("url", url)

            val intent =  Intent(activity, InstructionsActivity::class.java)

            intent.putExtras(bundle)

            startActivity(intent)
        }
    }

}