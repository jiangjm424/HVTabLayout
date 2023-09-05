package jm.droid.sample.hvtablayout.ui.dashboard

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import androidx.viewpager2.widget.ViewPager2
import jm.droid.lib.tab.ITabLayout
import jm.droid.lib.tab.TabIndicator

class Vp2VMediator(tabLayout: ITabLayout, viewPager: ViewPager2) : Vp2TabMediator(
    tabLayout,
    viewPager
) {
    private val paint = Paint().apply {
        color = Color.BLUE
    }

    override fun interrupt(position: Int): Boolean {
        return false;//position % 3 == 0
    }
    override fun onCreateTabIndicator(context: Context): TabIndicator? {
        return TabIndicator { canvas, element ->
            val startTab = element.start
            val endTab = element.end
            val fraction = element.fraction
            Log.i("jiang-draw", "v start:${startTab?.position}, t:${startTab?.itemView?.top}, b:${startTab?.itemView?.bottom}")
            Log.i("jiang-draw", "v end:${endTab?.position}, t:${endTab?.itemView?.top}, b:${endTab?.itemView?.bottom}")
            if (startTab != null && endTab != null) {
                val start =
                    lerp(startTab.itemView.top, endTab.itemView.top, fraction)
                val end =
                    lerp(startTab.itemView.bottom, endTab.itemView.bottom, fraction)
                var b: Int = startTab.itemView.right
                val top: Int = startTab.itemView.left
                b = top + 104
                canvas.drawRect(top.toFloat(),start.toFloat(),  b.toFloat(),end.toFloat(),  paint)

            }
        }
    }
}
