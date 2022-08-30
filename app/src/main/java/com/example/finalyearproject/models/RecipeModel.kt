package com.example.finalyearproject.models


import com.google.gson.annotations.SerializedName

data class RecipeModel (
    @SerializedName("aggregateLikes")
    var aggregateLikes: Int,
    @SerializedName("id")
    var id: Int,
    @SerializedName("title")
    var title : String,
    @SerializedName("sourceUrl")
    var sourceUrl: String,
    @SerializedName("image")
    var image: String,
    @SerializedName("summary")
    val summary: String,
    @SerializedName("readyInMinutes")
    val readyInMinutes : Int,
    @SerializedName("vegan")
    val vegan: Boolean,
    @SerializedName("vegetarian")
    val vegetarian: Boolean
/*
    var cheap: Boolean,

    val dairyFree: Boolean,

    //val extendedIngredients: Boolean,//@RawValue List<ExtendedIngredient>,

    val glutenFree: Boolean,


    val readyInMinutes: Int,

    val sourceName: String?,

    val sourceUrl: String,

    val summary: String,

    val title: String,

    val vegan: Boolean,

    val vegetarian: Boolean,

    val veryHealthy: Boolean,*/
)


