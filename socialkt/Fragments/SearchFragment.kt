package com.example.socialkt.Fragments


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.socialkt.Adapters.MyImagesAdapter
import com.example.socialkt.Adapters.UserAdapter
import com.example.socialkt.Models.Posts
import com.example.socialkt.Models.Users

import com.example.socialkt.R
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_search.view.*

/**
 * A simple [Fragment] subclass.
 */
class SearchFragment : Fragment() {
    private var firebaseUser: FirebaseUser? = null
    private var UserAdapter: UserAdapter? = null
    private var userList: MutableList<Users>? = null
    private var searchRecyclerView:RecyclerView? =null
    private var MyImagesAdapter:MyImagesAdapter?=null
    private var postList:MutableList<Posts>? =null
    private var imagesSearchRecycler:RecyclerView?=null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_search, container, false)


        searchRecyclerView = view.findViewById(R.id.searchRecyclerView)
        searchRecyclerView?.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context)
        searchRecyclerView?.layoutManager =layoutManager

        userList =ArrayList()
        UserAdapter = context?.let { UserAdapter(it ,userList as ArrayList<Users>, true) }
        searchRecyclerView?.adapter =UserAdapter

        imagesSearchRecycler = view.findViewById(R.id.imagesSearchRecyclerView)
        val gridLayoutManager =GridLayoutManager(context,2,GridLayoutManager.VERTICAL,false)
        imagesSearchRecycler?.layoutManager = gridLayoutManager

        postList =ArrayList()
        MyImagesAdapter = context?.let { MyImagesAdapter(it, postList as ArrayList<Posts>) }
        imagesSearchRecycler?.adapter =MyImagesAdapter

        retrieveAllPostImages()

        view.search_barEditText?.addTextChangedListener(object :TextWatcher{


            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                when {
                    view?.search_barEditText?.text.toString() =="" -> {
                        searchRecyclerView?.visibility =View.GONE
                        imagesSearchRecycler?.visibility =View.VISIBLE
                    }
                    else -> {
                        searchRecyclerView?.visibility =View.VISIBLE
                        imagesSearchRecycler?.visibility =View.GONE
                        retrieve_users()
                        search_user(s.toString().toLowerCase())

                    }
                }

            }
            override fun afterTextChanged(s: Editable?) {

            }

        })

        return  view
    }

    private fun retrieveAllPostImages() {

        val ref = FirebaseDatabase.getInstance().reference
        ref.child("posts")
            .addValueEventListener(object :ValueEventListener{
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists())
                    {
                        postList?.clear()
                        for (i in snapshot.children)
                        {
                            val post = i.getValue(Posts::class.java)
                            if (post!=null)
                            {
                                post.let { postList?.add(it) }
                            }
                            MyImagesAdapter?.notifyDataSetChanged()
                            postList?.reverse()
                        }
                    }
                }
            })
    }

    private fun retrieve_users() {
        val  user_reference = FirebaseDatabase.getInstance().getReference("Users")
        user_reference.addValueEventListener(object :ValueEventListener
        {
            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(context,p0.message, Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (view?.search_barEditText?.text.toString() != "")
                {
                    userList?.clear()
                    for (i in p0.children)
                    {
                        val user = i.getValue(Users::class.java)
                        if (user!=null)
                        {
                            userList?.add(user)
                        }
                        UserAdapter?.notifyDataSetChanged()
                    }

                }
            }

        })
    }

    private fun search_user(input: String) {
        val search_ref = FirebaseDatabase.getInstance().getReference("Users")
        search_ref.orderByChild("username")
            .startAt(input)
            .endAt(input +"\uf8ff")
            .addValueEventListener(object :ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (view?.search_barEditText?.text.toString() != "")
                    {
                        userList?.clear()
                        for (i in p0.children)
                        {
                            val user = i.getValue(Users::class.java)
                            if (user!=null)
                            {
                                userList?.add(user)
                            }
                            UserAdapter?.notifyDataSetChanged()
                        }

                    }
                }
            })

    }




}
