package com.example.finalyearproject.ui.recipes

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.finalyearproject.R
import com.example.finalyearproject.adapters.RecipesRecyclerAdapter
import com.example.finalyearproject.models.FoodItemModel
import com.example.finalyearproject.models.RecipeModel
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.function.Predicate
import javax.net.ssl.HttpsURLConnection

/**
 * This class queries the recipes api using the items provided in the food list
 * and then shows the recipes in a recyclerview
 */
class RecipesFragment : Fragment() {

    private var database: DatabaseReference =
        Firebase.database("https://finalyearproject-3d868-default-rtdb.europe-west1.firebasedatabase.app").reference
    private var userFirebaseUID: String = FirebaseAuth.getInstance().currentUser!!.uid
    private var foodList: MutableList<FoodItemModel> = ArrayList<FoodItemModel>()

    private var recipeList: MutableList<RecipeModel> = ArrayList<RecipeModel>()
    private var recipeIds: MutableList<Int> = ArrayList<Int>()

    private lateinit var mView: View
    private val mmAdapter by lazy { RecipesRecyclerAdapter(recipeList) }
    private lateinit var emptyTextView: TextView


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_recipes, container, false)
        emptyTextView = mView.findViewById(R.id.empty_textView)
        readDatabase()

        redirectToInstructions()
        return mView
    }

    /**
     * TAKE THE ITEMS CLOSE TO EXPIRE AND CREATE AN ARRAY => RANDOMLY CHOOSE A FEW OF THE ITEMS AND PROVIDE RECIPES
     *
     */
    private fun getRecipes(numRecipes: Int) {

        var counter = 0
        var ingredients: String = ""

        loop@ for (item in foodList) {
            if (counter > 5 || counter > foodList.size) break@loop

            ingredients += item.itemName.replace(" ", "") + ",+"
            counter++
        }

        ingredients = ingredients.dropLast(2)

        var urlIngredients =
            Constants.SEARCH_BY_INGREDIENTS_BASE_URL + ingredients + "&number=" + numRecipes + "&ranking=1&ignorePantry=true"
        Log.d("API_RESPONSE_GET_RECIPES", urlIngredients)
        fetchAPIData(urlIngredients).start()
        Thread.sleep(7000)

        //Log.d("API_RESPONSE_GET_RECIPES_DATA_RECYCLERVIEW", recipeList.first().title + " SIZE " + recipeList.size)

        setupRecyclerView()
    }

    /**
     *  Returns multiple querry results RecipeModels from SpoonecularAPI
     *  Takes a string as an input for the url and
     *  does Search Recipes by Ingredients
     *  and also Get Recipe Information(this one has recipe web link):
     *  api example: https://api.spoonacular.com/recipes/findByIngredients?apiKey=def03f97eb664436ac3a62c793e582a7&ingredients=apples,+flour,+sugar&number=5&ranking=1&ignorePantry=true
     *  api example: https://api.spoonacular.com/recipes/673463/information?apiKey=def03f97eb664436ac3a62c793e582a7
     *
     */
    private fun fetchAPIData(urlString: String): Thread {
        return Thread {

            try {
                val url = URL(urlString)
                val connection = url.openConnection() as HttpsURLConnection
                Log.d("API_RESPONSE_GET_RECIPES_CONNECTION_RESPONSE_CODE", connection.responseCode.toString())
                if (connection.responseCode == 200) {
                    val inputSystem = connection.inputStream
                    val inputStreamReader = InputStreamReader(inputSystem, "UTF-8")
                    val typeToken = object : TypeToken<List<RecipeModel>>() {}.type
                    val recipeModel =
                        Gson().fromJson<List<RecipeModel>>(inputStreamReader, typeToken)

                    for (recipe in recipeModel) {
                        recipeIds.add(recipe.id)
                    }
                    inputStreamReader.close()
                    inputSystem.close()
                } else {
                    Log.d("API_RESPONSE_GET_RECIPES_NO_CODE_API", "")
                }

                if(recipeIds.isNotEmpty()) {
                    emptyTextView.visibility = View.INVISIBLE
                    val randomElements = recipeIds.asSequence().shuffled().take(5).toList()


                    for (id in randomElements) {

                        var urlRecipe = Constants.SEARCH_BY_ID_BASE_URL + id + "/information?includeNutrition=false&apiKey=def03f97eb664436ac3a62c793e582a7"
                        val url2 = URL(urlRecipe)

                        val connection = url2.openConnection() as HttpsURLConnection

                        if (connection.responseCode == 200) {
                            val inputSystem = connection.inputStream
                            val inputStreamReader = InputStreamReader(inputSystem, "UTF-8")

                            var recipeModel =
                                Gson().fromJson(inputStreamReader, RecipeModel::class.java)

                            recipeList.add(recipeModel)
                            //Log.d("API_RESPONSE_GET_RECIPES",   Jsoup.parse(recipeApiRequest.summary).text())

                            inputStreamReader.close()
                            inputSystem.close()
                        }
                    }
                }
                else {
                    emptyTextView.visibility = View.VISIBLE
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    //removes expired items from our food list
    fun <FoodItemModel> removeExpiredItems(
        list: MutableList<FoodItemModel>,
        predicate: Predicate<FoodItemModel>
    ) {
        list.removeIf { x: FoodItemModel -> predicate.test(x) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun readDatabase() {

        database.child("foodItems").child(userFirebaseUID).get().addOnSuccessListener {
            //Log.d("list json", it.toString())
            for (el in it.children) {
                val name = el.child("itemName").value.toString()
                val expirationDate = el.child("itemExpirationDate").value.toString()

                foodList.add(FoodItemModel(name, expirationDate, "", false))
            }


            val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d/M/yyyy")
            val expired = Predicate { item: FoodItemModel ->
                LocalDate.parse(
                    item.itemExpirationDate,
                    dateTimeFormatter
                ) < LocalDate.now()
            }
            removeExpiredItems(foodList, expired)

            //SORT the food items list by the expiration date
            foodList.sortBy { foodItemModel ->
                LocalDate.parse(foodItemModel.itemExpirationDate, dateTimeFormatter)
            }

            if(foodList.isNotEmpty()) {
                emptyTextView.visibility = View.INVISIBLE
                getRecipes(25)
            }
            else {
                emptyTextView.visibility = View.VISIBLE
            }

        }.addOnFailureListener {
            Log.e("firebase", "Error getting data", it)
        }
    }

    private fun setupRecyclerView() {
        mView.recipes_recyclerview.adapter = mmAdapter
        mView.recipes_recyclerview.layoutManager = LinearLayoutManager(activity)
        mView.recipes_recyclerview.hasFixedSize()
    }

    private fun redirectToInstructions() {
        mmAdapter.onItemClick = { currentRecipe ->
            val url = currentRecipe.sourceUrl

            var bundle = Bundle()
            bundle.putString("url", url)

            val intent = Intent(activity, InstructionsActivity::class.java)

            intent.putExtras(bundle)

            startActivity(intent)
        }
    }

}