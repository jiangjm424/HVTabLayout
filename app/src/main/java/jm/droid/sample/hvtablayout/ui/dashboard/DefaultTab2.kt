package jm.droid.sample.hvtablayout.ui.dashboard

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import jm.droid.lib.tab.TabAdapter
import jm.droid.lib.tab.TabHolder
import jm.droid.lib.tab.TabIndicator
import jm.droid.sample.hvtablayout.databinding.TabHBinding
import kotlin.math.roundToInt

class DefaultTab2 : TabAdapter<DefaultTab2.VH2>() {
    class VH2(val bind: TabHBinding) : TabHolder(bind.root)

    private fun lerp(startValue: Int, endValue: Int, fraction: Float): Int {
        return startValue + (fraction * (endValue - startValue)).roundToInt()
    }

    private val datas = mutableListOf(
        "fasfa1",
        "fasfa2",
        "fasfa3",
        "fasfa4",
        "fasfa5",
        "fasfa6",
        "fasfa7",
        "fasfa8",
        "fasfa9",
        "fasfa10",
        "fasfa11",
        "fasfa12",
        "fasfa13",
        "fasfa14",
        "fasfa15",
        "fasfa16",
        "fasfa17",
        "fasfa18",
        "fasfa19",
    )
    fun remove(i:Int) {
        datas.removeAt(i)
        notifyTabRemoved(i)
    }
    override fun onCreateTabHolder(parent: ViewGroup): VH2 {
        val bind = TabHBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH2(bind)
    }

    private val paint = Paint().apply {
        color = Color.RED
    }

    override fun onCreateTabIndicator(context: Context?): TabIndicator {
        return TabIndicator { canvas, element ->
            val startTab = element.start
            val endTab = element.end
            val fraction = element.fraction
            Log.i("jiang-draw","start:$startTab")
            Log.i("jiang-draw","end:$endTab")
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
                    lerp(startTab.itemView.getLeft(), startTab.itemView.getLeft(), fraction) -20
                val end =
                    lerp(startTab.itemView.getRight(), startTab.itemView.getRight(), fraction) +30
                var b: Int = startTab.itemView.getBottom()
                val top: Int = startTab.itemView.getTop()
                b = top + 10
                canvas.drawRect(start.toFloat(), top.toFloat(), end.toFloat(), b.toFloat(), paint)

            }
        }
    }

    override fun getTabCount(): Int {
        return datas.size
    }

    override fun onTabActivatedChanged(tab: TabHolder) {
        Log.i("jiang","tab activity: $tab, ${tab.isActivated}, pos:${tab.position}")
    }
    override fun onBindTabHolder(holder: VH2, position: Int) {
//        if (position % 2 == 0) {
//
//            holder.bind.tab.text = "${datas.get(position)} ----adfadsfasfasdfas"
//        } else {
//        }
        holder.bind.tab.text = datas[position]
    }
}
