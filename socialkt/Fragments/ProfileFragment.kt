package com.example.socialkt.Fragments


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.socialkt.Adapters.MyImagesAdapter
import com.example.socialkt.Adapters.PostAdapter
import com.example.socialkt.Models.Posts
import com.example.socialkt.Models.Users

import com.example.socialkt.R
import com.example.socialkt.SettingsActivity
import com.example.socialkt.ShowUsersActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass.
 */
private const val TAG = "photos"


class ProfileFragment : Fragment() {
    private var firebaseUser: FirebaseUser? = null
    private var profileID: String = ""
    private var photosList: MutableList<String>? = null
    private var postAdapter:PostAdapter? =null
    private var postIdsSavedPosts:MutableList<String>? =null
    private var imagesRecyclerView: RecyclerView? = null
    private var savedPosts_recyclerView:RecyclerView?=null
    private var postList: MutableList<Posts>? = null
    private var savedPostList:MutableList<Posts>? =null
    private var MyImagesAdapter: MyImagesAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        imagesRecyclerView = view.findViewById(R.id.profile_recyclerView)
        val gridLayoutManager = GridLayoutManager(context, 2,GridLayoutManager.VERTICAL,false)
        imagesRecyclerView?.layoutManager = gridLayoutManager

        postList = ArrayList()
        MyImagesAdapter = context?.let { MyImagesAdapter(it, postList as ArrayList<Posts>) }
        imagesRecyclerView?.adapter = MyImagesAdapter

        retrieveMyPosts()

        savedPosts_recyclerView =view.findViewById(R.id.savedPosts_recyclerView)
        val savedPostLinearLayoutManager =GridLayoutManager(context,2,GridLayoutManager.VERTICAL,false)
        savedPosts_recyclerView?.layoutManager =savedPostLinearLayoutManager

        savedPostList =ArrayList()
        val savedPostAdapter = context?.let { MyImagesAdapter(it,savedPostList as ArrayList<Posts>) }
        savedPosts_recyclerView?.adapter = savedPostAdapter
        retrieveSavedPostsIDS()

        view.saved_posts?.setOnClickListener {
            imagesRecyclerView?.visibility = View.GONE
            savedPosts_recyclerView?.visibility = View.VISIBLE
        }
        view.my_images?.setOnClickListener {
            imagesRecyclerView?.visibility =View.VISIBLE
            savedPosts_recyclerView?.visibility =View.GONE

        }



        val pref: SharedPreferences = context?.getSharedPreferences("Prefs", Context.MODE_PRIVATE)!!
        if (pref != null) {
            this.profileID = pref.getString("profileID", "none").toString()
        }
        when (profileID) {
            firebaseUser!!.uid -> {
                view?.edit_profile_btn?.text = "Edit Profile"

            }
            else -> {
                view?.below?.visibility = View.GONE
                val followRef = firebaseUser?.uid.let { it1 ->
                    FirebaseDatabase.getInstance().reference
                        .child("follow")
                        .child(it1.toString())
                        .child("following")
                        .addValueEventListener(object : ValueEventListener {
                            override fun onCancelled(error: DatabaseError) {

                            }

                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.child(profileID).exists()) {
                                    view?.edit_profile_btn?.text = "Following"
                                } else {
                                    view?.edit_profile_btn?.text = "Follow"
                                }
                            }
                        })
                }


            }
        }
        view.edit_profile_btn.setOnClickListener {
            when (view.edit_profile_btn.text.toString()) {
                "Edit Profile" -> {
                    val intent = Intent(context, SettingsActivity::class.java)
                    startActivity(intent)
                }
                "Follow" -> {
                    val ref = firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("follow")
                            .child(it1.toString())
                            .child("following")
                            .child(profileID)
                            .setValue(true)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    FirebaseDatabase.getInstance().reference
                                        .child("follow")
                                        .child(profileID)
                                        .child("followers")
                                        .child(it1.toString())
                                        .setValue(true)
                                }
                            }
                    }
                }
                "Following" -> {
                    val ref = firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("follow")
                            .child(it1.toString())
                            .child("following")
                            .child(profileID)
                            .removeValue()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    FirebaseDatabase.getInstance().reference
                                        .child("follow")
                                        .child(profileID)
                                        .child("followers")
                                        .child(it1.toString())
                                        .removeValue()
                                }
                            }
                    }
                }
            }
        }



     view.total_no_of_followings.setOnClickListener {
         val intent = Intent(context, ShowUsersActivity::class.java)
         intent.putExtra("id",profileID)
         intent.putExtra("title", "Followings")
         context?.startActivity(intent)
     }
        view.total_no_of_followers.setOnClickListener {
            val intent = Intent(context, ShowUsersActivity::class.java)
            intent.putExtra("id",profileID)
            intent.putExtra("title", "Followers")
            context?.startActivity(intent)
        }



        updatePostsNumber()
        getFollowers()
        getFollowings()
        updateUser()

        return view
    }

    private fun retrieveSavedPostsIDS() {
        postIdsSavedPosts =ArrayList()
        FirebaseDatabase.getInstance().reference
            .child("saves")
            .child(firebaseUser!!.uid)
            .addValueEventListener(object :ValueEventListener{
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        (postIdsSavedPosts as ArrayList<String>).clear()
                        for (i in snapshot.children)
                        {
                            i.key?.let { (postIdsSavedPosts as ArrayList<String>).add(it) }
                        }
                        retrieveSavedPosts()
                    }
                }
            })
    }

    private fun retrieveSavedPosts() {
        FirebaseDatabase.getInstance().reference
            .child("posts")
            .addValueEventListener(object :ValueEventListener{
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists())
                    {
                        (savedPostList as ArrayList<String>).clear()
                        for (id in postIdsSavedPosts!!)
                        {
                            for (i in snapshot.children)
                            {
                                val post = i.getValue(Posts::class.java)
                                if (post!=null)
                                {
                                    if(id == post.getPostID()){
                                    savedPostList?.add(post)}
                                }
                            }
                            MyImagesAdapter?.notifyDataSetChanged()
                            savedPostList?.reverse()
                        }
                    }
                }
            })
    }

    private fun retrieveMyPosts() {
        val postRef = FirebaseDatabase.getInstance().reference
            .child("posts")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (i in snapshot.children) {
                            val post = i.getValue(Posts::class.java)
                            if (profileID == post!!.getPostPublisher()) {
                                postList?.add(post)
                            }
                            postList?.reverse()
                            MyImagesAdapter?.notifyDataSetChanged()
                        }
                    }
                }
            })
    }

    private fun getFollowings() {
        val followingRef = FirebaseDatabase.getInstance().reference
            .child("follow")
            .child(profileID)
            .child("following")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val followingCount = snapshot.childrenCount.toString()
                        view?.total_no_of_followings?.text = followingCount
                    }
                }
            })
    }

    private fun getFollowers() {
        val followersRef = FirebaseDatabase.getInstance().reference
            .child("follow")
            .child(profileID)
            .child("followers")
        followersRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val followersCount = snapshot.childrenCount.toString()
                    view?.total_no_of_followers?.text = followersCount
                }
            }
        })
    }

    private fun updateUser() {
        val userRef = FirebaseDatabase.getInstance().reference
            .child("Users")
            .child(profileID)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(Users::class.java)
                    if (user != null) {

                        view?.fullname_profile_frag?.text = user.getFullname()
                        view?.bio_information_profile?.text = user.getBio()
                        view?.location_profile_frag?.text = user.getUserLocation()
                        view?.username_profile?.text = user.getUsername()
                        //Picasso.get().load(user.getProfilePicUrl()).placeholder(R.drawable.profile).into(view?.profilePic_profileFrag)

                    }
                }
            }
        })
    }

    private fun updatePostsNumber() {
        photosList = ArrayList()
        FirebaseDatabase.getInstance().reference
            .child("posts")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        photosList?.clear()
                        var counter = 0
                        for (i in snapshot.children) {
                            val posts = i.getValue(Posts::class.java)
                            if (profileID == posts!!.getPostPublisher()) {
                                posts.getPostPublisher()
                                    .let { (photosList as ArrayList<String>).add(it) }

                            }

                        }
                        for (id in photosList as ArrayList<String>) {
                            Log.i(TAG, id)
                            counter++
                        }
                        view?.total_no_of_photos?.text = counter.toString()
                    }
                }
            })
    }


    override fun onDestroy() {
        super.onDestroy()
        val pref = context?.getSharedPreferences("Prefs", Context.MODE_PRIVATE)!!.edit()
        pref.putString("profileID", firebaseUser!!.uid)
        pref.apply()
    }

    override fun onStop() {
        super.onStop()
        val pref = context?.getSharedPreferences("Prefs", Context.MODE_PRIVATE)!!.edit()
        pref.putString("profileID", firebaseUser!!.uid)
        pref.apply()
    }

    override fun onStart() {
        super.onStart()
        val pref = context?.getSharedPreferences("Prefs", Context.MODE_PRIVATE)!!.edit()
        pref.putString("profileID", firebaseUser!!.uid)
        pref.apply()
    }

    override fun onPause() {
        super.onPause()
        val pref = context?.getSharedPreferences("Prefs", Context.MODE_PRIVATE)!!.edit()
        pref.putString("profileID", firebaseUser!!.uid)
        pref.apply()
    }


}

