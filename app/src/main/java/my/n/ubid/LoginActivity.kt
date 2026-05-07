package my.n.ubid

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth
        auth = Firebase.auth

        // Find views
        val etEmail = findViewById<EditText>(R.id.et_email)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val btnSignIn = findViewById<Button>(R.id.btn_sign_in)
        val btnCreateAccount = findViewById<Button>(R.id.btn_create_account)
        val tvSignUpLink = findViewById<TextView>(R.id.tv_sign_up_link)

        // Handle Sign In button click
        btnSignIn.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both Email and Passphrase.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Firebase Authentication: Sign In
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Sign in successful!", Toast.LENGTH_SHORT).show()

                        // ── ROUTE TO DASHBOARD ──
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish() // Close LoginActivity so the user can't hit the back button to log out

                    } else {
                        Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        // Navigate to Sign Up screen via the Outline Button
        btnCreateAccount.setOnClickListener {
            navigateToSignUp()
        }

        // Navigate to Sign Up screen via the bottom text link
        tvSignUpLink.setOnClickListener {
            navigateToSignUp()
        }
    }

    private fun navigateToSignUp() {
        val intent = Intent(this, SignupActivity::class.java)
        startActivity(intent)
    }

    // ── SKIP LOGIN IF ALREADY SIGNED IN ──
    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // User is signed in, skip login screen and go straight to Dashboard
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}