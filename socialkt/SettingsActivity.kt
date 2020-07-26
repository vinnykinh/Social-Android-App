package com.example.socialkt

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.InetAddresses
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.socialkt.Models.Users
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
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
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {
    private var checker: String = ""
    private var profileImageUri: Uri? = null
    private var myImageUrl: String = ""
    private var firebaseUser: FirebaseUser? = null
    private var profilePicReference: StorageReference? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        profilePicReference = FirebaseStorage.getInstance().reference.child("profilePic")

        back_settings_btn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        log_out_button.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }
        change_profile_image_btn.setOnClickListener {
            checker = "checked"
            CropImage
                .activity()
                .setAspectRatio(1, 1)
                .start(this)
        }
        save_button_settings.setOnClickListener {
            if (checker == "checked") {
                updateProfileImageAndUserInfo()

            } else if (checker == "") {
                updateUserInfoOnly()
            }
        }
        updateUser()
    }

    private fun updateUserInfoOnly() {
        val fullname = fullname_editText.text.toString().trim()
        val username = username_editText.text.toString().trim()
        val userLocation = location_editText.text.toString().trim()
        val bio = bio_editText.text.toString().trim()
        when {
            fullname.isEmpty() -> {
                Toast.makeText(
                    this,
                    "fullname is required"
                    , Toast.LENGTH_SHORT
                ).show()
            }
            username.isEmpty() -> {
                Toast.makeText(
                    this,
                    "username is required"
                    , Toast.LENGTH_SHORT
                ).show()
            }
            userLocation.isEmpty() -> {
                Toast.makeText(
                    this,
                    "userLocation is required"
                    , Toast.LENGTH_SHORT
                ).show()
            }
            bio.isEmpty() -> {
                Toast.makeText(
                    this,
                    "bio is required"
                    , Toast.LENGTH_SHORT
                ).show()

            }
            else -> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Updating")
                progressDialog.setMessage("please wait ...")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()
                val userUID = firebaseUser!!.uid
                val userRef = FirebaseDatabase.getInstance().reference
                    .child("Users")
                val userMap = HashMap<String, Any>()
                userMap["fullname"] = fullname
                userMap["username"] = username.toLowerCase()
                userMap["userUID"] = userUID
                userMap["bio"] = bio
                userMap["userLocation"] = userLocation

                userRef
                    .child(firebaseUser!!.uid)
                    .updateChildren(userMap)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            progressDialog.dismiss()
                            Toast.makeText(
                                this, "Account information updated"
                                , Toast.LENGTH_SHORT
                            ).show()
                            val intent = Intent(this, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or  Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(intent)
                        }

                    }


            }

        }
    }


    private fun updateUser() {
        val userRef = FirebaseDatabase.getInstance().reference
            .child("Users")
            .child(firebaseUser!!.uid)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@SettingsActivity,
                    error.message
                    , Toast.LENGTH_SHORT
                ).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(Users::class.java)
                    if (user != null) {
                        Picasso.get()
                            .load(user.getProfilePicUrl())
                            .placeholder(R.drawable.profile)
                            .into(profilePic_settingsActivity)
                        fullname_editText.setText(user.getFullname())
                        username_editText.setText(user.getUsername())
                        bio_editText.setText(user.getBio())
                        location_editText.setText(user.getUserLocation())
                    }
                }
            }
        })
    }

    private fun updateProfileImageAndUserInfo() {
        val fullname = fullname_editText.text.toString().trim()
        val username = username_editText.text.toString().trim()
        val userLocation = location_editText.text.toString().trim()
        val bio = bio_editText.text.toString().trim()
        when {
            profilePic_settingsActivity == null -> {
                Toast.makeText(
                    this,
                    "please select an image"
                    , Toast.LENGTH_SHORT
                ).show()
            }
            fullname.isEmpty() -> {
                Toast.makeText(
                    this,
                    "fullname is required"
                    , Toast.LENGTH_SHORT
                ).show()
            }
            username.isEmpty() -> {
                Toast.makeText(
                    this,
                    "username is required"
                    , Toast.LENGTH_SHORT
                ).show()
            }
            userLocation.isEmpty() -> {
                Toast.makeText(
                    this,
                    "userLocation is required"
                    , Toast.LENGTH_SHORT
                ).show()
            }
            bio.isEmpty() -> {
                Toast.makeText(
                    this,
                    "bio is required"
                    , Toast.LENGTH_SHORT
                ).show()
            }
            else -> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Updating")
                progressDialog.setMessage("please wait ...")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()
                val fileRef = profilePicReference!!.child(firebaseUser!!.uid + ".jpg")
                val uploadTask: StorageTask<*>
                uploadTask = fileRef.putFile(profileImageUri!!)

                uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful) {
                        task.exception!!.let {
                            throw it
                        }
                    }
                    return@Continuation fileRef.downloadUrl
                }).addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        myImageUrl = task.result.toString()
                        val userUID = firebaseUser!!.uid
                        val userRef = FirebaseDatabase.getInstance().reference
                            .child("Users")
                        val userMap = HashMap<String, Any>()
                        userMap["fullname"] = fullname
                        userMap["username"] = username.toLowerCase()
                        userMap["userUID"] = userUID
                        userMap["profilePicUrl"] = myImageUrl
                        userMap["bio"] = bio
                        userMap["userLocation"] = userLocation

                        userRef
                            .child(firebaseUser!!.uid)
                            .updateChildren(userMap)
                            .addOnCompleteListener (OnCompleteListener{ task ->
                                if (task.isSuccessful) {
                                    progressDialog.dismiss()
                                    Toast.makeText(
                                        this, "Account information updated"
                                        , Toast.LENGTH_SHORT
                                    ).show()
                                    val intent = Intent(this, MainActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    startActivity(intent)
                                }

                            })


                    }
                }


            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            var result = CropImage.getActivityResult(data)
            profileImageUri = result.uri
            Picasso.get()
                .load(profileImageUri)
                .placeholder(R.drawable.profile)
                .into(profilePic_settingsActivity)

        }
    }
}
