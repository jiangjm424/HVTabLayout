package jm.droid.sample.hvtablayout.ui.dashboard

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import jm.droid.sample.hvtablayout.ui.main.PlaceholderFragment

class VpFragmentAdapter(fm: FragmentManager) :
    FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getCount(): Int {
        return tabSize
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return "title:$position"
    }
    override fun getItem(position: Int): Fragment {
        return PlaceholderFragment.newInstance(position)
    }
}
