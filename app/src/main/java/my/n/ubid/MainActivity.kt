package my.n.ubid

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Initialize Firebase Auth
        auth = Firebase.auth

        // 2. Route to Login if the user is not authenticated
        if (auth.currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Close MainActivity
            return
        }

        // 3. Set up your custom UI
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        // 4. Handle Window Insets for your custom dark background
        val rootView = findViewById<android.view.View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ══════════════════════════════════════════════════
        // 5. LOGOUT LOGIC (Go back to LoginActivity)
        // ══════════════════════════════════════════════════
        val btnLogout = findViewById<FrameLayout>(R.id.btn_profile_logout)

        btnLogout?.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        // ══════════════════════════════════════════════════
        // 6. BOTTOM NAVIGATION
        // ══════════════════════════════════════════════════
        val navHome = findViewById<LinearLayout>(R.id.nav_home)
        navHome?.setOnClickListener {
            Toast.makeText(this, "Home Refreshed", Toast.LENGTH_SHORT).show()
        }

        val navList = findViewById<LinearLayout>(R.id.nav_list)
        navList?.setOnClickListener {
            val intent = Intent(this, BusinessListActivity::class.java)
            startActivity(intent)
        }

        val navSearch = findViewById<LinearLayout>(R.id.nav_search)
        navSearch?.setOnClickListener {
            val intent = Intent(this, SmartSearchActivity::class.java)
            startActivity(intent)
        }

        val navGraph = findViewById<LinearLayout>(R.id.nav_graph)
        navGraph?.setOnClickListener {
            val intent = Intent(this, GraphViewActivity::class.java)
            startActivity(intent)
        }

        // ══════════════════════════════════════════════════
        // 7. REMOTE AI CONNECTION LOGIC
        // ══════════════════════════════════════════════════
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val aiApi = retrofit.create(AiApiService::class.java)

        lifecycleScope.launch {
            try {
                val response = aiApi.getRecommendations("BUID30101", 3)
                val recs = response.recommendations

                val name1 = findViewById<TextView>(R.id.tv_biz_name_1)
                val status1 = findViewById<TextView>(R.id.tv_biz_status_1)

                val name2 = findViewById<TextView>(R.id.tv_biz_name_2)
                val status2 = findViewById<TextView>(R.id.tv_biz_status_2)

                val name3 = findViewById<TextView>(R.id.tv_biz_name_3)
                val status3 = findViewById<TextView>(R.id.tv_biz_status_3)


                if (recs.isNotEmpty()) {
                    name1?.text = recs[0].name ?: "Unknown Business"
                    applyStatusColor(status1, recs[0].Rule_Based_Label ?: "Low")
                }

                if (recs.size > 1) {
                    name2?.text = recs[1].name ?: "Unknown Business"
                    applyStatusColor(status2, recs[1].Rule_Based_Label ?: "Low")
                }

                if (recs.size > 2) {
                    name3?.text = recs[2].name ?: "Unknown Business"
                    applyStatusColor(status3, recs[2].Rule_Based_Label ?: "Low")
                }

            } catch (e: Exception) {
                Log.e("UBID_AI", "Connection failed: ${e.message}")
                findViewById<TextView>(R.id.tv_biz_status_1)?.text = "ERROR"
                findViewById<TextView>(R.id.tv_biz_status_2)?.text = "ERROR"
                findViewById<TextView>(R.id.tv_biz_status_3)?.text = "ERROR"
            }
        }
    }

    // ══════════════════════════════════════════════════
    // 8. HELPER FUNCTION
    // ══════════════════════════════════════════════════
    private fun applyStatusColor(textView: TextView?, label: String) {
        textView?.text = label.uppercase()
        when (label) {
            "High" -> {
                textView?.setBackgroundResource(R.drawable.bg_tag_red)
                textView?.setTextColor(getColor(R.color.tag_red_text))
            }
            "Medium" -> {
                textView?.setBackgroundResource(R.drawable.bg_tag_amber)
                textView?.setTextColor(getColor(R.color.tag_amber_text))
            }
            else -> { // Low / Match
                textView?.setBackgroundResource(R.drawable.bg_tag_green)
                textView?.setTextColor(getColor(R.color.tag_green_text))
            }
        }
    }
}