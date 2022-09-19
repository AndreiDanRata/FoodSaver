package com.example.finalyearproject.util

class Constants {

    companion object {

        //api example: https://api.upcdatabase.org/product/0111222333446?apikey=4368CFAB9E2F2B72DBBDA6EE20DD7877
        const val BARCODE_API_KEY ="4368CFAB9E2F2B72DBBDA6EE20DD7877"
        const val BARCODE_BASE_URL = "https://api.upcdatabase.org/product/"



        const val RECIPES_API_KEY ="def03f97eb664436ac3a62c793e582a7"

        //api example:https://api.spoonacular.com/recipes/findByIngredients?apiKey=def03f97eb664436ac3a62c793e582a7&ingredients=apples,+flour,+sugar&number=5&ranking=1&ignorePantry=true
        const val SEARCH_BY_INGREDIENTS_BASE_URL = "https://api.spoonacular.com/recipes/findByIngredients?apiKey=def03f97eb664436ac3a62c793e582a7&ingredients="


        //  https://api.spoonacular.com/recipes/673463/information?apiKey=def03f97eb664436ac3a62c793e582a7
        const val SEARCH_BY_ID_BASE_URL = "https://api.spoonacular.com/recipes/"

    }
}

