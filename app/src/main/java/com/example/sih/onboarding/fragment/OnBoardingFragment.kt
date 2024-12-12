package com.example.sih.onboarding.fragment

import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.sih.R
import com.example.sih.common.basefragment.BaseFragment
import com.example.sih.databinding.FragmentAdminControlBinding
import com.example.sih.databinding.FragmentOnBoardingBinding
import com.example.sih.model.OnBoardingItems
import com.example.sih.onboarding.onBoardingAdapter.ViewPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator

class OnBoardingFragment : BaseFragment<FragmentOnBoardingBinding>(FragmentOnBoardingBinding::inflate) {

    private lateinit var viewPager: ViewPager2
    private lateinit var skipButton: TextView
    private lateinit var viewPagerAdapter: ViewPagerAdapter

    private var currentItem = 0
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().findViewById<View>(R.id.bottom_navigation).visibility = View.GONE

        viewPager = binding.viewPager
        skipButton = binding.skipButton

        val pageContents = listOf(
            OnBoardingItems("Clear Air, Clear Mind: Know Your AQI!", R.drawable.aqi_intro_img),
            OnBoardingItems("Fresh Air Starts with Awareness!", R.drawable.air_ware_img),
        )

        viewPagerAdapter = ViewPagerAdapter(requireContext(), pageContents)
        viewPager.adapter = viewPagerAdapter

        skipButton.setOnClickListener {
            findNavController().navigate(R.id.action_onBoardingFragment_to_homeFragment)
        }

        for (i in 0 until binding.tablayout.getTabCount()) {
            val tab = (binding.tablayout.getChildAt(0) as ViewGroup).getChildAt(i)
            val p = tab.layoutParams as MarginLayoutParams
            p.setMargins(0, 0, 50, 0)
            tab.requestLayout()
        }
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentItem = position
                if(position==pageContents.size-1){
                    binding.skipButton.text = getString(R.string.continue_str)
                }else{
                    binding.skipButton.text =getString(R.string.skip)
                }
            }
        })

        TabLayoutMediator(binding.tablayout, viewPager) { tab, position ->

        }.attach()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        // Show BottomNavigationView when leaving onboarding
        requireActivity().findViewById<View>(R.id.bottom_navigation).visibility = View.VISIBLE
    }
}