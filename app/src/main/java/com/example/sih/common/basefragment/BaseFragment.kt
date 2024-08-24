package com.example.sih.common.basefragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.example.sih.R


abstract class BaseFragment<T: ViewBinding>(private val bindingInflater: (layoutInflater: LayoutInflater) ->T) : Fragment() {

    private var _binding :T? =null
    protected val binding: T get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding=bindingInflater.invoke(inflater)
        return binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }
}