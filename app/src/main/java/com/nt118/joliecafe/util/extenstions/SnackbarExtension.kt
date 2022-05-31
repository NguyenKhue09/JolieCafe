package com.nt118.joliecafe.util.extenstions

import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.widget.TextView
import androidx.annotation.ColorInt
import com.google.android.material.snackbar.Snackbar

fun Snackbar.setIcon(drawable: Drawable, @ColorInt colorTint: Int, iconPadding: Int): Snackbar {
    return this.apply {
        val textView = view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)

        drawable.setTint(colorTint)
        drawable.setTintMode(PorterDuff.Mode.SRC_ATOP)
        textView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
        textView.compoundDrawablePadding = iconPadding
        textView.textSize = 16f
    }
}

fun Snackbar.setCustomBackground(drawable: Drawable): Snackbar {
    return this.apply {
        val snackBarView = this.view
        snackBarView.background = drawable
    }
}
