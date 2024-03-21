package com.tutorial.activityrecognition

object Constants {
    const val REQUEST_CODE = 100
}

val ActivityCodesToString = mapOf(
    0 to "Still",
    1 to "Walk",
    2 to "Run",
    3 to "Drive"
)

val ActivityCodesToDescription = mapOf(
    0 to "The device is still (not moving).",
    1 to "The device is on a user who is walking.",
    2 to "The device is on a user who is running.",
    3 to "The device is in a vehicle, such as a car."
)

val ActivityCodesToImage = mapOf(
    0 to R.drawable.still,
    1 to R.drawable.walk,
    2 to R.drawable.run,
    3 to R.drawable.driving
)

val ActivityCodesToBackground = mapOf(
    0 to R.drawable.blue,
    1 to R.drawable.red,
    2 to R.drawable.green,
    3 to R.drawable.black
)

val activityQuotes = mapOf(
    2 to "Run when you can, walk if you have to, crawl if you must; just never give up.",
    1 to "Walking is man's best medicine.",
    3 to "Life is a journey. Enjoy the ride!",
    0 to "Sometimes the most productive thing you can do is relax."
)

val toastMessagesfromPrevious = mapOf(
    0 to "You rested for ",
    1 to "You have walked for ",
    2 to "Good job ! you ran for ",
    3 to "You drove for "
)