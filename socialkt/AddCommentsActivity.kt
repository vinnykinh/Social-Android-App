package com.example.socialkt

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.socialkt.Adapters.CommentsAdapter
import com.example.socialkt.Models.Comments
import com.example.socialkt.Models.Posts
import com.example.socialkt.Models.Users
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_add_comments.*
import kotlinx.android.synthetic.main.activity_add_posts.*
import kotlinx.android.synthetic.main.post_card.*

class AddCommentsActivity : AppCompatActivity() {
    private var firebaseUser: FirebaseUser? = null
    private var postID: String = ""
    private var postPublisher: String = ""
    private var commentRecyclerView: RecyclerView? = null
    private var commentList: MutableList<Comments>? = null
    private var CommentsAdapter: CommentsAdapter? = null

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_comments)
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        val intent = intent




        postID = intent.getStringExtra("postID")
        postPublisher = intent.getStringExtra("postPublisher")
        commentRecyclerView = findViewById(R.id.comment_recycler_view)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.stackFromEnd = true
        linearLayoutManager.reverseLayout = true
        commentRecyclerView?.layoutManager = linearLayoutManager



        commentList = ArrayList()
        CommentsAdapter = CommentsAdapter(this,commentList as ArrayList<Comments>)
        commentRecyclerView?.adapter =CommentsAdapter
        addCommentBtn.setOnClickListener {
            when {
                commentEditText.text.toString() == "" -> {
                    Toast.makeText(this, "please add a comment first", Toast.LENGTH_SHORT).show()
                }
                else -> {

                    val commentRef = FirebaseDatabase.getInstance().reference
                        .child("comments")

                    val commentID = commentRef.push().key.toString()
                    val commentsHashMap = HashMap<String, Any>()
                    commentsHashMap["commentText"] = commentEditText.text.toString()
                    commentsHashMap["commentID"] = commentID.toString()
                    commentsHashMap["commentPublisher"] = firebaseUser!!.uid
                    commentRef
                        .child(postID)
                        .child(commentID)
                        .setValue(commentsHashMap)
                    commentEditText!!.text.clear()

                }
            }
        }
        checkLikesStatus()
        likeBtnAddComment.setOnClickListener {
            when(likeBtnAddComment.tag){
                "liked"->{
                    FirebaseDatabase.getInstance().reference
                        .child("likes")
                        .child(postID)
                        .child(firebaseUser!!.uid)
                        .removeValue()
                }
                "not liked"->{
                    FirebaseDatabase.getInstance().reference
                        .child("likes")
                        .child(postID)
                        .child(firebaseUser!!.uid)
                        .setValue(true)

                }
            }
        }
        no_of_likes()
        no_of_comments()
        retrievePost()
        updateUserProfilePic()
        postPic()
    }

    private fun no_of_comments() {
        FirebaseDatabase
            .getInstance().reference
            .child("comments")
            .child(postID)
            .addValueEventListener(object :ValueEventListener{
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists())
                    {
                        no_of_commentAdd.text =snapshot.childrenCount.toString()
                    }
                }
            })
    }

    private fun checkLikesStatus() {
        FirebaseDatabase.getInstance().reference
            .child("likes")
            .child(postID)
            .addValueEventListener(object :ValueEventListener{
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.child(firebaseUser!!.uid).exists())
                    {
                        likeBtnAddComment.setImageResource(R.drawable.heart_clicked)
                        likeBtnAddComment.tag ="liked"
                    }
                    else{
                        likeBtnAddComment.setImageResource(R.drawable.heart_not_clicked)
                        likeBtnAddComment.tag ="not liked"
                    }
                }
            })
    }



    private fun no_of_likes() {
        val likesRef  =FirebaseDatabase.
                getInstance()
            .reference
            .child("likes")
            .child(postID)
        likesRef.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists())
                {
                    no_of_likes_AddComment.text = snapshot.childrenCount.toString()
                }
            }
        })
    }

    private fun retrievePost() {
        val commentRef = FirebaseDatabase.getInstance().reference
            .child("comments")
            .child(postID)
            .addValueEventListener(object :ValueEventListener{
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists())
                    {
                        commentList?.clear()
                        for (i in snapshot.children)
                        {
                            val post = i.getValue(Comments::class.java)
                            if (post!=null)

                            {
                                commentList!!.add(post)
                            }
                            CommentsAdapter?.notifyDataSetChanged()

                        }
                    }
                }
            })
    }

    private fun updateUserProfilePic() {
        val ref = FirebaseDatabase.getInstance().reference
            .child("Users")
            .child(firebaseUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val user = snapshot.getValue(Users::class.java)
                        if (user != null) {
                            Picasso.get()
                                .load(user.getProfilePicUrl())
                                .placeholder(R.drawable.profile)
                                .into(commentProfileImage)

                        }
                    }
                }
            })
    }

    private fun postPic() {
        FirebaseDatabase
            .getInstance().reference
            .child("posts")
            .child(postID)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val post = snapshot.getValue(Posts::class.java)
                        if (post != null) {
                            Picasso.get()
                                .load(post.getPostImageUrl())
                                .fit()
                                .into(postImageAddComment)
                        }

                    }
                }
            })
    }
}
