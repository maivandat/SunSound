package com.sunasterisk.music_72.screen.fragment.playtrack

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.CompoundButton
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.sunasterisk.music_72.R
import com.sunasterisk.music_72.data.anotation.Loop
import com.sunasterisk.music_72.data.anotation.State
import com.sunasterisk.music_72.data.anotation.Type
import com.sunasterisk.music_72.data.model.Track
import com.sunasterisk.music_72.data.source.local.TrackLocalDataSource
import com.sunasterisk.music_72.data.source.remote.TrackRemoteDataSource
import com.sunasterisk.music_72.data.source.remote.connection.RetrofitClient
import com.sunasterisk.music_72.data.source.repository.TrackRepositoryImplementor
import com.sunasterisk.music_72.databinding.FragmentPlayTrackBinding
import com.sunasterisk.music_72.screen.MainActivity
import com.sunasterisk.music_72.screen.factory.ViewModelFactory
import com.sunasterisk.music_72.screen.fragment.audio.AudioFragment
import com.sunasterisk.music_72.screen.fragment.audio.AudioViewModel
import com.sunasterisk.music_72.screen.fragment.playtrack.control.ControlFragment
import com.sunasterisk.music_72.screen.fragment.search.SearchFragment
import com.sunasterisk.music_72.screen.fragment.search.SearchViewModel
import com.sunasterisk.music_72.screen.fragment.tracks.TracksViewModel
import com.sunasterisk.music_72.screen.service.DownloadTrackService
import com.sunasterisk.music_72.screen.service.PlayTrackService
import com.sunasterisk.music_72.screen.service.listener.OnPlayTrackListener
import com.sunasterisk.music_72.utils.BindingUtils
import com.sunasterisk.music_72.utils.MusicUtils
import com.sunasterisk.music_72.utils.replaceFragmentToActivityNoBack
import com.sunasterisk.music_72.utils.setupToolbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_play_track.*
import kotlinx.android.synthetic.main.layout_body_playtrack.*
import kotlinx.android.synthetic.main.layout_footer_playtrack.*
import kotlinx.android.synthetic.main.layout_header_playtrack.*
import java.util.*

class PlayTrackFragment : Fragment(), View.OnClickListener,
    CompoundButton.OnCheckedChangeListener,
    OnPlayTrackListener, SeekBar.OnSeekBarChangeListener {
    private lateinit var binding: FragmentPlayTrackBinding
    private lateinit var service: PlayTrackService
    private var typeActivity: String? = null
    private var track: Track? = null
    private var tracks = mutableListOf<Track>()
    private var loop = Loop.NON
    private var shuffle = false
    private var prefs: SharedPreferences? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_play_track, container, false)
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

    override fun onResume() {
        super.onResume()
        service.registerPlayTrackListener(this)
    }

    private fun initView() {
        (activity as AppCompatActivity).run {
            setupToolbar(toolBarPlayTrack)
            containerControl.visibility = View.GONE
        }
        buttonBack.setOnClickListener(this)
        buttonNext.setOnClickListener(this)
        buttonPrevious.setOnClickListener(this)
        buttonLoop.setOnClickListener(this)
        cbActionPlayTrack.setOnCheckedChangeListener(this)
        cbShuffle.setOnCheckedChangeListener(this)
        seekBarTime.setOnSeekBarChangeListener(this)
        service.registerPlayTrackListener(this)
    }

    private fun initData() {
        arguments?.apply {
            track = getParcelable(ARGUMENT_PLAY_TRACK_KEY)
            typeActivity = getString(ARGUMENT_TYPE_ACTIVITY)
        }
        prefs = context?.getSharedPreferences(PREF_LOOP, Context.MODE_PRIVATE)
        loop = prefs?.getString(PREF_KEY_LOOP, Loop.NON).toString()
        prefs?.getBoolean(PREF_KEY_SHUFFLE, false)?.let {
            shuffle = it
        }
        service.loop = loop
        typeActivity?.let { type ->
            when(type) {
                Type.TRACKS -> {
                    activity?.let { activity ->
                        ViewModelProviders.of(activity).get(TracksViewModel::class.java)
                            .tracks.observe(this, Observer {
                                tracks.clear()
                                tracks.addAll(it)
                                checkShuffle(shuffle)
                                playTrack()
                            })
                    }
                    service.setType(Type.TRACKS)
                }
                Type.SEARCH -> {
                    activity?.let { activity ->
                        ViewModelProviders.of(activity).get(SearchViewModel::class.java)
                            .tracks.observe(this, Observer {
                                tracks.clear()
                                tracks.addAll(it)
                                checkShuffle(shuffle)
                                playTrack()
                            })
                    }
                    service.setType(Type.SEARCH)
                }
                else -> {
                    activity?.let { activity ->
                        ViewModelProviders.of(activity).get(AudioViewModel::class.java)
                            .audios.observe(this, Observer {
                                tracks.clear()
                                tracks.addAll(it)
                                checkShuffle(shuffle)
                                playTrack()

                            })
                    }
                    service.setType(Type.AUDIOS)
                }
            }
        }
    }

    private fun bindDataToView(track: Track) {
        setHasOptionsMenu(track.downloadable)
        BindingUtils.image(imageLogoUser, track.user.avatarUrl)
        textTrackName.text = track.title
        textUserName.text = track.user.username
        if (service.state == State.PLAY) {
            cbActionPlayTrack.isChecked = true
            animationImageUser()
        } else {
            cbActionPlayTrack.isChecked = false
            imageLogoUser.clearAnimation()
        }
        buttonLoop.setImageResource(setImageLoop(loop))
        cbShuffle.isChecked = shuffle
    }

    private fun playTrack() {
        track?.let {
            if (service.getCurrentTrack() == null) {
                service.changeTrack(it)
            } else {
                if (it.id == service.getCurrentTrack()?.id) {
                    bindDataToView(service.getCurrentTrack()!!)
                } else {
                    service.changeTrack(it)
                }
            }
        }
    }

    private fun animationImageUser() {
        val anim =
            RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        anim.interpolator = LinearInterpolator()
        anim.repeatCount = Animation.INFINITE
        anim.duration = 7000
        imageLogoUser.startAnimation(anim)
    }

    private fun setImageLoop(@Loop loop: String) =
       when(loop) {
           Loop.NON -> R.drawable.ic_non_repeat_50dp
           Loop.ONE -> R.drawable.ic_repeat_one_50dp
           else -> R.drawable.ic_repeat_50dp
       }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        if (typeActivity.equals(Type.TRACKS) || typeActivity.equals(Type.SEARCH)) {
            inflater.inflate(R.menu.menu_download, menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_download) {
            binding.viewModel?.getAudios()
            binding.viewModel?.audios?.observe(this, Observer {
                loop@for(i in it.indices) {
                    if (track?.id == it[i].id) {
                        Toast.makeText(context, context?.getString(R.string.message_downloaded), Toast.LENGTH_SHORT).show()
                        break@loop
                    }
                    if (i == it.size - 1) {
                        activity?.startService(DownloadTrackService.getIntent(context, track))
                    }
                }
            })
            return true
        }
        return false
    }

    private fun createViewModel() {
        binding.viewModel =
            ViewModelProviders.of(
                this,
                ViewModelFactory{
                    PlayTrackViewModel(
                        TrackRepositoryImplementor(
                            TrackLocalDataSource(),
                            TrackRemoteDataSource(RetrofitClient())
                        )
                    )
                }
            ).get(PlayTrackViewModel::class.java)
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.buttonBack -> {
                imageLogoUser.animation = null
                (activity as AppCompatActivity).apply {
                    supportFragmentManager.popBackStack()
                    if (typeActivity.equals(AudioFragment::class.java.simpleName) ||typeActivity.equals(SearchFragment::class.java.simpleName) ) {
                        navigationBottomHome.visibility = View.VISIBLE
                    }
                    replaceFragmentToActivityNoBack(supportFragmentManager, ControlFragment.newInstance(), R.id.containerControl)
                    containerControl.visibility = View.VISIBLE
                    containerControl.animation = AnimationUtils.loadAnimation(this, R.anim.anim_alpha)
                }
            }
            R.id.buttonNext -> {
                imageLogoUser.animation = null
                cbActionPlayTrack.isChecked = false
                textCurrentDuration.text = context?.getString(R.string.value_default_time)
                textDuration.text = context?.getString(R.string.value_default_time)
                service.nextTrack()
            }
            R.id.buttonPrevious -> {
                imageLogoUser.animation = null
                cbActionPlayTrack.isChecked = false
                textCurrentDuration.text = context?.getString(R.string.value_default_time)
                textDuration.text = context?.getString(R.string.value_default_time)
                service.previousTrack()
            }
            R.id.buttonLoop -> {
                when(service.loop) {
                    Loop.NON -> {
                        loop = Loop.ONE
                        buttonLoop.setImageResource(setImageLoop(Loop.ONE))
                        service.loop = Loop.ONE
                    }
                    Loop.ONE -> {
                        loop = Loop.ALL
                        buttonLoop.setImageResource(setImageLoop(Loop.ALL))
                        service.loop = Loop.ALL
                    }
                    Loop.ALL -> {
                        loop = Loop.NON
                        buttonLoop.setImageResource(setImageLoop(Loop.NON))
                        service.loop = Loop.NON
                    }
                }
            }
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when(buttonView?.id) {
            R.id.cbActionPlayTrack -> {
                if (isChecked) {
                    animationImageUser()
                    service.startTrack()
                } else {
                    imageLogoUser.animation = null
                    service.pauseTrack()
                }
            }
            R.id.cbShuffle -> {
                checkShuffle(isChecked)
                shuffle = isChecked
            }
        }
    }

    private fun checkShuffle(isShuffle: Boolean) {
        if (isShuffle) {
            val shuffleTracks = tracks.shuffled()
            service.setTracks(shuffleTracks.toMutableList())
            updateNextTrackToView()
        } else {
            service.setTracks(tracks)
            updateNextTrackToView()
        }
    }

    override fun onChangeTrackListener(track: Track) {
        cbActionPlayTrack.isChecked = true
        seekBarTime.progress = 0
        seekBarTime.max = 0
        progressTime.progress = 0
        progressTime.max = 0
        textCurrentDuration.text = context?.getString(R.string.value_default_time)
        textDuration.text = context?.getString(R.string.value_default_time)
        bindDataToView(track)
        updateNextTrackToView()
    }

    private fun updateNextTrackToView() {
        service.getNextTrack()?.let {
            BindingUtils.image(imageLogoUserNext, it.user.avatarUrl)
            textTrackNameNext.text = it.title
            textUserNameNext.text = it.user.username
            textNextSong.animation = AnimationUtils.loadAnimation(context, R.anim.anim_fade_out_x)
            constraintNext.animation = AnimationUtils.loadAnimation(context, R.anim.anim_fade_out_x)
        }
    }

    override fun onUpdateTimeListener() {
        seekBarTime?.let { seekBar ->
            service.getDuration()?.let {
                seekBar.max = it
                progressTime.max = it
                textDuration.text = MusicUtils.formatIntToTimeString(it)
            }

            service.getCurrentDuration()?.let {
                seekBar.progress = it
                progressTime.progress = it
                textCurrentDuration.text = MusicUtils.formatIntToTimeString(it)
            }
        }
    }

    override fun onUpdateActionPlayAndPauseTrack() {
        if (service.state == State.PLAY) {
            animationImageUser()
            cbActionPlayTrack.isChecked = true
        } else {
            imageLogoUser.clearAnimation()
            cbActionPlayTrack.isChecked = false
        }
    }

    override fun onDefaultTimeChangeTrack() {
        textCurrentDuration.text = getString(R.string.value_default_time)
        textDuration.text = getString(R.string.value_default_time)
    }


    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        // Do nothing
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        // Do nothing
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        seekBar?.progress?.let {
            service.seekTo(it)
        }
    }

    override fun onPause() {
        super.onPause()
        prefs?.edit()?.putString(PREF_KEY_LOOP, loop)?.apply()
        prefs?.edit()?.putBoolean(PREF_KEY_SHUFFLE, shuffle)?.apply()
        service.registerPlayTrackListener(null)
    }

    companion object {
        private const val ARGUMENT_PLAY_TRACK_KEY = "ARGUMENT_PLAY_TRACK_KEY"
        private const val ARGUMENT_TYPE_ACTIVITY = "ARGUMENT_TYPE_ACTIVITY"
        private const val PREF_LOOP = "PREF_LOOP"
        private const val PREF_KEY_LOOP = "PREF_KEY_LOOP"
        private const val PREF_KEY_SHUFFLE = "PREF_KEY_SHUFFLE"

        fun newInstance(track: Track, type: String) =
            PlayTrackFragment().apply {
                arguments = bundleOf(
                    ARGUMENT_PLAY_TRACK_KEY to track,
                    ARGUMENT_TYPE_ACTIVITY to type
                )
            }
    }
}
