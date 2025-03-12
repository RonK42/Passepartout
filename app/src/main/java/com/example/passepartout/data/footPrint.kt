package com.example.passepartout.data

data class footPrint(
    var tripID: String = "",
    var description: String = "",
    var location: String = "",
    var photoUrl: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var likesCount: Int = 0,
    var likedBy: HashMap<String, Boolean> = HashMap()
)
