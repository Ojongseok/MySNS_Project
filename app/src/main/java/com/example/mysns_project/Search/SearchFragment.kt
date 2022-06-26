package com.example.mysns_project.Alarm

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.example.mysns_project.R
import com.example.mysns_project.Search.SearchRecyclerviewAdapter
import kotlinx.android.synthetic.main.fragment_search.view.*

class SearchFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        view?.search_recyclerview?.adapter = SearchRecyclerviewAdapter(this)
        view?.search_recyclerview?.layoutManager = GridLayoutManager(activity,3)

        return view
    }
}