package com.example.socialkt.story_pacakage

class Story {
    private var imageUrl: String = ""
    private var timestart: Long = 0
    private var timeend: Long =0
    private var storyID: String = ""
    private var userUID: String = ""


    constructor()


    constructor(
        imageUrl: String,
        timestart: Long,
        timeend: Long,
        storyID: String,
        userUID: String
    ) {
        this.imageUrl = imageUrl
        this.timestart = timestart
        this.timeend = timeend
        this.storyID = storyID
        this.userUID = userUID
    }

    fun getImageUrl(): String {
        return imageUrl
    }

    fun setImageUrl(imageUrl: String) {
        this.imageUrl = imageUrl
    }

    fun getTimestart(): Long {
        return timestart
    }

    fun setTimestart(timestart: Long) {
        this.timestart = timestart
    }

    fun getTimeend(): Long {
        return timeend
    }

    fun setTimeend(timeend: Long) {
        this.timeend = timeend
    }

    fun getStoryID(): String {
        return storyID
    }

    fun setStoryID(storyID: String) {
        this.storyID = storyID
    }

    fun getUserUID(): String {
        return userUID
    }

    fun setUserUID(userUID: String) {
        this.userUID = userUID
    }
}