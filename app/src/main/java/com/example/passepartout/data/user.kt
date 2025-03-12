package com.example.passepartout.data

data class user(
    var name: String = "",
    var email: String = "",
    var phone: String = "",
    var photoUrl: String = "",
    var uid: String = "",
    var footPrints: HashMap<String, footPrint> = HashMap(),
    var friends: HashMap<String, String> = HashMap()
)
