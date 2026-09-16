package com.probuilder.scrollcount.ui.share

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.probuilder.scrollcount.util.Formatting
import kotlin.math.roundToInt

/** Everything the share card needs to draw itself. */
data class ShareCardData(
    val weeklyTotal: Int,
    val dailyAverage: Int,
    val estimatedSeconds: Int,
    /** Seven (label, count) pairs, oldest first, for the mini chart. */
    val dailyTotals: List<Pair<String, Int>>,
)

/**
 * Draws the story-sized image people post to show off (or confess) their week.
 *
 * This uses plain Android Canvas rather than rendering a Compose layout to a
 * bitmap. Compose needs a real window to measure itself in, which makes
 * off-screen capture fiddly and flaky; drawing 1080x1920 directly always
 * produces the exact same image on every phone.
 */
object ShareCardRenderer {

    const val WIDTH = 1080
    const val HEIGHT = 1920

    private const val BACKGROUND_TOP = 0xFF2B1B63.toInt()
    private const val BACKGROUND_BOTTOM = 0xFF120A2A.toInt()
    private const val ACCENT = 0xFFC7BCFF.toInt()
    private const val MUTED = 0xFF9C93C4.toInt()
    private const val TRACK = 0x33FFFFFF
    private const val SIDE_MARGIN = 110f

    fun render(data: ShareCardData): Bitmap {
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        drawBackground(canvas)

        // Wordmark
        canvas.drawText(
            "SCROLLCOUNT",
            WIDTH / 2f,
            220f,
            paint(size = 44f, color = MUTED, bold = true, letterSpacing = 0.25f),
        )

        // Headline number
        canvas.drawText(
            data.weeklyTotal.toString(),
            WIDTH / 2f,
            660f,
            paint(size = 340f, color = Color.WHITE, bold = true),
        )
        canvas.drawText(
            "reels this week",
            WIDTH / 2f,
            760f,
            paint(size = 64f, color = ACCENT),
        )

        // Estimated time
        canvas.drawText(
            "about " + Formatting.duration(data.estimatedSeconds) + " of scrolling",
            WIDTH / 2f,
            890f,
            paint(size = 52f, color = MUTED),
        )

        drawMiniChart(canvas, data.dailyTotals, top = 1010f, height = 360f)

        // Fun line, wrapped by hand so it never runs off the edge of the card.
        val lines = wrap(funLine(data.estimatedSeconds), maxCharsPerLine = 28)
        var y = 1540f
        lines.forEach { line ->
            canvas.drawText(line, WIDTH / 2f, y, paint(size = 56f, color = Color.WHITE))
            y += 74f
        }

        // Footer
        drawChevron(canvas, centerX = WIDTH / 2f, centerY = 1740f)
        canvas.drawText(
            "counted with ScrollCount",
            WIDTH / 2f,
            1850f,
            paint(size = 40f, color = MUTED),
        )

        return bitmap
    }

    private fun drawBackground(canvas: Canvas) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f,
                0f,
                0f,
                HEIGHT.toFloat(),
                BACKGROUND_TOP,
                BACKGROUND_BOTTOM,
                Shader.TileMode.CLAMP,
            )
        }
        canvas.drawRect(0f, 0f, WIDTH.toFloat(), HEIGHT.toFloat(), paint)
    }

    /** Seven slim bars, with the tallest scaled to the full height available. */
    private fun drawMiniChart(
        canvas: Canvas,
        totals: List<Pair<String, Int>>,
        top: Float,
        height: Float,
    ) {
        if (totals.isEmpty()) return
        val maxValue = totals.maxOf { it.second }.coerceAtLeast(1)
        val slotWidth = (WIDTH - 2 * SIDE_MARGIN) / totals.size
        val barWidth = slotWidth * 0.46f
        val baseline = top + height

        val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ACCENT }
        val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = TRACK }
        val labelPaint = paint(size = 34f, color = MUTED)

        totals.forEachIndexed { index, entry ->
            val label = entry.first
            val value = entry.second
            val centerX = SIDE_MARGIN + slotWidth * index + slotWidth / 2f
            val left = centerX - barWidth / 2f
            val right = centerX + barWidth / 2f

            canvas.drawRoundRect(
                RectF(left, top, right, baseline),
                barWidth / 2f,
                barWidth / 2f,
                trackPaint,
            )
            val minimum = if (value > 0) 14f else 8f
            val barHeight = (height * value / maxValue).coerceAtLeast(minimum)
            canvas.drawRoundRect(
                RectF(left, baseline - barHeight, right, baseline),
                barWidth / 2f,
                barWidth / 2f,
                barPaint,
            )
            canvas.drawText(label, centerX, baseline + 56f, labelPaint)
        }
    }

    /** The two-chevron placeholder logo, drawn to match the app icon. */
    private fun drawChevron(canvas: Canvas, centerX: Float, centerY: Float) {
        val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 12f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            color = Color.WHITE
        }
        val width = 52f
        val drop = 24f
        canvas.drawLines(
            floatArrayOf(
                centerX - width, centerY - drop, centerX, centerY,
                centerX, centerY, centerX + width, centerY - drop,
            ),
            stroke,
        )
        stroke.color = ACCENT
        canvas.drawLines(
            floatArrayOf(
                centerX - width, centerY + 10f, centerX, centerY + 34f,
                centerX, centerY + 34f, centerX + width, centerY + 10f,
            ),
            stroke,
        )
    }

    /** The line that makes the number mean something. */
    fun funLine(estimatedSeconds: Int): String {
        val hours = estimatedSeconds / 3600.0
        return when {
            hours >= 2.0 -> {
                val movies = (hours / 2.0).roundToInt().coerceAtLeast(1)
                if (movies == 1) {
                    "That is about one whole movie."
                } else {
                    "That is about " + movies + " movies worth of scrolling."
                }
            }

            estimatedSeconds >= 600 -> "That is about " + (estimatedSeconds / 60) + " minutes of scrolling."
            estimatedSeconds > 0 -> "A quiet week. Barely a scroll."
            else -> "Nothing scrolled. Nothing lost."
        }
    }

    /** Greedy word wrap - enough for the one sentence this card shows. */
    private fun wrap(text: String, maxCharsPerLine: Int): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var current = StringBuilder()
        words.forEach { word ->
            if (current.isEmpty()) {
                current.append(word)
            } else if (current.length + 1 + word.length <= maxCharsPerLine) {
                current.append(" ").append(word)
            } else {
                lines.add(current.toString())
                current = StringBuilder(word)
            }
        }
        if (current.isNotEmpty()) lines.add(current.toString())
        return lines
    }

    private fun paint(
        size: Float,
        color: Int,
        bold: Boolean = false,
        letterSpacing: Float = 0f,
    ): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        textSize = size
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, if (bold) Typeface.BOLD else Typeface.NORMAL)
        this.letterSpacing = letterSpacing
    }
}
