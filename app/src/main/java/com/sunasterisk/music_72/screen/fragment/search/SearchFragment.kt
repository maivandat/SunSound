package com.sunasterisk.music_72.screen.fragment.search

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sunasterisk.music_72.R
import com.sunasterisk.music_72.data.anotation.Type
import com.sunasterisk.music_72.data.model.Track
import com.sunasterisk.music_72.data.source.local.TrackLocalDataSource
import com.sunasterisk.music_72.data.source.remote.TrackRemoteDataSource
import com.sunasterisk.music_72.data.source.remote.connection.RetrofitClient
import com.sunasterisk.music_72.data.source.repository.TrackRepositoryImplementor
import com.sunasterisk.music_72.databinding.FragmentSearchBinding
import com.sunasterisk.music_72.screen.MainActivity
import com.sunasterisk.music_72.screen.adapter.TrackAdapter
import com.sunasterisk.music_72.screen.factory.ViewModelFactory
import com.sunasterisk.music_72.screen.fragment.playtrack.PlayTrackFragment
import com.sunasterisk.music_72.screen.service.DownloadTrackService
import com.sunasterisk.music_72.screen.service.PlayTrackService
import com.sunasterisk.music_72.utils.Constants.LIMIT_ITEM
import com.sunasterisk.music_72.utils.listener.OnDownloadItemListener
import com.sunasterisk.music_72.utils.listener.OnRecyclerViewItemListener
import com.sunasterisk.music_72.utils.replaceFragmentToActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_search.*
import java.util.*

class SearchFragment : Fragment(), SearchView.OnQueryTextListener,
    OnRecyclerViewItemListener<Track>, OnDownloadItemListener {
    private lateinit var binding: FragmentSearchBinding
    private lateinit var service: PlayTrackService
    private val adapter: TrackAdapter by lazy { TrackAdapter() }
    private val tracks = mutableListOf<Track?>()
    private var query: String? = null
    private var limitItem = 0
    private var isLoading = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_search,
                container, false)
        createViewModel()
        (activity as? MainActivity)?.let { main ->
            main.service?.let { service ->
                this.service = Objects.requireNonNull(service)
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        searchTrack.setOnQueryTextListener(this)
        adapter.setOnItemClickListener(this, this)
        (activity as? AppCompatActivity)?.apply {
            navigationBottomHome.visibility = View.VISIBLE
            containerControl.visibility = View.GONE
            service.getCurrentTrack()?.let {
                adapter.setCurrentTrack(it)
                containerControl.visibility = View.VISIBLE
            }
        }
        recyclerViewSearchResult.adapter = adapter
        bindDataToView()
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        this.query = query
        query?.let { binding.viewModel?.getTracksSearchResult(it, LIMIT_ITEM) }
        progressLoadingHead.visibility = View.VISIBLE
        return true
    }

    private fun bindDataToView() {
        binding.viewModel?.tracks?.observe(this, Observer {
            progressLoadingHead.visibility = View.GONE
            tracks.clear()
            tracks.addAll(it)
            adapter.submitList(it)
            initScrollListener()
        })
        binding.viewModel?.error?.observe(this, Observer {
            Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
        })
    }

    private fun initScrollListener() {
        recyclerViewSearchResult.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
                if (!isLoading) {
                    if (linearLayoutManager.findLastVisibleItemPosition() == tracks.size - 1) {
                        loadMore()
                        isLoading = true
                    }
                }
            }
        })
    }

    private fun loadMore() {
        if ((recyclerViewSearchResult.layoutManager as LinearLayoutManager).itemCount < 50) {
            progressLoadingEnd?.let { it.visibility = View.VISIBLE }
            limitItem += 10
            Handler().postDelayed({
                query?.let { binding.viewModel?.getTracksSearchResult(it, limitItem) }
                progressLoadingEnd?.let { it.visibility = View.GONE }
                isLoading = false
            }, 3000)
        }
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        // Do Nothing
        return true
    }

    private fun createViewModel() {
        activity?.let {
            binding.viewModel =
                ViewModelProviders.of(
                    it,
                    ViewModelFactory{
                        SearchViewModel(
                            TrackRepositoryImplementor(
                                TrackLocalDataSource(),
                                TrackRemoteDataSource(RetrofitClient())
                            )
                        )
                    }
                ).get(SearchViewModel::class.java)
        }
    }

    override fun onItemClick(data: Track) {
        (activity as AppCompatActivity).apply {
            navigationBottomHome.visibility = View.GONE
            replaceFragmentToActivity(
                supportFragmentManager,
                PlayTrackFragment.newInstance(data, Type.SEARCH),
                R.id.container
            )
        }
    }

    override fun onItemDownloadClick(data: Track) {
        binding.viewModel?.getAudios()
        binding.viewModel?.audios?.observe(this, Observer {
            if (it.isNotEmpty()) {
                loop@for(position in it.indices) {
                    if (data.id == it[position].id) {
                        Toast.makeText(context, context?.getString(R.string.message_downloaded), Toast.LENGTH_SHORT).show()
                        break@loop
                    }
                    if (position == it.size - 1) {
                        activity?.startService(DownloadTrackService.getIntent(context, data))
                    }
                }
            } else {
                activity?.startService(DownloadTrackService.getIntent(context, data))
            }

        })
    }

    companion object {
        fun newInstance() = SearchFragment()
    }
}
