package my.n.ubid

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query


data class AiMatch(
    val BUID: String?,
    val name: String?,

    @SerializedName("homepage_url")
    val website: String?,

    val market: String?,

    @SerializedName("funding_total_usd")
    val fundingTotalUsd: Long?, // Note: It's a Long (number), not a String!

    val status: String?,
    val region: String?,
    val city: String?,

    @SerializedName("category_list")
    val categoryList: String?,

    @SerializedName("Similarity_Rank")
    val rank: Int?,

    val Similarity_Score: Double?,
    val Rule_Based_Label: String?

)

// 2. Tell Android the AI returns a list of these businesses
data class AiResponse(
    val recommendations: List<AiMatch> // Now uses the updated AiMatch class
)

interface AiApiService {
    @GET("recommend")
    suspend fun getRecommendations(
        @Query("buid") buid: String,
        @Query("top_n") topN: Int
    ): AiResponse
}