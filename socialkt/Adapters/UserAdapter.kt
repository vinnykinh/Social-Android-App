package com.example.socialkt.Adapters

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.view.menu.MenuView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.socialkt.Fragments.ProfileFragment
import com.example.socialkt.Models.Users
import com.example.socialkt.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.user_card.view.*

class UserAdapter(
    private var mContext: Context,
    private var userList: List<Users>,
    private var isFragment: Boolean = false
) : RecyclerView.Adapter<UserAdapter.userViewHolder>() {
    private var firebaseUser: FirebaseUser? = null

    class userViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profile_pic = itemView.findViewById<CircleImageView>(R.id.profileImage_usercard)
        val username: TextView = itemView.findViewById(R.id.username_search)
        val bio: TextView = itemView.findViewById(R.id.bio_search)
        val followBtn: Button = itemView.findViewById(R.id.follow_btn_search)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): userViewHolder {
        val view = LayoutInflater
            .from(mContext)
            .inflate(R.layout.user_card, parent, false)
        return userViewHolder(view)
    }

    override fun getItemCount(): Int = userList.size

    override fun onBindViewHolder(holder: userViewHolder, position: Int) {
        val current = userList[position]
        Picasso.get()
            .load(current.getProfilePicUrl())
            .into(holder.profile_pic)
        holder.username.text = current.getUsername()
        holder.bio.text = current.getBio()
        firebaseUser =FirebaseAuth.getInstance().currentUser!!
        checkFollowingStatus(holder.followBtn,current.getUserUID())
        holder.followBtn.setOnClickListener {
            if (holder.followBtn.text =="Follow") {
               val followRef =  firebaseUser?.uid.let {  it1 ->
                   FirebaseDatabase.getInstance().reference
                       .child("follow")
                       .child(it1.toString())
                       .child("following")
                       .child(current.getUserUID())
                       .setValue(true)
                       .addOnCompleteListener { task ->
                           if (task.isSuccessful)
                           {
                               FirebaseDatabase.getInstance().reference
                                   .child("follow")
                                   .child(current.getUserUID())
                                   .child("followers")
                                   .child(it1.toString())
                                   .setValue(true)
                           }
                       }
               }
            }
            else {
                val followRef = firebaseUser?.uid.let { it1 ->
                    FirebaseDatabase.getInstance().reference
                        .child("follow")
                        .child(it1.toString())
                        .child("following")
                        .child(current.getUserUID())
                        .removeValue()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful)
                            {
                                FirebaseDatabase.getInstance().reference
                                    .child("follow")
                                    .child(current.getUserUID())
                                    .child("followers")
                                    .child(it1.toString())
                                    .removeValue()
                            }
                        }
                }

            }

        }
        holder.itemView.setOnClickListener {
            val pref: SharedPreferences.Editor? = mContext.getSharedPreferences("Prefs",Context.MODE_PRIVATE)!!.edit()
            pref?.putString("profileID",current.getUserUID())
            pref?.apply()
            (mContext as FragmentActivity)
                .supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainer,ProfileFragment())
                .commit()
        }


    }

    private fun checkFollowingStatus(followBtn: Button, userUID: String) {
        val followRef =FirebaseDatabase.getInstance().reference
            .child("follow")
            .child(firebaseUser!!.uid)
            .child("following")
            .addValueEventListener(object :ValueEventListener{
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {
                   if(snapshot.child(userUID).exists()) {
                       followBtn.text = "Following"
                   }
                    else{
                       followBtn.text ="Follow"

                   }
                }
            })

    }


}