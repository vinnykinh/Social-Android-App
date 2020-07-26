package com.example.socialkt.story_pacakage

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
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
import kotlinx.android.synthetic.main.add_story_card.view.*
import java.util.*

class StoryAdapter constructor(private var mContext: Context,private var mList:List<Story>):RecyclerView.Adapter<StoryAdapter.StoryViewHolder>() {
   private var firebaseUser:FirebaseUser?=null
    class StoryViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        //add story
        var add_story_btn:CircleImageView?=null
        var add_story_text:TextView?=null
        var add_story_image:CircleImageView?=null


        //story item
        var story_username:TextView?=null
        var story_seen_image:CircleImageView?=null
        var story_not_seen_image:CircleImageView?=null

        init {
            //add story
            add_story_btn =itemView.findViewById(R.id.add_story_button)
            add_story_image=itemView.findViewById(R.id.add_story_image)
            add_story_text =itemView.findViewById(R.id.addStory_text)

            //story item
            story_username =itemView.findViewById(R.id.username)
            story_seen_image =itemView.findViewById(R.id.story_image_seen)
            story_not_seen_image =itemView.findViewById(R.id.story_image_not_seen)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        when(viewType)
        {
            0-> {
                val View =
                    LayoutInflater.from(mContext).inflate(R.layout.add_story_card, parent, false)
                return StoryViewHolder(View)
            }
            else->{
                val View =
                    LayoutInflater.from(mContext).inflate(R.layout.story_card, parent, false)
                return StoryViewHolder(View)
            }
        }

    }

    override fun getItemCount(): Int {
         return mList.size
    }

    override fun getItemViewType(position: Int): Int {
        when(position){
            0->{
                return 0
            }
            else ->{
                return  1

            }
        }
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val current_story =mList[position]
        firebaseUser =FirebaseAuth.getInstance().currentUser!!
        updateUserInfo(holder,current_story.getUserUID(),position)
        holder.itemView.setOnClickListener {
            if (holder.adapterPosition ===0)
            {
                myStories(holder,true)
            }
            else if (holder.adapterPosition !==0)
            {
                otherStories(holder,current_story.getUserUID(),true)
            }
        }
        if (holder.adapterPosition ===0)
        {
            myStories(holder,false)
        }
        else if (holder.adapterPosition !==0)
        {
            otherStories(holder,current_story.getUserUID(), false)
        }

    }

    private fun otherStories(holder: StoryAdapter.StoryViewHolder, userUID: String, click: Boolean) {
         FirebaseDatabase.getInstance().reference.child("story")
             .child(userUID)
             .addListenerForSingleValueEvent(object :ValueEventListener{
                 override fun onCancelled(error: DatabaseError) {
                     TODO("Not yet implemented")
                 }

                 override fun onDataChange(snapshot: DataSnapshot) {
                     var otherCounter =0
                     val timeNow =System.currentTimeMillis()
                     for (i in snapshot.children)
                     {
                         val new_story = i.getValue(Story::class.java)
                         if (snapshot.child("views").child(firebaseUser!!.uid).exists() && timeNow<new_story!!.getTimeend() && timeNow>new_story.getTimestart() ){
                             otherCounter++
                         }
                     }
                     when(click)
                     {
                         true ->{
                            val intent = Intent(mContext,StoryActivity::class.java)
                             intent.putExtra("userUID",userUID)
                             mContext.startActivity(intent)

                         }
                         else ->{
                             if (otherCounter>0){
                                 holder.story_not_seen_image!!.visibility =View.GONE
                                 holder.story_seen_image!!.visibility =View.VISIBLE
                             }
                             else{
                                 holder.story_not_seen_image!!.visibility =View.VISIBLE
                                 holder.story_seen_image!!.visibility =View.GONE
                             }
                         }
                     }
                 }
             })
    }


    private fun myStories(holder: StoryAdapter.StoryViewHolder, b: Boolean) {
        val storyRef = FirebaseDatabase.getInstance().reference.child("story")
            .child(firebaseUser!!.uid)
            .addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val currentTime = System.currentTimeMillis()
                    var counter= 0
                    for (i in snapshot.children)
                    {
                        val story = i.getValue(Story::class.java)
                        if (currentTime<story!!.getTimeend() && currentTime>story.getTimestart()){
                            counter++
                        }
                    }
                   when(b)
                   {
                       true ->{
                           if (counter>0){
                               val builder = AlertDialog.Builder(mContext).create()
                               builder.apply {
                                   setButton(AlertDialog.BUTTON_POSITIVE,"View Story",DialogInterface.OnClickListener { dialog, which ->
                                       val intent = Intent(mContext,StoryActivity::class.java)
                                       intent.putExtra("userUID",firebaseUser!!.uid)
                                       mContext.startActivity(intent)
                                       dialog.dismiss()
                                   })
                                   setButton(AlertDialog.BUTTON_NEUTRAL,"Add Story",DialogInterface.OnClickListener { dialog, which ->
                                       mContext.startActivity(Intent(mContext,AddStoryActivity::class.java))
                                       dialog.dismiss()
                                   })
                               }
                               builder.show()
                           }
                           else
                           {
                               mContext.startActivity(Intent(mContext,AddStoryActivity::class.java))
                           }
                       }
                       else->{
                           if (counter>0)
                           {
                               holder.add_story_btn!!.visibility =View.GONE
                               holder.add_story_text!!.text = "View Story"
                           }
                           else
                           {
                               holder.add_story_btn!!.visibility = View.VISIBLE
                               holder.add_story_text!!.text = "Add Story"
                           }

                       }
                   }
                }
            })

    }


    private fun updateUserInfo(holder: StoryAdapter.StoryViewHolder, userUID: String, position: Int) {
        FirebaseDatabase.getInstance().reference.child("Users").child(userUID).addValueEventListener(object :ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val new_user = snapshot.getValue(Users::class.java)
                    if (new_user!=null)
                    {
                        when(position)
                        {
                            0->{
                                Picasso.get().load(new_user.getProfilePicUrl()).placeholder(R.drawable.profile).into(holder.add_story_image)
                            }
                            else->{
                                Picasso.get().load(new_user.getProfilePicUrl()).placeholder(R.drawable.profile).into(holder.story_seen_image)
                                Picasso.get().load(new_user.getProfilePicUrl()).placeholder(R.drawable.profile).into(holder.story_not_seen_image)
                                holder.story_username!!.text = new_user.getUsername()
                            }
                        }
                    }
                }
            }
        })

    }
}