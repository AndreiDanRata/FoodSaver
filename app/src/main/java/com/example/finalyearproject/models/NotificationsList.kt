package com.example.finalyearproject.models

import javax.inject.Singleton

class NotificationsList {

        object Singleton {
                var notificationsList: MutableList<FoodItemModel> = mutableListOf()
        }

        fun addItemsToNotifications(item: FoodItemModel) {
                Singleton.notificationsList.add(item)
        }

        fun getItemsInNotifications(): MutableList<FoodItemModel> {
                return Singleton.notificationsList
        }
}