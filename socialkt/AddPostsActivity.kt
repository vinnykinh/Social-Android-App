package com.example.socialkt

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentProvider
import android.content.ContentResolver
import android.content.Intent
import android.media.Image
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.Toast
import com.example.socialkt.Models.Users
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_add_posts.*
import java.time.DateTimeException
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.coroutines.Continuation
import kotlin.math.roundToInt

class AddPostsActivity : AppCompatActivity() {
    private var postUri: Uri? = null
    private var postUrl: String = ""
    private var firebaseUser: FirebaseUser? = null
    private var postStorageRef: StorageReference? = null

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_posts)
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        postStorageRef = FirebaseStorage.getInstance().reference.child("post pictures")
        val postImage = findViewById<ImageView>(R.id.post_image_add_frag)
        postImage.setOnClickListener {
            CropImage.activity()
                .setAspectRatio(5, 3)
                .start(this)
        }
        val back_add_button: ImageView = findViewById(R.id.back_add_button)
        val addPostBtn: FloatingActionButton = findViewById(R.id.AddPost_Button)
        back_add_button.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        addPostBtn.setOnClickListener {
            when {
                postImage == null -> {
                    Toast.makeText(this, "please select an image", Toast.LENGTH_SHORT).show()
                }
                post_description.text.toString() == "" -> {
                    Toast.makeText(this, "please enter a post description", Toast.LENGTH_SHORT)
                        .show()

                }
                else -> {
                    val progressDialog = ProgressDialog(this)
                    progressDialog.setTitle("Uploading")
                    progressDialog.setMessage("please wait...")
                    progressDialog.setCanceledOnTouchOutside(false)
                    progressDialog.show()
                    val postRef = postStorageRef!!.child(
                        System.currentTimeMillis().toString() + getFileExtension(postUri!!)
                    )
                    val uploadTask: StorageTask<*>
                    uploadTask = postRef.putFile(postUri!!)
                    uploadTask.continueWithTask(com.google.android.gms.tasks.Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                        if (!task.isSuccessful) {
                            task.exception!!.let { throw  it }
                        }
                        return@Continuation postRef.downloadUrl
                    }).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val downloadUrl = task.result
                            postUrl = downloadUrl.toString()


                            val postRef = FirebaseDatabase.getInstance().reference
                                .child("posts")
                            val postID = postRef.push().key
                            val postDataMap = HashMap<String, Any>()
                            postDataMap["postID"] = postID!!
                            postDataMap["postImageUrl"] = postUrl
                            postDataMap["postPublisher"] = firebaseUser!!.uid
                            postDataMap["postDescription"] = post_description.text.toString()
                            postRef
                                .child(postID)
                                .updateChildren(postDataMap)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        progressDialog.dismiss()
                                        val intent = Intent(this, MainActivity::class.java)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                        startActivity(intent)
                                        Toast.makeText(
                                            this,
                                            "Post uploaded successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }

                        }
                    }


                }

            }

        }
        updateUserProfilePic()
    }

    private fun getFileExtension(uri: Uri): String? {
        val contentResolver: ContentResolver = getContentResolver()
        val mimeTypeMap: MimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getMimeTypeFromExtension(contentResolver.getType(uri))
    }

    private fun updateUserProfilePic() {
        val ref = FirebaseDatabase.getInstance().reference
            .child("Users")
            .child(firebaseUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val user = snapshot.getValue(Users::class.java)
                        if (user != null) {
                            Picasso.get()
                                .load(user.getProfilePicUrl())
                                .placeholder(R.drawable.profile)
                                .into(appPost_profileImage)

                        }
                    }
                }
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val result = CropImage.getActivityResult(data)
            postUri = result.uri
            notification.visibility = View.GONE
            Picasso.get()
                .load(postUri)

                .into(post_image_add_frag!!)

        }
    }
}
