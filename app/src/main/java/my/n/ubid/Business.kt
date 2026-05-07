package my.n.ubid

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Business(
    @SerializedName("BUID", alternate = ["buid"])
    val buid: String = "",

    @SerializedName("name")
    val name: String = "",

    @SerializedName("homepage_url")
    val website: String = "",

    @SerializedName("market")
    val market: String = "",

    @SerializedName("funding_total_usd")
    val funding: String = "$0",

    @SerializedName("status")
    val status: String = "",

    @SerializedName("region")
    val region: String = "",

    @SerializedName("city")
    val city: String = "",

    @SerializedName("category_list")
    val categoryList: String = "",

    @SerializedName("Similarity_Rank", alternate = ["similarity_rank"])
    val rank: Int = 0,

    @SerializedName("Similarity_Score", alternate = ["similarity_score"])
    val score: Double = 0.0,

    @SerializedName("Rule_Based_Label", alternate = ["rule_based_label"])
    val ruleLabel: String = "Low"
): Serializable