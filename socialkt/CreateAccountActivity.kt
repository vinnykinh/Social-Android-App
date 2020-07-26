package com.example.socialkt

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_create_account.*
import java.util.regex.Pattern

class CreateAccountActivity : AppCompatActivity() {
    private val PASSWORD_PATTERN: Pattern =
        Pattern.compile(
            "^" +
                    "(?=.*[0-9])" +
                    "(?=.*[a-z])" +
                    "(?=.*[A-Z])" +
                    ".{6,}" +
                    "$"
        )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        login_link.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        createAccountBtn.setOnClickListener {
            createAccount()
        }


    }

    private fun createAccount() {
        val email = createEmail.text.toString().trim()
        val fullnames = createFullname.text.toString().trim()
        val username = createUsername.text.toString().trim()
        val password = createPassword.text.toString().trim()
        when {
            TextUtils.isEmpty(fullnames) -> {
                createFullname.setError("Field can't be empty")
            }
            TextUtils.isEmpty(username) -> {
                createUsername.setError("Field can't be empty")
            }
            !validateEmail() -> {
            }
            !validatePassword() -> {

            }

            else -> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Creating Account")
                progressDialog.setMessage("please wait...")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()
                val mAuth = FirebaseAuth.getInstance()
                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            saveUserData()
                        }
                    }

            }

        }
    }

    private fun saveUserData() {
        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Creating Account")
        progressDialog.setMessage("please wait...")
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.show()
        val userDbRef = FirebaseDatabase.getInstance().reference
            .child("Users")
        val userID = FirebaseAuth.getInstance().currentUser!!.uid
        val userDataHashMap = HashMap<String, Any>()

        userDataHashMap["fullname"] = createFullname.text.toString().trim()
        userDataHashMap["username"] = createUsername.text.toString().trim().toLowerCase()
        userDataHashMap["email"] = createEmail.text.toString().trim()
        userDataHashMap["password"] = createPassword.text.toString().trim()
        userDataHashMap["userUID"] = userID
        userDataHashMap["bio"] ="create bio"
        userDataHashMap["userLocation"] ="Add Location City"
        userDataHashMap["profilePicUrl"] = "https://firebasestorage.googleapis.com/v0/b/socialkt-c1888.appspot.com/o/default%2Fprofile.png?alt=media&token=f1fde46f-d68e-4fd1-8763-5ece1ac07444"

        userDbRef
            .child(userID)
            .updateChildren(userDataHashMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    progressDialog.dismiss()
                    Toast.makeText(
                        this,
                        "Account created successfully",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
            }.addOnFailureListener { taskId ->
                Toast.makeText(
                    this,
                    taskId.message!!,
                    Toast.LENGTH_SHORT
                ).show()
            }

    }

    private fun validateEmail(): Boolean {
        return when {
            TextUtils.isEmpty(createEmail.text.toString()) -> {
                createEmail.setError("Field can't be empty")
                return false
            }
            !(Patterns.EMAIL_ADDRESS.matcher(createEmail.text.toString()).matches()) -> {
                createEmail.setError("Enter a valid Email Adress")
                return false
            }
            else -> {
                return true
            }
        }
    }

    private fun validatePassword(): Boolean {
        return when {
            TextUtils.isEmpty(createPassword.text.toString()) -> {
                createPassword.setError("Field can't be empty")
                return false
            }
            !(PASSWORD_PATTERN.matcher(createPassword.text.toString()).matches()) -> {
                createPassword.setError("Should contain atleast 1 uppercase,1 lowercase, 1 digit")
                return false
            }
            else -> {
                return true
            }
        }
    }
}
