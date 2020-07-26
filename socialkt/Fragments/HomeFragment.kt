package com.example.socialkt.Fragments


import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.socialkt.Adapters.PostAdapter
import com.example.socialkt.Models.Posts
import com.example.socialkt.story_pacakage.Story
import com.example.socialkt.Models.Users

import com.example.socialkt.R
import com.example.socialkt.story_pacakage.StoryAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.post_card.view.*

/**
 * A simple [Fragment] subclass.
 */
private const val TAG = "home"

class HomeFragment : Fragment() {
    private var homeRecyclerView: RecyclerView? = null
    private var firebaseUser: FirebaseUser? = null
    private var storyRecyclerView: RecyclerView? = null
    private var PostList: MutableList<Posts>? = null
    private var storyList: MutableList<Story>? = null
    private var FollowingList: MutableList<String>? = null
    private var PostAdapter: PostAdapter? = null
    private var StoryAdapter: StoryAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.fragment_home, container, false)
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        val activeBtn = view.findViewById<ImageView>(R.id.active_btn)
//
        homeRecyclerView = view.findViewById(R.id.home_recyclerView)
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.stackFromEnd = true
        linearLayoutManager.reverseLayout = true
        homeRecyclerView?.layoutManager = linearLayoutManager

        PostList = ArrayList()
        PostAdapter = context?.let { PostAdapter(it, PostList as ArrayList<Posts>) }
        homeRecyclerView?.adapter = PostAdapter

        checkFollowing()



        storyRecyclerView = view.findViewById(R.id.story_recyclerView)
        val storyLinearMananger =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        storyRecyclerView?.layoutManager = storyLinearMananger

        storyList = ArrayList()
        StoryAdapter = context?.let { StoryAdapter(it, storyList as ArrayList<Story>) }
        storyRecyclerView?.adapter = StoryAdapter

        retrieveStories()



//

//


        return view
    }

    private fun retrieveStories() {
        FirebaseDatabase.getInstance().reference.child("story")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {

                    (storyList as ArrayList<Story>).clear()
                    (storyList as ArrayList<Story>).add(
                        Story(
                            "", 0, 0, "", firebaseUser!!.uid
                        )
                    )
                    for (id in FollowingList!!) {
                        var story: Story? = null
                        val current_time = System.currentTimeMillis()
                        for (i in snapshot.child(id).children) {
                            story = i.getValue(Story::class.java)
                            if (current_time < story!!.getTimeend() && current_time > story.getTimestart()) {
                                storyList?.add(story)
                            }
                        }
                        StoryAdapter?.notifyDataSetChanged()
                    }
                }
            })
    }


    private fun checkFollowing() {
        FollowingList = ArrayList()
        val followingRef = FirebaseDatabase.getInstance().reference
            .child("follow")
            .child(firebaseUser!!.uid)
            .child("following")
        followingRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    (FollowingList as ArrayList<String>)?.clear()
                    for (i in snapshot.children) {
                        i.key?.let { (FollowingList as ArrayList<String>).add(it) }

                    }

                    retrieveUsers()


                }
            }
        })
    }

    private fun retrieveUsers() {
        val postRef = FirebaseDatabase.getInstance().reference
            .child("posts")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        PostList?.clear()
                        for (i in snapshot.children) {
                            val new_post = i.getValue(Posts::class.java)
                            for (id in (FollowingList as ArrayList<String>)) {
                                if (id == new_post!!.getPostPublisher()) {
                                    PostList?.add(new_post)
                                }
                                PostAdapter?.notifyDataSetChanged()
                            }
                        }
                    }
                }
            })
    }





}
