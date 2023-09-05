package jm.droid.sample.hvtablayout.ui.dashboard

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import jm.droid.lib.tab.ITabLayout
import jm.droid.lib.tab.TabHolder
import jm.droid.lib.tab.TabIndicator
import jm.droid.lib.tab.TabMediatorViewPager
import jm.droid.sample.hvtablayout.databinding.TabHBinding
import kotlin.math.roundToInt

open class VpMediator(tabLayout: ITabLayout, viewPager: ViewPager) :
    TabMediatorViewPager<VpMediator.MatVp>(tabLayout, viewPager, true) {

    private val paint = Paint().apply {
        color = Color.CYAN
    }
    class MatVp(val bind: TabHBinding) : TabHolder(bind.root)

    override fun onCreateTabHolder(parent: ViewGroup): MatVp {
        val bind = TabHBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MatVp(bind)
    }

    override fun onBindTabHolder(holder: MatVp, adapter: PagerAdapter?, position: Int) {
        holder.bind.tab.text = adapter?.getPageTitle(position)
    }
    private fun lerp(startValue: Int, endValue: Int, fraction: Float): Int {
        return startValue + (fraction * (endValue - startValue)).roundToInt()
    }
    override fun onCreateTabIndicator(context: Context?): TabIndicator {
        return TabIndicator { canvas, element ->
            val startTab = element.start
            val endTab = element.end
            val fraction = element.fraction
            Log.i("jiang-mat", "v start:${startTab?.position}, t:${startTab?.itemView?.top}, b:${startTab?.itemView?.bottom}")
            Log.i("jiang-mat", "v end:${endTab?.position}, t:${endTab?.itemView?.top}, b:${endTab?.itemView?.bottom}")
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

    override fun onTabActivatedChanged(tab: TabHolder) {
        Log.i("jianggb","tab:"+tab.isActivated)
    }
}
