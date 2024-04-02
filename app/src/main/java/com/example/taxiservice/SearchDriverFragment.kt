package com.example.taxiservice

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar

class SearchDriverFragment : Fragment() {
    lateinit var cancelButton: Button
    lateinit var progressBar : ProgressBar



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
                activity?.let{
                    val intent = it.intent
                    val uid = arguments?.getString("UID")
                    it.finish()
                    intent.putExtra("RESTART_INTENT", true)
                    intent.putExtra("UID", uid)
                    startActivity(intent)
                    it.overridePendingTransition(0,0)
                }

        }
    }

        override fun onActivityCreated(savedInstanceState: Bundle?) {
            super.onActivityCreated(savedInstanceState)
            viewModel = ViewModelProvider(this).get(SearchDriverViewModel::class.java)
            // TODO: Use the ViewModel
        }

    }