package com.example.sih.onboarding.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.sih.R
import com.example.sih.common.basefragment.BaseFragment
import com.example.sih.databinding.FragmentOnBoardingBinding
import com.example.sih.databinding.FragmentSelectorBinding

class SelectorFragment : BaseFragment<FragmentSelectorBinding>(FragmentSelectorBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.admin.setOnClickListener {
            findNavController().navigate(R.id.action_selectorFragment_to_adminControlFragment)
        }
        binding.users.setOnClickListener {
            findNavController().navigate(R.id.action_selectorFragment_to_userFragment)
        }

    }


}