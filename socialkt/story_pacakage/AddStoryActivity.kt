package com.example.socialkt.story_pacakage

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.socialkt.MainActivity
import com.example.socialkt.R
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage

class AddStoryActivity : AppCompatActivity() {
    private var imageUri: Uri? = null
    private var storageReference: StorageReference? = null
    private var storyUrl: String = ""
    private var firebaseUser: FirebaseUser? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_story)
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        storageReference = FirebaseStorage.getInstance().reference.child("story pictures")
        CropImage
            .activity()
            .setAspectRatio(9, 16)
            .start(this)


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            var result = CropImage.getActivityResult(data)
            imageUri = result.uri
            uploadStoryImage()
        }
    }

    private fun uploadStoryImage() {
        when (imageUri) {
            null -> {
                Toast.makeText(this, "please select an image", Toast.LENGTH_SHORT).show()
            }
            else -> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Adding your Story")
                progressDialog.setMessage("Please wait ...")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()

                val storyRef =
                    storageReference!!.child(System.currentTimeMillis().toString() + ".jpg")
                val uploadTask: StorageTask<*>
                uploadTask = storyRef.putFile(imageUri!!)
                uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful) {
                        task.exception!!.let { throw it }
                        progressDialog.dismiss()
                    }
                    return@Continuation storyRef.downloadUrl
                }).addOnCompleteListener(OnCompleteListener<Uri> { task ->
                    if (task.isSuccessful) {
                        val downloadUrl = task.result
                        storyUrl = downloadUrl.toString()
                        val timeend = System.currentTimeMillis() + 86400000

                        val storyRef = FirebaseDatabase.getInstance().reference
                            .child("story") .child(FirebaseAuth.getInstance().currentUser!!.uid)


                        val storyID = storyRef.push().key.toString()
                        val storyMap = HashMap<String, Any>()
                        storyMap["imageUrl"] = storyUrl
                        storyMap["timestart"] = ServerValue.TIMESTAMP
                        storyMap["timeend"] = timeend
                        storyMap["storyID"] = storyID
                        storyMap["userUID"] = firebaseUser!!.uid
                        storyRef.child(storyID).updateChildren(storyMap)
                        progressDialog.dismiss()
                        Toast.makeText(this, "Story uploaded successfully", Toast.LENGTH_SHORT).show()

                        val intent =Intent(this, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)

                    }
                })

            }
        }

    }
}
