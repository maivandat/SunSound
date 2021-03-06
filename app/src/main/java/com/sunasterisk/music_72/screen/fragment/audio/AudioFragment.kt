package com.sunasterisk.music_72.screen.fragment.audio

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.sunasterisk.music_72.R
import com.sunasterisk.music_72.data.anotation.Type
import com.sunasterisk.music_72.data.model.Track
import com.sunasterisk.music_72.data.source.local.TrackLocalDataSource
import com.sunasterisk.music_72.data.source.remote.TrackRemoteDataSource
import com.sunasterisk.music_72.data.source.remote.connection.RetrofitClient
import com.sunasterisk.music_72.data.source.repository.TrackRepositoryImplementor
import com.sunasterisk.music_72.databinding.FragmentAudioBinding
import com.sunasterisk.music_72.screen.MainActivity
import com.sunasterisk.music_72.screen.adapter.TrackAdapter
import com.sunasterisk.music_72.screen.factory.ViewModelFactory
import com.sunasterisk.music_72.screen.fragment.playtrack.PlayTrackFragment
import com.sunasterisk.music_72.screen.service.PlayTrackService
import com.sunasterisk.music_72.utils.listener.OnRecyclerViewItemListener
import com.sunasterisk.music_72.utils.replaceFragmentToActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_audio.*
import java.util.*

class AudioFragment : Fragment(),
    OnRecyclerViewItemListener<Track> {
    private lateinit var binding: FragmentAudioBinding
    private lateinit var service: PlayTrackService
    private val adapter: TrackAdapter by lazy { TrackAdapter() }
    private val audios = mutableListOf<Track>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_audio, container, false)
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
        (activity as? AppCompatActivity)?.apply {
            navigationBottomHome.visibility = View.VISIBLE
            containerControl.visibility = View.GONE
            service.getCurrentTrack()?.let {
                adapter.setCurrentTrack(it)
                containerControl.visibility = View.VISIBLE
            }
        }
        adapter.setOnItemClickListener(this, null)
        recyclerViewAudios.adapter = adapter
    }

    private fun initData() {
        binding.viewModel?.getAudios()
        binding.viewModel?.audios?.observe(this, Observer {
            adapter.submitList(it)
            audios.addAll(it)
        })
        binding.viewModel?.error?.observe(this, Observer {
            Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
        })
    }

    private fun createViewModel() {
        activity?.let {
            binding.viewModel =
                ViewModelProviders.of(
                    it,
                    ViewModelFactory{
                        AudioViewModel(
                            TrackRepositoryImplementor(
                                TrackLocalDataSource(), TrackRemoteDataSource(RetrofitClient())
                            )
                        )
                    }
                ).get(AudioViewModel::class.java)
        }
    }

    override fun onItemClick(data: Track) {
        (activity as AppCompatActivity).apply {
            navigationBottomHome.visibility = View.GONE
            replaceFragmentToActivity(
                supportFragmentManager,
                PlayTrackFragment.newInstance(data, Type.AUDIOS),
                R.id.container
            )
        }
    }

    companion object {
        fun newInstance() = AudioFragment()
    }
}
