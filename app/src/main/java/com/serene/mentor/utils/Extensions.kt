package com.serene.mentor.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.serene.mentor.R

// ─── View Extensions ──────────────────────────────────────────

fun View.show() { visibility = View.VISIBLE }
fun View.hide() { visibility = View.GONE }
fun View.invisible() { visibility = View.INVISIBLE }

fun View.isVisible() = visibility == View.VISIBLE

fun View.showWithFade() {
    if (visibility != View.VISIBLE) {
        visibility = View.VISIBLE
        startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_in))
    }
}

fun View.hideWithFade() {
    if (visibility == View.VISIBLE) {
        startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_out))
        visibility = View.GONE
    }
}

fun View.slideInFromRight() {
    visibility = View.VISIBLE
    startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_in_right))
}

fun View.snackbar(message: String, duration: Int = Snackbar.LENGTH_SHORT) {
    Snackbar.make(this, message, duration).show()
}

fun View.snackbarError(message: String) {
    Snackbar.make(this, message, Snackbar.LENGTH_LONG)
        .setBackgroundTint(ContextCompat.getColor(context, R.color.error))
        .setTextColor(ContextCompat.getColor(context, R.color.white))
        .show()
}

fun View.snackbarSuccess(message: String) {
    Snackbar.make(this, message, Snackbar.LENGTH_SHORT)
        .setBackgroundTint(ContextCompat.getColor(context, R.color.score_high))
        .setTextColor(ContextCompat.getColor(context, R.color.white))
        .show()
}

// ─── TextView Extensions ──────────────────────────────────────

fun TextView.setScoreColor(score: Int) {
    val colorRes = when {
        score >= 75 -> R.color.score_high
        score >= 50 -> R.color.score_mid
        else -> R.color.score_low
    }
    setTextColor(ContextCompat.getColor(context, colorRes))
}

// ─── Context Extensions ───────────────────────────────────────

fun Context.isNetworkAvailable(): Boolean {
    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = cm.activeNetwork ?: return false
    val caps = cm.getNetworkCapabilities(network) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}

fun Context.dpToPx(dp: Float): Int {
    return (dp * resources.displayMetrics.density).toInt()
}

// ─── Int / Float Extensions ───────────────────────────────────

fun Int.toScoreLabel(): String = when {
    this >= 85 -> "Excellent"
    this >= 70 -> "Good"
    this >= 55 -> "Average"
    this >= 40 -> "Needs Work"
    else -> "Beginner"
}

fun Int.toScoreEmoji(): String = when {
    this >= 85 -> "🌟"
    this >= 70 -> "✅"
    this >= 55 -> "📈"
    this >= 40 -> "💪"
    else -> "🎯"
}

fun Long.toFormattedDuration(): String {
    val minutes = this / 60
    val seconds = this % 60
    return String.format("%02d:%02d", minutes, seconds)
}

fun Long.toReadableDuration(): String {
    val minutes = this / 60
    val seconds = this % 60
    return when {
        minutes == 0L -> "${seconds}s"
        seconds == 0L -> "${minutes}m"
        else -> "${minutes}m ${seconds}s"
    }
}

// ─── String Extensions ────────────────────────────────────────

fun String.capitalizeWords(): String {
    return split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { it.uppercase() }
    }
}

fun String.toDisplayCategory(): String {
    return replace("_", " ").capitalizeWords()
}

fun String.wordCount(): Int {
    return trim().split("\\s+".toRegex()).filter { it.isNotBlank() }.size
}
