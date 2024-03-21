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