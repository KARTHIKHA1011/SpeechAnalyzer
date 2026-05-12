package com.serene.mentor.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.serene.mentor.R
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

class WaveformView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.primary)
        strokeWidth = 6f
        strokeCap = Paint.Cap.ROUND
    }

    private val bars = 20
    private val barHeights = FloatArray(bars) { Random.nextFloat() * 0.3f }
    private var amplitude = 0f
    private var animOffset = 0f

    fun setLevel(rmsDb: Float) {
        amplitude = (rmsDb / 10f).coerceIn(0f, 1f)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        val barWidth = w / (bars * 2)
        val centerY = h / 2f

        animOffset += 0.1f

        for (i in 0 until bars) {
            val x = i * (barWidth * 2) + barWidth
            val wave = sin((i + animOffset).toDouble()).toFloat()
            val barH = (abs(wave) * amplitude * h * 0.45f).coerceAtLeast(4f)

            canvas.drawLine(x, centerY - barH, x, centerY + barH, paint)
        }
    }
}
