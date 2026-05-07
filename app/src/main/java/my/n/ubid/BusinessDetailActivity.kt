package my.n.ubid

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class BusinessDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_company_detail)

        // 1. Retrieve the business object
        val business = intent.getSerializableExtra("BUSINESS_DATA") as? Business ?: return

        // 2. Populate Hero Header
        findViewById<TextView>(R.id.tvBizName).text = business.name
        findViewById<TextView>(R.id.tvBizAddress).text = "${business.city} · ${business.market}".uppercase()
        findViewById<TextView>(R.id.tvUbidValue).text = business.buid

        // 3. Populate Company Profile Card (Using confirmed Business.kt fields)
        findViewById<TextView>(R.id.tvOpStatus).text = business.status
        findViewById<TextView>(R.id.tvFunding).text = if (business.funding.isNotBlank()) business.funding else "N/A"
        findViewById<TextView>(R.id.tvCategory).text = if (business.categoryList.isNotBlank()) business.categoryList else "General"

        // Changed to .website to match your data class
        findViewById<TextView>(R.id.tvWebsite).text = if (business.website.isNotBlank()) business.website else "No Website Available"

        // 4. Populate AI Assessment Card
        val percentageScore = (business.score * 100).toInt()
        findViewById<TextView>(R.id.tvSimilarityScore).text = "$percentageScore%"
        findViewById<TextView>(R.id.tvRawScore).text = String.format("%.6f", business.score)

        // Changed to .rank to match your data class
        findViewById<TextView>(R.id.tvRank).text = "Rank #${business.rank}"

        val tvStatusTag = findViewById<TextView>(R.id.tvStatusTag)
        val tvRuleLabel = findViewById<TextView>(R.id.tvRuleLabel)

        // Use ruleLabel for risk assessment
        val ruleText = if (business.ruleLabel.isNotBlank()) business.ruleLabel else "Low"
        tvStatusTag.text = "${ruleText.uppercase()} RISK"
        tvRuleLabel.text = ruleText

        // 5. Apply Dynamic Visuals
        updateRiskUI(ruleText, percentageScore)

        // 6. Back navigation
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
    }

    private fun updateRiskUI(ruleLabel: String, percentageScore: Int) {
        val scoreBarFill = findViewById<View>(R.id.scoreBarFill)
        val tvSimilarityScore = findViewById<TextView>(R.id.tvSimilarityScore)
        val tvStatusTag = findViewById<TextView>(R.id.tvStatusTag)
        val tvRuleLabel = findViewById<TextView>(R.id.tvRuleLabel)
        val tvOpStatus = findViewById<TextView>(R.id.tvOpStatus)

        // Dynamically set the width of the score bar (Assuming 140dp max width)
        val density = resources.displayMetrics.density
        val maxWidthPx = (140 * density).toInt()
        val calculatedWidth = (maxWidthPx * (percentageScore / 100f)).toInt()

        val params = scoreBarFill.layoutParams
        params.width = calculatedWidth
        scoreBarFill.layoutParams = params

        // Color mapping based on Risk Level
        when (ruleLabel.lowercase()) {
            "high" -> {
                scoreBarFill.setBackgroundResource(R.drawable.bg_score_fill_red)
                tvStatusTag.setBackgroundResource(R.drawable.bg_tag_score_red)
                tvRuleLabel.setBackgroundResource(R.drawable.bg_tag_score_red)

                val color = ContextCompat.getColor(this, R.color.tag_red_text)
                setTextColor(color, tvSimilarityScore, tvStatusTag, tvRuleLabel, tvOpStatus)
            }
            "medium", "review" -> {
                scoreBarFill.setBackgroundResource(R.drawable.bg_score_fill_amber)
                tvStatusTag.setBackgroundResource(R.drawable.bg_tag_score_amber)
                tvRuleLabel.setBackgroundResource(R.drawable.bg_tag_score_amber)

                val color = ContextCompat.getColor(this, R.color.tag_amber_text)
                setTextColor(color, tvSimilarityScore, tvStatusTag, tvRuleLabel, tvOpStatus)
            }
            else -> {
                // "Low" or "Matched" gets GREEN
                scoreBarFill.setBackgroundResource(R.drawable.bg_score_fill_green)
                tvStatusTag.setBackgroundResource(R.drawable.bg_tag_score_green)
                tvRuleLabel.setBackgroundResource(R.drawable.bg_tag_score_green)

                val color = ContextCompat.getColor(this, R.color.tag_green_text)
                setTextColor(color, tvSimilarityScore, tvStatusTag, tvRuleLabel, tvOpStatus)
            }
        }
    }

    // Helper to keep code clean
    private fun setTextColor(color: Int, vararg views: TextView) {
        for (view in views) view.setTextColor(color)
    }
}