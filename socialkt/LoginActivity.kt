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
import kotlinx.android.synthetic.main.activity_login.*
import java.util.regex.Pattern

class LoginActivity : AppCompatActivity() {
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
        setContentView(R.layout.activity_login)


        createLink_btn.setOnClickListener {
            val intent = Intent(this, CreateAccountActivity::class.java)
            startActivity(intent)
        }
        loginButton.setOnClickListener {
            loginAccount()
        }

    }

    private fun loginAccount() {
        val email = loginEmail.text.toString().trim()
        val password = loginPassword.text.toString().trim()
        when {

            !validateEmail() ->{}
                !validatePassword() -> {

            }

            else -> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Logging in Account")
                progressDialog.setMessage("please wait...")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()

                val mAuth = FirebaseAuth.getInstance()
                mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            progressDialog.dismiss()
                            Toast.makeText(
                                this,
                                "Account logged in successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                        }

                    }.addOnFailureListener { taskId ->
                        progressDialog.dismiss()
                        Toast.makeText(this,taskId.message!!,Toast.LENGTH_SHORT).show()
                    }


            }

        }
    }


    private fun validateEmail(): Boolean {
        return when {
            TextUtils.isEmpty(loginEmail.text.toString()) -> {
                loginEmail.setError("Field can't be empty")
                return false
            }
            !(Patterns.EMAIL_ADDRESS.matcher(loginEmail.text.toString()).matches()) -> {
                loginEmail.setError("Enter a valid Email Adress")
                return false
            }
            else -> {
                return true
            }
        }
    }

    private fun validatePassword(): Boolean {
        return when {
            TextUtils.isEmpty(loginPassword.text.toString()) -> {
                loginPassword.setError("Field can't be empty")
                return false
            }
            !(PASSWORD_PATTERN.matcher(loginPassword.text.toString()).matches()) -> {
                loginPassword.setError("Should contain atleast 1 uppercase,1 lowercase, 1 digit")
                return false
            }
            else -> {
                return true
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (FirebaseAuth.getInstance().currentUser!=null)
        {
            Toast.makeText(
                this,
                "Account logged in successfully",
                Toast.LENGTH_SHORT
            ).show()
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }

    }
}

