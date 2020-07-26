package com.example.socialkt

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.RecoverySystem
import android.renderscript.Sampler
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.socialkt.Adapters.UserAdapter
import com.example.socialkt.Models.Users
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ShowUsersActivity : AppCompatActivity() {
    private  var id: String = ""
    private var title: String = ""
    private var userAdapter: UserAdapter? = null
    private var userList: MutableList<Users>? = null
    private var idsList: MutableList<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_users)

        val intent = intent
        id = intent.getStringExtra("id")
        title = intent.getStringExtra("title")


        val toolBar = findViewById<Toolbar>(R.id.toolBar)
        setSupportActionBar(toolBar)
        supportActionBar!!.title = title
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolBar.setNavigationOnClickListener {
            finish()
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_show_users)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)

         userList =ArrayList()
        userAdapter = UserAdapter(this, userList as ArrayList<Users>,true)
        recyclerView.adapter = userAdapter

        idsList = ArrayList()
        when (title) {
            "likes" -> getLikes()
            "Followings" -> getFollowing()
            "Followers" -> getFollowers()
            "views" -> getViews()
        }


    }

    private fun getViews() {

    }

    private fun getFollowers() {

        val following = FirebaseDatabase.getInstance().reference
            .child("follow")
            .child(id)
            .child("followers")
            .addValueEventListener(object :ValueEventListener{
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists())
                    {
                        (idsList as ArrayList<String>)?.clear()
                        for (i in snapshot.children)
                        {
                            (idsList as ArrayList<String>).add(i.key.toString())
                        }
                        showUsers()
                    }
                }
            })
    }

    private fun getFollowing() {
        val following = FirebaseDatabase.getInstance().reference
            .child("follow")
            .child(id)
            .child("following")
            .addValueEventListener(object :ValueEventListener{
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists())
                    {
                        (idsList as ArrayList<String>)?.clear()
                        for (i in snapshot.children)
                        {
                            (idsList as ArrayList<String>).add(i.key.toString())
                        }
                        showUsers()
                    }
                }
            })
    }

    private fun getLikes() {
        val likesRef = FirebaseDatabase.getInstance().reference
            .child("likes")
            .child(id!!)
        likesRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    (idsList as ArrayList<String>).clear()
                    for (i in snapshot.children) {
                        (idsList as ArrayList<String>).add(i.key.toString())
                    }
                    showUsers()


                }
            }
        })
    }

    private fun showUsers() {
        FirebaseDatabase.getInstance().reference
            .child("Users")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    (userList as ArrayList<Users>)?.clear()
                    for (i in snapshot.children) {
                        val user = i.getValue(Users::class.java)
                        for (id in idsList!!) {
                            if (id == user!!.getUserUID()) {
                                (userList as ArrayList<Users>).add(user)
                            }

                        }
                    }
                    userAdapter?.notifyDataSetChanged()

                }
            })
    }
}
