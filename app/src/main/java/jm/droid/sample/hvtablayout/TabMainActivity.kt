package jm.droid.sample.hvtablayout

import android.os.Bundle
import android.util.Log
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import jm.droid.sample.hvtablayout.ui.main.SectionsPagerAdapter
import jm.droid.sample.hvtablayout.databinding.ActivityTabMainBinding

class TabMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTabMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTabMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter
        viewPager.currentItem = 30
        val tabs: TabLayout = binding.tabs
        tabs.setupWithViewPager(viewPager)
        tabs.getTabAt(0)?.view?.setOnClickListener {
            Log.i("jiang","did")
        }
//        val bb = binding.hTab
//        bb.setAdapter(VpTabMediator(binding.hTab, binding.viewPager).attach())
        val fab: FloatingActionButton = binding.fab

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
            viewPager.adapter = null
        }
    }
}
