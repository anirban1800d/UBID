package my.n.ubid

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class BusinessListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_business_list)

        recyclerView = findViewById(R.id.rv_business_list)
        recyclerView.layoutManager = LinearLayoutManager(this)

        fetchData()
    }

    private fun fetchData() {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(AiApiService::class.java)

        lifecycleScope.launch {
            try {
                val response = api.getRecommendations("BUID30101", 50)
                val mappedList = response.recommendations.map {
                    Business(it.BUID ?: "", it.name ?: "", "", it.market ?: "", "",
                        it.Rule_Based_Label ?: "", it.region ?: "", it.city ?: "",
                        "", 0, it.Similarity_Score ?: 0.0, "")
                }

                // Pass data AND click behavior to adapter
                recyclerView.adapter = BusinessAdapter(mappedList) { business ->
                    val intent = Intent(this@BusinessListActivity, BusinessDetailActivity::class.java)
                    intent.putExtra("BUSINESS_DATA", business)
                    startActivity(intent)
                }
            } catch (e: Exception) {
                Toast.makeText(this@BusinessListActivity, "API Error", Toast.LENGTH_SHORT).show()
            }
        }
    }
}