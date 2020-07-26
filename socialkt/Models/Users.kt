package com.example.socialkt.Models

class Users {
    private var fullname: String = ""
    private var username: String = ""
    private var email: String = ""
    private var bio: String = ""
    private var userLocation: String = ""
    private var profilePicUrl: String = ""
    private var userUID: String = ""

    constructor()

    constructor(
        fullname: String,
        profilePicUrl: String,
        userLocation: String,
        bio: String,
        userUID: String,
        email: String,
        username: String
    ) {
        this.fullname = fullname
        this.username = username
        this.userUID = userUID
        this.profilePicUrl = profilePicUrl
        this.email = email
        this.bio = bio
        this.userLocation = userLocation
    }

    fun getUserUID(): String {
        return userUID
    }

    fun setUserUID(userUID: String) {
        this.userUID = userUID
    }


    fun getUsername(): String {
        return username
    }

    fun setUsername(username: String) {
        this.username = username
    }

    fun getFullname():String{
        return fullname
    }
    fun setFullname(fullname: String){
        this.fullname =fullname
    }


    fun getEmail(): String {
        return email
    }

    fun setEmail(email: String) {
        this.email = email
    }


    fun getProfilePicUrl(): String {
        return profilePicUrl
    }

    fun setProfilePicUrl(profilePicUrl: String) {
        this.profilePicUrl = profilePicUrl
    }

    fun getBio(): String {
        return bio
    }

    fun setBio(bio: String) {
        this.bio = bio
    }


    fun getUserLocation(): String {
        return userLocation
    }

    fun setUserLocation(userLocation: String) {
        this.userLocation = userLocation
    }


}