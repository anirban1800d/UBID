package my.n.ubid

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BusinessAdapter(
    private var businessList: List<Business>, // MERGE NOTE: Changed to 'var' so search can update it
    private val onItemClick: (Business) -> Unit // The click listener function
) : RecyclerView.Adapter<BusinessAdapter.BusinessViewHolder>() {

    class BusinessViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvBizName: TextView = view.findViewById(R.id.tv_biz_name)
        val tvRiskScore: TextView = view.findViewById(R.id.tv_risk_score)
        val tvUbid: TextView = view.findViewById(R.id.tv_ubid)
        val tvMeta: TextView = view.findViewById(R.id.tv_meta)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BusinessViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_business_card, parent, false)
        return BusinessViewHolder(view)
    }

    override fun onBindViewHolder(holder: BusinessViewHolder, position: Int) {
        val business = businessList[position]

        holder.tvBizName.text = business.name
        holder.tvUbid.text = business.buid
        holder.tvRiskScore.text = String.format("%.1f", business.score)

        // Safely format the meta text to prevent crashes if fields are missing
        val status = business.status.ifEmpty { "UNKNOWN" }
        val market = business.market.ifEmpty { "General" }
        val city = business.city.ifEmpty { "N/A" }
        holder.tvMeta.text = "${status.uppercase()} · $market · $city"

        // Color Logic
        val color = when {
            business.score >= 75 -> Color.parseColor("#FCA5A5")
            business.score >= 50 -> Color.parseColor("#F9A825")
            else -> Color.parseColor("#6EE7B7")
        }
        holder.tvRiskScore.setTextColor(color)

        // Setting the click
        holder.itemView.setOnClickListener { onItemClick(business) }
    }

    override fun getItemCount() = businessList.size

    // MERGE NOTE: Added this helper function.
    // This allows your SmartSearchActivity to dynamically swap out
    // the list of businesses when the AI returns new search results!
    fun updateData(newList: List<Business>) {
        businessList = newList
        notifyDataSetChanged()
    }
}