package my.n.ubid

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

class NetworkGraphView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var centralBuid: String = ""
    private var aiSuggestions: List<AiMatch> = emptyList()

    // --- NEW: Touch Detection Variables ---
    var onNodeClicked: ((AiMatch) -> Unit)? = null
    private val nodePositions = mutableListOf<Pair<AiMatch, NodeBounds>>()
    private data class NodeBounds(val x: Float, val y: Float, val radius: Float)

    private val centralNodePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4db8ff") // Active Brand Blue
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 30f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val subTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4d6a85")
        textSize = 22f
        textAlign = Paint.Align.CENTER
    }

    fun updateGraph(buid: String, suggestions: List<AiMatch>) {
        this.centralBuid = buid
        this.aiSuggestions = suggestions
        invalidate() // Trigger redraw
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (centralBuid.isEmpty()) return

        nodePositions.clear() // NEW: Clear old positions on redraw

        val cx = width / 2f
        val cy = height / 2f
        val maxDist = min(width, height) / 2.2f
        val minDist = 200f
        val nodeRadius = 55f

        aiSuggestions.forEachIndexed { i, match ->
            val angle = Math.toRadians((i * (360f / aiSuggestions.size)).toDouble())

            // Score determines physical distance
            val score = match.Similarity_Score?.toFloat() ?: 0f
            val dynamicRadius = maxDist - ((score / 100f) * (maxDist - minDist))

            val nodeX = (cx + dynamicRadius * cos(angle)).toFloat()
            val nodeY = (cy + dynamicRadius * sin(angle)).toFloat()

            // NEW: Save position for click detection later
            nodePositions.add(match to NodeBounds(nodeX, nodeY, nodeRadius))

            // Draw Connection Line
            val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#2e5a82")
                strokeWidth = 2f + (score / 20f)
                style = Paint.Style.STROKE
            }
            canvas.drawLine(cx, cy, nodeX, nodeY, linePaint)

            // Determine Node Color from AI Label
            val nodeColor = when (match.Rule_Based_Label?.lowercase()) {
                "high", "high risk" -> "#ff4d4d"
                "medium", "review" -> "#f9a825"
                else -> "#3fcf8e"
            }

            val nodePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor(nodeColor)
                style = Paint.Style.FILL
            }

            canvas.drawCircle(nodeX, nodeY, nodeRadius, nodePaint)

            val rawName = match.name ?: "Unknown"
            val shortName = if (rawName.length > 10) rawName.take(8) + ".." else rawName
            canvas.drawText(shortName, nodeX, nodeY - nodeRadius - 10, textPaint)
            canvas.drawText("${score.toInt()}%", nodeX, nodeY + nodeRadius + 30, subTextPaint)
        }

        // Draw Center Node
        canvas.drawCircle(cx, cy, nodeRadius + 20f, centralNodePaint)
        canvas.drawText(centralBuid, cx, cy + 10f, textPaint)
    }

    // --- NEW: Detect User Taps ---
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val tx = event.x
            val ty = event.y

            // Math to figure out if the tap happened inside any of the circles
            for (pos in nodePositions) {
                val dx = tx - pos.second.x
                val dy = ty - pos.second.y
                val distance = sqrt(dx * dx + dy * dy)

                // Added 1.5x padding to radius to make it easier for thick fingers to tap!
                if (distance <= pos.second.radius * 1.5f) {
                    onNodeClicked?.invoke(pos.first)
                    return true // Tell Android we handled this touch
                }
            }
        }
        return super.onTouchEvent(event)
    }
}