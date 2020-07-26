package com.example.socialkt.Models

class Posts {

        private var postID: String = ""
        private var postImageUrl: String = ""
        private var postPublisher: String = ""
        private var postDescription: String = ""


        constructor()

        constructor(
            postID: String,
            postImageUrl: String,
            postPublisher: String,
            postDescription: String

        ) {
            this.postID = postID
            this.postImageUrl = postImageUrl
            this.postPublisher = postPublisher
            this.postDescription = postDescription

        }

        fun getPostID(): String {
            return postID
        }

        fun setPostID(postID: String) {
            this.postID = postID
        }


        fun getPostImageUrl(): String {
            return postImageUrl
        }

        fun setPostImageUrl(postImageUrl: String) {
            this.postImageUrl = postImageUrl
        }

        fun getPostPublisher():String{
            return postPublisher
        }
        fun setPostPublisher(postPublisher: String){
            this.postPublisher =postPublisher
        }


        fun getPostDescription(): String {
            return postDescription
        }

        fun setPostDescription(postDescription: String) {
            this.postDescription = postDescription
        }






}