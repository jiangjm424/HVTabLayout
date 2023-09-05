package jm.droid.sample.hvtablayout.ui.dashboard

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import jm.droid.lib.tab.TabMediatorViewPager2
import jm.droid.lib.tab.HTabLayout
import jm.droid.lib.tab.ITabLayout
import jm.droid.lib.tab.TabHolder
import jm.droid.lib.tab.TabIndicator
import jm.droid.sample.hvtablayout.databinding.TabHBinding
import kotlin.math.roundToInt

open class Vp2TabMediator(tabLayout: ITabLayout, viewPager: ViewPager2) :
    TabMediatorViewPager2<Vp2TabMediator.VHVp2, Vp2FragmentAdapter>(
        tabLayout,
        viewPager
    ) {
    class VHVp2(val bind: TabHBinding) : TabHolder(bind.root)

    protected fun lerp(startValue: Int, endValue: Int, fraction: Float): Int {
        return startValue + (fraction * (endValue - startValue)).roundToInt()
    }

    private val paint = Paint().apply {
        color = Color.RED
    }

    override fun onCreateTabHolder(parent: ViewGroup): VHVp2 {
        val bind = TabHBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VHVp2(bind)
    }

    override fun onBindTabHolder(holder: VHVp2, adapter: Vp2FragmentAdapter, position: Int) {
        holder.bind.tab.text = adapter.getItem(position)
    }

    override fun onCreateTabIndicator(context: Context): TabIndicator? {
        return TabIndicator { canvas, element ->
            val startTab = element.start
            val endTab = element.end
            val fraction = element.fraction
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
    }
}
