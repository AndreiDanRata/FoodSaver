package com.example.finalyearproject.models


import com.google.gson.annotations.SerializedName
class RecipesListModel (
    @SerializedName("recipesList")
    var recipesList: List<RecipeModel>,
)