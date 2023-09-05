package jm.droid.sample.hvtablayout.ui.dashboard

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import com.google.android.material.tabs.TabLayout

class MTabLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : TabLayout(context, attrs) {
    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        val x = scrollX
        canvas.drawText("scroll:$x", 30f+x,30f, paint)
    }
    val paint = Paint().apply {
        color = Color.RED
        textSize = 34f
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
    }

}
