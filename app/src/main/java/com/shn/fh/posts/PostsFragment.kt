package com.shn.fh.posts


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.shn.fh.R
import com.shn.fh.databinding.LayoutPostsBinding

// Here ":" symbol is indicate that LoginFragment
// is child class of Fragment Class
class PostsFragment : Fragment() {
    lateinit var binding: LayoutPostsBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = LayoutPostsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}