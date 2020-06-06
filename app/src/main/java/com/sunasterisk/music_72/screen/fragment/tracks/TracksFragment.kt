package com.sunasterisk.music_72.screen.fragment.tracks

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sunasterisk.music_72.R
import com.sunasterisk.music_72.data.anotation.GenreName
import com.sunasterisk.music_72.data.anotation.Type
import com.sunasterisk.music_72.data.model.Track
import com.sunasterisk.music_72.data.source.local.TrackLocalDataSource
import com.sunasterisk.music_72.data.source.remote.TrackRemoteDataSource
import com.sunasterisk.music_72.data.source.remote.connection.RetrofitClient
import com.sunasterisk.music_72.data.source.repository.TrackRepositoryImplementor
import com.sunasterisk.music_72.databinding.FragmentTracksBinding
import com.sunasterisk.music_72.screen.MainActivity
import com.sunasterisk.music_72.screen.adapter.TrackAdapter
import com.sunasterisk.music_72.screen.factory.ViewModelFactory
import com.sunasterisk.music_72.screen.fragment.playtrack.PlayTrackFragment
import com.sunasterisk.music_72.screen.service.DownloadTrackService
import com.sunasterisk.music_72.screen.service.PlayTrackService
import com.sunasterisk.music_72.utils.MusicUtils
import com.sunasterisk.music_72.utils.listener.OnDownloadItemListener
import com.sunasterisk.music_72.utils.listener.OnRecyclerViewItemListener
import com.sunasterisk.music_72.utils.replaceFragmentToActivity
import com.sunasterisk.music_72.utils.setupToolbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_tracks.*
import java.util.*

class TracksFragment : Fragment(), View.OnClickListener,
    OnRecyclerViewItemListener<Track>,
    OnDownloadItemListener {
    private lateinit var binding: FragmentTracksBinding
    private lateinit var service: PlayTrackService
    private val adapter: TrackAdapter by lazy { TrackAdapter() }
    private var genreName: String? = null
    private var limitItem = 0
    private var isLoading = false
    private val tracks = mutableListOf<Track?>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_tracks,
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
        initData()
    }

    private fun initView() {
        (activity as? AppCompatActivity)?.run {
            setupToolbar(toolBarTracks)
            containerControl.visibility = View.GONE
            navigationBottomHome?.visibility = View.GONE
            service.getCurrentTrack()?.let {
                containerControl.visibility = View.VISIBLE
            }
        }
        progressLoading.visibility = View.VISIBLE
        buttonBack.setOnClickListener(this)
        adapter.setOnItemClickListener(this, this)
        recyclerViewTracks.adapter = adapter
    }

    private fun initData() {
        service.getCurrentTrack()?.let { adapter.setCurrentTrack(it) }
        genreName = arguments?.getString(ARGUMENT_GENRE_KEY, GenreName.ALL_TRACK)
        genreName?.let { it ->
            textTitleToolBar.text = genreName
            MusicUtils.getGenreImage(it)?.let {
                imageGenre.setImageResource(it)
            }
            binding.viewModel?.getTracks(it, limitItem)
        }
        bindDataToView()
    }

    private fun bindDataToView() {
        binding.viewModel?.tracks?.observe(this, Observer {
            progressLoading.visibility = View.GONE
            tracks.clear()
            tracks.addAll(it)
            textTotalTrack.text = it.size.toString()
            textDescriptionTotalTrack.text =
                binding.viewModel?.getDescriptionTotalTrack(it.size)
            adapter.submitList(it)
            initScrollListener()
        })
        binding.viewModel?.error?.observe(this, Observer {
            progressLoading.visibility = View.GONE
        })
    }

    private fun createViewModel() {
        activity?.let {
            binding.viewModel =
                ViewModelProviders.of(
                    it,
                    ViewModelFactory{
                        TracksViewModel(
                            TrackRepositoryImplementor(
                                TrackLocalDataSource(),
                                TrackRemoteDataSource(RetrofitClient())
                            )
                        )
                    }
                ).get(TracksViewModel::class.java)
        }
    }

    override fun onClick(v: View?) {
        (activity as? AppCompatActivity)?.apply {
            supportFragmentManager.apply {
                popBackStack()
            }
            navigationBottomHome.visibility = View.VISIBLE
        }
    }

    override fun onItemClick(data: Track) {
        (activity as AppCompatActivity).apply {
            replaceFragmentToActivity(
                supportFragmentManager,
                PlayTrackFragment.newInstance(data, Type.TRACKS),
                R.id.container
            )
        }
    }

    private fun initScrollListener() {
        recyclerViewTracks.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
        if ((recyclerViewTracks.layoutManager as LinearLayoutManager).itemCount < 50) {
            progressLoading.visibility = View.VISIBLE
            limitItem += 10
            Handler().postDelayed({
                genreName?.let { binding.viewModel?.getTracks(it, limitItem) }
                isLoading = false
            }, 3000)
        }
    }

    override fun onItemDownloadClick(data: Track) {
        binding.viewModel?.getAudios()
        binding.viewModel?.audios?.observe(this, Observer {
            it.indexOf(data)
            loop@for(position in it.indices) {
                if (data.id == it[position].id) {
                    Toast.makeText(context, context?.getString(R.string.message_downloaded), Toast.LENGTH_SHORT).show()
                }
                if (position == it.size - 1) {
                    activity?.startService(DownloadTrackService.getIntent(context, data))
                }
            }
        })
    }

    companion object {
        private const val ARGUMENT_GENRE_KEY = "ARGUMENT_GENRE_KEY"

        fun newInstance(genre: String) =
            TracksFragment().apply {
                arguments = bundleOf(ARGUMENT_GENRE_KEY to genre)
            }
    }
}
