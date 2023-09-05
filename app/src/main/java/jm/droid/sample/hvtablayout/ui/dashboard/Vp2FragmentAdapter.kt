package jm.droid.sample.hvtablayout.ui.dashboard

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import jm.droid.sample.hvtablayout.ui.main.PlaceholderFragment

class Vp2FragmentAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return tabSize
    }
    fun getItem(position: Int) = "pos:-- $position"

    override fun createFragment(position: Int): Fragment {
        return PlaceholderFragment.newInstance(position)
    }
}
