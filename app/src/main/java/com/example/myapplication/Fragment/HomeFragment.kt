package com.example.myapplication.Fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.Activity.SearchActivity
import com.example.myapplication.Adapter.CategoryAdapter
import com.example.myapplication.Adapter.PopularAdapter
import com.example.myapplication.Adapter.OfferAdapter
import com.example.myapplication.ViewModel.MainViewModel
import com.example.myapplication.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        initCategory()
        initPopular()
        initOffers()
        initBrew()

        binding.search.setOnClickListener {
            startActivity(Intent(requireContext(), SearchActivity::class.java))
        }
    }

    private fun initCategory() {
        binding.progressBarCategory.visibility = View.VISIBLE
        viewModel.category.observe(viewLifecycleOwner) { categoryList ->
            binding.recyclerViewCategory.layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            binding.recyclerViewCategory.adapter = CategoryAdapter(categoryList)
            binding.progressBarCategory.visibility = View.GONE
        }
        viewModel.loadCategory()
    }

    private fun initPopular() {
        binding.progressBarPopular.visibility = View.VISIBLE
        viewModel.popular.observe(viewLifecycleOwner) {
            binding.recyclerViewPopular.layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            binding.recyclerViewPopular.adapter = PopularAdapter(it)
            binding.progressBarPopular.visibility = View.GONE
        }
        viewModel.loadPopular()
    }

    private fun initBrew() {
        binding.progressBarPopular.visibility = View.VISIBLE
        viewModel.brewBased.observe(viewLifecycleOwner) {
            binding.recyclerViewBrewBased.layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            binding.recyclerViewBrewBased.adapter = PopularAdapter(it)
            binding.progressBarBrewBased.visibility = View.GONE
        }
        viewModel.loadbrewBased()
    }

    private fun initOffers() {
        binding.progressBarOffer.visibility = View.VISIBLE
        viewModel.offer.observe(viewLifecycleOwner) {
            binding.recyclerViewOffer.layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            binding.recyclerViewOffer.adapter = OfferAdapter(it)
            binding.progressBarOffer.visibility = View.GONE
        }
        viewModel.loadOffer()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
