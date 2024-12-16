package com.example.sih.presentation.broadcast.fragment.news

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.sih.R
import com.example.sih.common.basefragment.BaseFragment
import com.example.sih.databinding.FragmentBlogPostBinding
import com.example.sih.databinding.FragmentMapBinding
import java.util.UUID
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController


class BlogPostFragment: BaseFragment<FragmentBlogPostBinding>(FragmentBlogPostBinding::inflate) {

    private lateinit var _binding: FragmentBlogPostBinding
    private val REQUEST_CODE_IMAGE_PICKER = 1001

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBlogPostBinding.bind(view)

        _binding.addImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE_IMAGE_PICKER)
        }

        _binding.compileButton.setOnClickListener {
            val markdownContent = _binding.markdownEditor.text.toString()

            // Navigate to CompilerFragment with NavController
            val navController = findNavController()
            /*
            val action = CurrentFragmentDirections.actionCurrentFragmentToCompilerFragment(markdownContent)

            navController.navigate(action)

             */

        }


    }
}