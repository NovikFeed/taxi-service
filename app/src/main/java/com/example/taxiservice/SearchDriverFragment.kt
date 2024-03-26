package com.example.taxiservice

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

class SearchDriverFragment : Fragment() {
    lateinit var cancelButton: Button


    companion object {
        fun newInstance() = SearchDriverFragment()
    }

    private lateinit var viewModel: SearchDriverViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search_driver, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view?.findViewById<Button>(R.id.button4)?.setOnClickListener {
            if(requireActivity().supportFragmentManager.backStackEntryCount > 0){
                requireActivity().supportFragmentManager.popBackStack()

            }
        }
    }

        override fun onActivityCreated(savedInstanceState: Bundle?) {
            super.onActivityCreated(savedInstanceState)
            viewModel = ViewModelProvider(this).get(SearchDriverViewModel::class.java)
            // TODO: Use the ViewModel
        }

    }