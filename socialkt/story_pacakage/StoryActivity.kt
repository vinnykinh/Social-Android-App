package com.example.socialkt.story_pacakage

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
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
import jp.shts.android.storiesprogressview.StoriesProgressView
import kotlinx.android.synthetic.main.activity_story.*

class StoryActivity : AppCompatActivity(), StoriesProgressView.StoriesListener {
    private var currentUID: FirebaseUser? = null
    private var userUID: String = ""
    private var imagesList: MutableList<String>? = null
    private var storyIDSList: MutableList<String>? = null

    var counter = 0
    var pressTime = 0L
    var limit = 500L

    private val onTouchEvent = View.OnTouchListener { view, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                pressTime = System.currentTimeMillis()
                storiesProgressView!!.pause()
                return@OnTouchListener false
            }
            MotionEvent.ACTION_UP -> {
                val now = System.currentTimeMillis()
                storiesProgressView!!.pause()
                return@OnTouchListener limit < now - pressTime

            }

        }
        false
    }
    private var storiesProgressView: StoriesProgressView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story)

        currentUID = FirebaseAuth.getInstance().currentUser!!
        storiesProgressView = findViewById(R.id.stories_progress)

        val intent = intent
        this.userUID = intent.getStringExtra("userUID").toString()

        seen_number.setOnClickListener {
            val intent = Intent(this, ShowUsersActivity::class.java)
            intent.putExtra("id",userUID)
            intent.putExtra("title",storyIDSList!![counter])
            startActivity(intent)
        }
        delete_story.setOnClickListener {
            val ref = FirebaseDatabase.getInstance().reference
                .child("story")
                .child(userUID)
                .child(storyIDSList!![counter])
            ref.removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, task.exception!!.toString(), Toast.LENGTH_SHORT).show()
                }
            }
        }
        layout_seen.visibility = View.GONE
        delete_story.visibility = View.GONE
        if (userUID == currentUID!!.uid) {
            layout_seen.visibility = View.VISIBLE
            delete_story.visibility = View.VISIBLE
        }
        getStories()
        updateUserInfo(userUID)


        val reverse: View = findViewById(R.id.reverse)
        reverse.setOnClickListener { storiesProgressView!!.reverse() }
        reverse.setOnTouchListener(onTouchEvent)

        val skip: View = findViewById(R.id.forward)
        skip.setOnClickListener { storiesProgressView!!.reverse() }
        skip.setOnTouchListener(onTouchEvent)
    }
    private fun addViewsToStory(storyID:String){
        val ref = FirebaseDatabase.getInstance().reference
            .child("story")
            .child(userUID)
            .child(storyID)
            .child("views")
            .child(currentUID!!.uid)
            .setValue(true)

    }
    private fun seenNumber(storyID: String){
        FirebaseDatabase.getInstance().reference
            .child("story")
            .child(userUID)
            .child(storyID)
            .child("views")
            .addValueEventListener(object :ValueEventListener{
                override fun onCancelled(error: DatabaseError) {

                }

                @SuppressLint("SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        seen_number.text ="seen by "+ snapshot.childrenCount.toString()
                    }
                }
            })
    }

    private fun getStories() {
        storyIDSList =ArrayList()
        imagesList =ArrayList()
        FirebaseDatabase.getInstance().reference
            .child("story")
            .child(userUID)
            .addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onCancelled(error: DatabaseError) {


                }

                override fun onDataChange(snapshot: DataSnapshot) {
                  if (snapshot.exists())
                  {
                      (storyIDSList as ArrayList<String>).clear()
                      (imagesList as ArrayList<String>).clear()
                      for (i in snapshot.children)
                      {
                          val timeNow  = System.currentTimeMillis()
                          val story = i.getValue(Story::class.java)
                          if (timeNow>story!!.getTimestart() && timeNow<story.getTimeend()){
                              (storyIDSList as ArrayList<String>).add(story.getStoryID())
                              (imagesList as ArrayList<String>).add(story.getImageUrl())
                          }
                      }
                      storiesProgressView!!.setStoriesCount(imagesList!!.size)
                      storiesProgressView!!.setStoriesListener(this@StoryActivity)
                      storiesProgressView!!.setStoryDuration(3000L)
                      storiesProgressView!!.startStories()
                      Picasso.get().load(imagesList!![counter]).placeholder(R.drawable.profile).into(image_story)
                      seenNumber(storyIDSList!![counter])
                      addViewsToStory(storyIDSList!![counter])
                  }

                }
            })


    }

    private fun updateUserInfo(userUID: String) {
        FirebaseDatabase.getInstance().reference
            .child("Users")
            .child(userUID)
            .addValueEventListener(object :ValueEventListener{
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        val user = snapshot.getValue(Users::class.java)
                        Picasso.get().load(user!!.getProfilePicUrl()).placeholder(R.drawable.profile).into(story_profile_image)
                        story_username.text = user!!.getUsername()

                    }
                }
            })

    }

    override fun onComplete() {
      finish()
    }

    override fun onPrev() {
        Picasso.get().load(imagesList!![--counter]).placeholder(R.drawable.profile).into(image_story)
        seenNumber(storyIDSList!![counter])
        addViewsToStory(storyIDSList!![counter])
    }

    override fun onNext() {
        Picasso.get().load(imagesList!![++counter]).placeholder(R.drawable.profile).into(image_story)
        seenNumber(storyIDSList!![counter])
        addViewsToStory(storyIDSList!![counter])
    }

    override fun onPause() {
       super.onPause()
        storiesProgressView!!.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        storiesProgressView!!.destroy()
    }




}
