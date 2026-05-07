package my.n.ubid

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import my.n.ubid.R

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Initialize Firebase Auth
        auth = Firebase.auth

        // Find views
        val etEmailReg = findViewById<EditText>(R.id.et_email_reg)
        val etPassReg = findViewById<EditText>(R.id.et_pass_reg)
        val btnRegister = findViewById<Button>(R.id.btn_register)
        val llBack = findViewById<LinearLayout>(R.id.ll_back)
        val tvSignInLink = findViewById<TextView>(R.id.tv_sign_in_link)

        // Handle Register button click
        btnRegister.setOnClickListener {
            val email = etEmailReg.text.toString().trim()
            val password = etPassReg.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill out all required fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Passphrase must be at least 6 characters.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Firebase Authentication: Create User
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Account created successfully! Please sign in.", Toast.LENGTH_SHORT).show()
                        auth.signOut()
                        finish()
                    } else {
                        Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        // Navigate back to Login via the top back button
        llBack.setOnClickListener {
            finish()
        }

        // Navigate back to Login via the bottom text link
        tvSignInLink.setOnClickListener {
            finish()
        }
    }
}