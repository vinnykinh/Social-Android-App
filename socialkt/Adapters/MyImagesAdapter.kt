package com.example.socialkt.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.example.socialkt.Models.Posts
import com.example.socialkt.R
import com.example.socialkt.SavedPostsActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class MyImagesAdapter(
    private var mContext: Context,
    private var mList: List<Posts>
) : RecyclerView.Adapter<MyImagesAdapter.ImagesViewHolder>() {
    class ImagesViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        val postImage: ImageView = itemView.findViewById(R.id.myPostsImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagesViewHolder {
        val view =LayoutInflater
            .from(mContext)
            .inflate(R.layout.image_card,parent,false)
        return ImagesViewHolder(view)
    }

    override fun getItemCount(): Int =mList.size

    override fun onBindViewHolder(holder: ImagesViewHolder, position: Int) {
        val my_current_post =  mList[position]
        Picasso.get().load(my_current_post.getPostImageUrl())
            .fit()
            .into(holder.postImage)
        holder.itemView.setOnClickListener {
            val intent =Intent(mContext,SavedPostsActivity::class.java)
            intent.putExtra("postID",my_current_post.getPostID())
            mContext.startActivity(intent)

        }
    }
}