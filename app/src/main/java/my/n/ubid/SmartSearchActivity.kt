package my.n.ubid

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SmartSearchActivity : AppCompatActivity() {

    // 1. Declare our UI elements
    private lateinit var spinnerCategory: Spinner
    private lateinit var spinnerRegion: Spinner
    private lateinit var etNlpQuery: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        // Load the Smart Search layout
        setContentView(R.layout.fragment_smart_search) // (Or activity_smart_search depending on what you named it)

        // Handle Window Insets for the dark background
        val rootView = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ══════════════════════════════════════════════════
        // SEARCH & FILTER LOGIC
        // ══════════════════════════════════════════════════

        // 2. Initialize the views from XML
        spinnerCategory = findViewById(R.id.spinnerCategory)
        spinnerRegion = findViewById(R.id.spinnerRegion)
        etNlpQuery = findViewById(R.id.etNlpQuery)

        // --- NEW: Apply custom colors to Spinners ---

        // Setup Category Spinner
        val categoryAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.category_list,
            R.layout.item_spinner_text // Uses our new custom color layout
        )
        categoryAdapter.setDropDownViewResource(R.layout.item_spinner_text)
        spinnerCategory.adapter = categoryAdapter

        // Setup Region Spinner
        val regionAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.region_list,
            R.layout.item_spinner_text // Uses our new custom color layout
        )
        regionAdapter.setDropDownViewResource(R.layout.item_spinner_text)
        spinnerRegion.adapter = regionAdapter

        // ---------------------------------------------

        // 3. Listen for the user hitting the "Search" button on their keyboard
        etNlpQuery.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true // Consumes the action so the keyboard hides properly
            } else {
                false
            }
        }

        // ══════════════════════════════════════════════════
        // BOTTOM NAVIGATION LOGIC
        // ══════════════════════════════════════════════════

        // 1. Go to Home (Dashboard)
        val navHome = findViewById<LinearLayout>(R.id.nav_home)
        navHome?.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            // Clears the back stack so we don't open infinite pages
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        // 2. Go to List
        val navList = findViewById<LinearLayout>(R.id.nav_list)
        navList?.setOnClickListener {
            val intent = Intent(this, BusinessListActivity::class.java)
            startActivity(intent)
            finish() // Closes search so hitting back from the list doesn't get messy
        }

        // 3. Go to Graph
        val navGraph = findViewById<LinearLayout>(R.id.nav_graph)
        navGraph?.setOnClickListener {
            val intent = Intent(this, GraphViewActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // ══════════════════════════════════════════════════
    // SEARCH FUNCTION EXECUTION
    // ══════════════════════════════════════════════════

    private fun performSearch() {
        // Grab the text the user typed
        val query = etNlpQuery.text.toString().trim()

        // Grab the selected text from the Spinners
        val selectedCategory = spinnerCategory.selectedItem.toString()
        val selectedRegion = spinnerRegion.selectedItem.toString()

        // Check if they actually selected a filter, or if it's still on "Select..." (Index 0)
        val categoryFilter = if (spinnerCategory.selectedItemPosition > 0) selectedCategory else "None"
        val regionFilter = if (spinnerRegion.selectedItemPosition > 0) selectedRegion else "None"

        // --- DO SOMETHING WITH THE DATA ---
        // For now, show a toast to prove that it captures all 3 inputs correctly!
        val debugMessage = "Query: $query\nCat: $categoryFilter\nReg: $regionFilter"
        Toast.makeText(this, debugMessage, Toast.LENGTH_LONG).show()

        // TODO: Pass 'query', 'categoryFilter', and 'regionFilter' to your AI Backend via Retrofit
    }
}