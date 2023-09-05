package jm.droid.sample.hvtablayout

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import jm.droid.lib.tab.TabMediatorViewPager
import jm.droid.lib.tab.HTabLayout
import jm.droid.lib.tab.TabHolder
import jm.droid.lib.tab.TabIndicator
import jm.droid.sample.hvtablayout.databinding.TabHBinding
import kotlin.math.roundToInt


class VpTabMediator(tabLayout: HTabLayout, viewPager: ViewPager) :
    TabMediatorViewPager<VpTabMediator.VHVp>(
        tabLayout,
        viewPager
    ) {
    class VHVp(val bind: TabHBinding) : TabHolder(bind.root)

    private fun lerp(startValue: Int, endValue: Int, fraction: Float): Int {
        return startValue + (fraction * (endValue - startValue)).roundToInt()
    }

    private val paint = Paint().apply {
        color = Color.RED
    }

    override fun onCreateTabHolder(parent: ViewGroup): VHVp {
        val bind = TabHBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VHVp(bind)
    }

    override fun onBindTabHolder(holder: VHVp, adapter: PagerAdapter?, position: Int) {
        holder.bind.tab.text = adapter?.getPageTitle(position)
    }

    override fun onCreateTabIndicator(context: Context): TabIndicator? {
        return TabIndicator { canvas, element ->
            val startTab = element.start
            val endTab = element.end
            val fraction = element.fraction
            Log.i("jiang-draw", "start:$startTab")
            Log.i("jiang-draw", "end:$endTab")
            if (startTab != null && endTab != null) {
                val start =
                    lerp(startTab.itemView.left, endTab.itemView.left, fraction)
                val end =
                    lerp(startTab.itemView.right, endTab.itemView.right, fraction)
                var b: Int = startTab.itemView.bottom
                val top: Int = startTab.itemView.top
                b = top + 10
                canvas.drawRect(start.toFloat(), top.toFloat(), end.toFloat(), b.toFloat(), paint)

            } else if (startTab != null) {
                val start =
                    lerp(startTab.itemView.getLeft(), startTab.itemView.getLeft(), fraction) - 20
                val end =
                    lerp(startTab.itemView.getRight(), startTab.itemView.getRight(), fraction) + 30
                var b: Int = startTab.itemView.getBottom()
                val top: Int = startTab.itemView.getTop()
                b = top + 10
                canvas.drawRect(start.toFloat(), top.toFloat(), end.toFloat(), b.toFloat(), paint)

            }
        }
    }

    override fun onTabActivatedChanged(tab: TabHolder) {
        Log.i("jiang--tabmediator", "tab activity: $tab, ${tab.isActivated}, pos:${tab.position}")
    }
}

