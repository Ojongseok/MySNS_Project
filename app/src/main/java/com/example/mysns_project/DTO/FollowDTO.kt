package com.example.mysns_project.DTO

data class FollowDTO (
    var email : String? = null,
    var imageUrl : String? =null,

    var followerCount : Int = 0,
    var followers : MutableMap<String,Boolean> = HashMap(),

    var followingCount : Int = 0,
    var followings : MutableMap<String,Boolean> = HashMap()
)