package com.example.mysns_project.DTO

data class PostDTO(
    var explain : String? = null,
    var imageUrl : String? = null,
    var uid : String? = null,
    var userId : String? = null,
    var timestamp : Long? = null,
    var favoriteCount : Int = 0,
    var commentCount : Int = 0,
    var favorites : MutableMap<String,Boolean> = HashMap() ) {
    data class Comment(
        var uid : String? =null,
        var userId : String? =null,
        var comment : String? =null,
        var timestamp: Long? = null
    )
}
