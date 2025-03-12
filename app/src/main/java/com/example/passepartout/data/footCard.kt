package com.example.passepartout.data

data class footCard private constructor(
    val tripId: String,
    val tripName: String,
    val description: String,
    val location: String,
    val image: String,
    val avatar: String,
    val latitude: Double,
    val longitude: Double,
    val ownerId: String,
    val likedBy: MutableMap<String, Boolean> = mutableMapOf()
) {
    class builder {
        private var tripId: String = ""
        private var tripName: String = ""
        private var description: String = ""
        private var location: String = ""
        private var image: String = ""
        private var avatar: String = ""
        private var latitude: Double = 0.0
        private var longitude: Double = 0.0
        private var ownerId: String = ""
        private var likedBy: MutableMap<String, Boolean> = mutableMapOf()

        fun tripId(tripId: String) = apply { this.tripId = tripId }
        fun tripName(tripName: String) = apply { this.tripName = tripName }
        fun description(description: String) = apply { this.description = description }
        fun location(location: String) = apply { this.location = location }
        fun image(image: String) = apply { this.image = image }
        fun avatar(avatar: String) = apply { this.avatar = avatar }
        fun latitude(latitude: Double) = apply { this.latitude = latitude }
        fun longitude(longitude: Double) = apply { this.longitude = longitude }
        fun ownerId(ownerId: String) = apply { this.ownerId = ownerId }
        fun likedBy(likedBy: MutableMap<String, Boolean>) = apply { this.likedBy = likedBy }

        fun build() = footCard(
            tripId,
            tripName,
            description,
            location,
            image,
            avatar,
            latitude,
            longitude,
            ownerId,
            likedBy
        )
    }
}
