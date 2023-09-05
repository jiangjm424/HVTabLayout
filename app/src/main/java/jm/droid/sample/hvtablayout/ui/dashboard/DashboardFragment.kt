/*
 * Copyright 2023 The Jmdroid Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jm.droid.sample.hvtablayout.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.tabs.TabLayoutMediator
import jm.droid.sample.hvtablayout.databinding.FragmentDashboardBinding

val tabSize = 40
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textDashboard
        dashboardViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        val tab2 = DefaultTab2()
        binding.pre.setOnClickListener {
            if (index == 0) return@setOnClickListener
            index --
            binding.tab2.selectTab(index)
            tab2.remove(index)
        }
        binding.first.setOnClickListener {
            index = 0
            binding.tab2.selectTab(index)
            tab2.remove(index)

        }
        binding.next.setOnClickListener {
            index ++
            binding.tab2.selectTab(index)
        }
        VpMediator(binding.vTab, binding.vp).bind()
        VpMediator(binding.tab2, binding.vp).bind()
        binding.vp2.adapter = Vp2FragmentAdapter(this)
        binding.vp.adapter = VpFragmentAdapter(childFragmentManager)
//        TabLayoutMediator(binding.mattab, binding.vp2) {
//            tab, pos -> tab.text = "place:$pos"
//        }.attach()



        binding.vp.setCurrentItem(30, false)
//        binding.mattab.setupWithViewPager(binding.vp)
        binding.vp2.setCurrentItem(30,false)
        binding.vp2.registerOnPageChangeCallback(object : OnPageChangeCallback(){
            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
            }
        })
//        binding.vp2.isUserInputEnabled = false
//        binding.tab2.setAdapter(Vp2TabMediator(binding.tab2, binding.vp2).attach())
//        Vp2VMediator(binding.tab2, binding.vp).attach()
//        binding.vTab.setAdapter(Vp2VMediator(binding.vTab, binding.vp2).attach())
//        binding.mattab.˛
        return root
    }

    var index = 0
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
