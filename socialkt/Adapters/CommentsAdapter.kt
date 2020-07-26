package com.example.socialkt.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.example.socialkt.Models.Comments
import com.example.socialkt.Models.Users
import com.example.socialkt.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class CommentsAdapter (
    private var mContext:Context,
    private var mList:List<Comments>
):RecyclerView.Adapter<CommentsAdapter.ViewHolder>() {
    class ViewHolder(@NonNull itemView: View):RecyclerView.ViewHolder(itemView){
        val commentText:TextView =itemView.findViewById(R.id.commentTextCommentsCard)
        val username:TextView =itemView.findViewById(R.id.usernameCommentCard)
        val commentProfilePic:CircleImageView =itemView.findViewById(R.id.profileCommentCard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater
            .from(mContext)
            .inflate(R.layout.comment_card,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
       return mList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentComment = mList[position]
        updateCommentPublisherInfo(currentComment.getCommentPublisher(),holder.username,holder.commentProfilePic)
        holder.commentText.text = currentComment.getCommentText()
    }

    private fun updateCommentPublisherInfo(commentPublisher: String, username: TextView, commentProfilePic: CircleImageView) {
         val userRef = FirebaseDatabase.getInstance().reference
             .child("Users")
             .child(commentPublisher)
        userRef.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists())
                {
                    val user = snapshot.getValue(Users::class.java)
                    if (user!=null){
                        Picasso.get()
                            .load(user.getProfilePicUrl())
                            .into(commentProfilePic)
                        username.text = user.getFullname()
                    }
                }
            }
        })
    }


}