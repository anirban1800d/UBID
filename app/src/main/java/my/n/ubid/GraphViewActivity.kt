package my.n.ubid

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class GraphViewActivity : AppCompatActivity() {

    private lateinit var etBuid: TextInputEditText
    private lateinit var btnLoadGraph: MaterialButton
    private lateinit var networkGraph: NetworkGraphView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph_view)

        // 1. Initialize Views
        etBuid = findViewById(R.id.et_buid)
        btnLoadGraph = findViewById(R.id.btn_load_graph)
        networkGraph = findViewById(R.id.network_graph)

        // 2. MERGED: Handle Node Clicks to open BusinessDetailActivity
        // Inside GraphViewActivity.kt
        networkGraph.onNodeClicked = { clickedMatch ->

            // Convert the score to a decimal (e.g., 71.42 -> 0.7142)
            val scoreDecimal = (clickedMatch.Similarity_Score ?: 0.0) / 100.0

            // Format the funding number into a nice string (e.g., "$15000000")
            val formattedFunding = if (clickedMatch.fundingTotalUsd != null) {
                "$${clickedMatch.fundingTotalUsd}"
            } else {
                "N/A"
            }

            // Map ALL the AI data into the Business UI model
            val businessDetail = Business(
                buid = clickedMatch.BUID ?: "UNKNOWN",
                name = clickedMatch.name ?: "Unknown Business",
                website = clickedMatch.website ?: "",
                market = clickedMatch.market ?: "",
                funding = formattedFunding,
                status = clickedMatch.status ?: "AI Match",
                region = clickedMatch.region ?: "",
                city = clickedMatch.city ?: "Graph Search",
                categoryList = clickedMatch.categoryList ?: "",
                rank = clickedMatch.rank ?: 0,
                score = scoreDecimal,
                ruleLabel = clickedMatch.Rule_Based_Label ?: "Low"
            )

            // Launch the Detail Screen
            val intent = Intent(this@GraphViewActivity, BusinessDetailActivity::class.java)
            intent.putExtra("BUSINESS_DATA", businessDetail)
            startActivity(intent)
        }

        // 3. Button Click triggers AI Call
        btnLoadGraph.setOnClickListener {
            val input = etBuid.text.toString().trim()
            if (input.isNotEmpty()) {
                fetchAiData(input)
            } else {
                Toast.makeText(this, "Please enter a BUID", Toast.LENGTH_SHORT).show()
            }
        }

        // 4. Setup bottom navigation bar
        setupBottomNavigation()
    }

    private fun fetchAiData(buid: String) {
        // Connect to your local Python/FastAPI server
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(AiApiService::class.java)

        lifecycleScope.launch {
            try {
                // Fetch recommendations from AI
                val response = api.getRecommendations(buid, 6)

                // Push data to custom graph canvas to trigger drawing
                networkGraph.updateGraph(buid, response.recommendations)

            } catch (e: Exception) {
                Log.e("GraphActivity", "API Failed", e)
                Toast.makeText(this@GraphViewActivity, "Server Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupBottomNavigation() {
        findViewById<View>(R.id.nav_home).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        findViewById<View>(R.id.nav_list).setOnClickListener {
            startActivity(Intent(this, BusinessListActivity::class.java))
            finish()
        }
        // (Optional: add search navigation if needed)
    }
}