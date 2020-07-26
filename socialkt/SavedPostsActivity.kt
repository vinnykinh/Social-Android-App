package com.example.socialkt

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.socialkt.Adapters.PostAdapter
import com.example.socialkt.Adapters.PostAdapter2
import com.example.socialkt.Models.Posts
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SavedPostsActivity : AppCompatActivity() {
    private var postID:String = ""

    private var postList:MutableList<Posts>? =null
    private var postAdapter:PostAdapter2?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_posts)


        this.postID = intent.getStringExtra("postID")
        val toolBar = findViewById<Toolbar>(R.id.savedPostToolBar)
        setSupportActionBar(toolBar)
        supportActionBar!!.title = "Posts"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolBar.setNavigationOnClickListener {
            finish()
        }

        var savedPostRecycler:RecyclerView?= null
        savedPostRecycler = findViewById(R.id.savedImages_recyclerView)
        val linearLayoutManager =LinearLayoutManager(this)
        savedPostRecycler.layoutManager= linearLayoutManager

        postList =ArrayList()
        postAdapter = PostAdapter2(this,postList as ArrayList<Posts>)
        savedPostRecycler.adapter =postAdapter

        retrieveSavedPost()

    }

    private fun retrieveSavedPost() {
        FirebaseDatabase.getInstance().reference
            .child("posts")
            .child(postID)
            .addValueEventListener(object :ValueEventListener{
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val post = snapshot.getValue(Posts::class.java)
                    if (post!=null){
                        postList?.add(post)
                    }
                    postAdapter?.notifyDataSetChanged()
                }
            })
    }
}
