package com.nfar.allergydetective.data.db.model

/**
 * What the user logged: a meal of the day, a snack or a drink (feedback 20.11.).
 * Stored with the meal, so changing the time does not change the label.
 */
enum class MealKind { BREAKFAST, LUNCH, DINNER, SNACK, DRINK }
