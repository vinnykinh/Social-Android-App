package com.example.socialkt.Adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.text.Layout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.NonNull
import androidx.appcompat.view.menu.MenuView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.socialkt.AddCommentsActivity
import com.example.socialkt.AddPostsActivity
import com.example.socialkt.Fragments.ProfileFragment
import com.example.socialkt.Models.Posts
import com.example.socialkt.Models.Users
import com.example.socialkt.R
import com.example.socialkt.ShowUsersActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

private const val TAG = "post"

class PostAdapter(
    private var mContext: Context,
    private var mList: List<Posts>
) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {
    private var firebaseUser: FirebaseUser? = null

    class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        val postImage: ImageView = itemView.findViewById(R.id.post_image_postCard)
        val postProfileImage: CircleImageView = itemView.findViewById(R.id.profile_image_postCard)
        val likeButton: ImageView = itemView.findViewById(R.id.like_button_postCard)
        val no_of_likes: TextView = itemView.findViewById(R.id.no_of_likes_postCard)
        val postDescription: TextView = itemView.findViewById(R.id.postDescription)
        val commentsInfo: TextView = itemView.findViewById(R.id.commentsPostCard)
        val name: TextView = itemView.findViewById(R.id.postUsername)
        val comments_button: ImageView = itemView.findViewById(R.id.comment_button_postCard)
        val no_of_comments: TextView = itemView.findViewById(R.id.no_of_comments_postCard)
        val bio: TextView = itemView.findViewById(R.id.bio_postCard)
        val username: TextView = itemView.findViewById(R.id.username_postCard)
        val save_btn: ImageView = itemView.findViewById(R.id.save_btn)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext)
            .inflate(R.layout.post_card, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val current_post = mList[position]
        Picasso.get()
            .load(current_post.getPostImageUrl())
            .fit()
            .into(holder.postImage)
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        updateUserInfo(
            holder.username,
            holder.bio,
            holder.postProfileImage,
            current_post.getPostPublisher(),
            holder.name
        )
        holder.postImage.setOnClickListener {

        }
        holder.username.setOnClickListener {
            val pref: SharedPreferences.Editor =
                mContext?.getSharedPreferences("Prefs", Context.MODE_PRIVATE)!!.edit()
            pref.putString("profileID", current_post.getPostPublisher())
            pref.apply()

            (mContext as FragmentActivity)
                .supportFragmentManager
                .beginTransaction().replace(R.id.fragmentContainer, ProfileFragment()).commit()
        }

        checkLikeStatus(holder.likeButton, current_post.getPostID())
        holder.no_of_likes.setOnClickListener {
            val intent = Intent(mContext, ShowUsersActivity::class.java)
            intent.putExtra("id", current_post.getPostID())
            intent.putExtra("title", "likes")
            mContext.startActivity(intent)
        }

        holder.postDescription.text = current_post.getPostDescription()
        holder.likeButton.setOnClickListener {
            if (holder.likeButton.tag == "liked") {
                val likeRef =
                    FirebaseDatabase.getInstance().reference
                        .child("likes")
                        .child(current_post.getPostID())
                        .child(firebaseUser!!.uid)
                        .removeValue()


            } else if (holder.likeButton.tag == "not Liked") {
                Log.i(TAG, "clicked")

                val likeRef =
                    FirebaseDatabase.getInstance().reference
                        .child("likes")
                        .child(current_post.getPostID())
                        .child(firebaseUser!!.uid)
                        .setValue(true)

            }

        }
        no_of_likes(current_post.getPostID(), holder.no_of_likes)
        no_of_comments(current_post.getPostID(), holder.no_of_comments, holder.commentsInfo)
        holder.commentsInfo.setOnClickListener {
            val intent = Intent(mContext, AddCommentsActivity::class.java)
            intent.putExtra("postID", current_post.getPostID())
            intent.putExtra("postPublisher", current_post.getPostPublisher())
            mContext.startActivity(intent)
        }
        holder.comments_button.setOnClickListener {
            val intent = Intent(mContext, AddCommentsActivity::class.java)
            intent.putExtra("postID", current_post.getPostID())
            intent.putExtra("postPublisher", current_post.getPostPublisher())
            mContext.startActivity(intent)
        }
        checkSaveStatus(holder.save_btn, current_post.getPostID())
        holder.save_btn.setOnClickListener {
            when (holder.save_btn.tag) {
                "saved" -> {
                    FirebaseDatabase.getInstance().reference
                        .child("saves")
                        .child(firebaseUser!!.uid)
                        .child(current_post.getPostID())
                        .removeValue()
                }
                "not saved" -> {
                    FirebaseDatabase.getInstance().reference
                        .child("saves")
                        .child(firebaseUser!!.uid)
                        .child(current_post.getPostID())
                        .setValue(true)
                }
            }

        }

        holder.postProfileImage.setOnClickListener {
            alertProfile(it, current_post.getPostPublisher())
        }


    }


    private fun alertProfile(v: View, userUID: String) {
        val builder = AlertDialog.Builder(mContext)
        val dialogLayout = LayoutInflater.from(mContext).inflate(R.layout.profile_dialog, null)

        builder.setPositiveButton("View Profile", DialogInterface.OnClickListener { dialog, which ->
            val pref: SharedPreferences.Editor =
                mContext?.getSharedPreferences("Prefs", Context.MODE_PRIVATE)!!.edit()
            pref.putString("profileID", userUID)
            pref.apply()

            (mContext as FragmentActivity)
                .supportFragmentManager
                .beginTransaction().replace(R.id.fragmentContainer, ProfileFragment()).commit()
            dialog.dismiss()
        })
         builder.setNeutralButton("Follow", DialogInterface.OnClickListener { dialog, which ->
             dialog.dismiss()
        })

        builder.setView(dialogLayout)
        builder.show()
    }


    private fun checkSaveStatus(saveBtn: ImageView, postImageUrl: String) {
        val saveRef = FirebaseDatabase.getInstance().reference
            .child("saves")
            .child(firebaseUser!!.uid)
        saveRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(postImageUrl).exists()) {
                    saveBtn.setImageResource(R.drawable.saved)
                    saveBtn.tag = "saved"
                } else {
                    saveBtn.setImageResource(R.drawable.unsaved)
                    saveBtn.tag = "not saved"

                }
            }
        })

    }

    private fun no_of_comments(postID: String, noOfComments: TextView, commentInfo: TextView) {
        FirebaseDatabase
            .getInstance().reference
            .child("comments")
            .child(postID)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {

                }

                @SuppressLint("SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        noOfComments.text = snapshot.childrenCount.toString() + " comments"
                        commentInfo.text =
                            "View all " + snapshot.childrenCount.toString() + " comments"
                    }
                }
            })
    }

    private fun no_of_likes(postID: String, noOfLikes: TextView) {
        val likesRef = FirebaseDatabase.getInstance().reference
            .child("likes")
            .child(postID)
        likesRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val likes = snapshot.childrenCount.toString()
                    if (likes == "0") {
                        noOfLikes.text = "0 likes"
                    } else {

                        noOfLikes.text = likes + " likes"
                    }


                }
            }
        })

    }


    private fun checkLikeStatus(likeButton: ImageView, postID: String) {
        val likesRef = FirebaseDatabase.getInstance().reference
            .child("likes")
            .child(postID)
        likesRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(firebaseUser!!.uid).exists()) {

                    likeButton.setImageResource(R.drawable.heart_clicked)
                    likeButton.tag = "liked"
                } else {

                    likeButton.setImageResource(R.drawable.heart_not_clicked)
                    likeButton.tag = "not Liked"

                }
            }
        })

    }


    private fun updateUserInfo(
        username: TextView,
        bio: TextView,
        postProfileImage: CircleImageView,
        postPublisher: String,
        name: TextView
    ) {
        val userRef = FirebaseDatabase.getInstance().reference
            .child("Users")
            .child(postPublisher)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {

                    val new_user = snapshot.getValue(Users::class.java)
                    if (new_user != null) {
                        Picasso.get()
                            .load(new_user.getProfilePicUrl())
                            .into(postProfileImage)
                        username.text = new_user.getFullname()
                        name.text = new_user.getUsername()
                        bio.text = new_user.getBio()
                    }


                }
            }
        })
    }
}