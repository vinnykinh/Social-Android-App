package com.example.socialkt.Models

class Comments {
    private var commentText: String = ""
    private var commentID: String = ""
    private var commentPublisher: String = ""



    constructor()

    constructor(
        commentText: String,
        commentID: String,
        commentPublisher: String


    ) {
        this.commentID = commentID
        this.commentText = commentText
        this.commentPublisher = commentPublisher


    }
    fun getCommentID(): String {
        return commentID
    }

    fun setCommentID(commentID: String) {
        this.commentID = commentID
    }


    fun getCommentText(): String {
        return commentText
    }

    fun setCommentText(commentText: String) {
        this.commentText = commentText
    }

    fun getCommentPublisher():String{
        return commentPublisher
    }
    fun setCommentPublisher(commentPublisher: String){
        this.commentPublisher =commentPublisher
    }



}