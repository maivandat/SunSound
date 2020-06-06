package com.sunasterisk.music_72.screen.fragment.playtrack.control

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.sunasterisk.music_72.R
import com.sunasterisk.music_72.data.anotation.State
import com.sunasterisk.music_72.data.model.Track
import com.sunasterisk.music_72.data.source.local.TrackLocalDataSource
import com.sunasterisk.music_72.data.source.remote.TrackRemoteDataSource
import com.sunasterisk.music_72.data.source.remote.connection.RetrofitClient
import com.sunasterisk.music_72.data.source.repository.TrackRepositoryImplementor
import com.sunasterisk.music_72.databinding.FragmentControlBinding
import com.sunasterisk.music_72.screen.MainActivity
import com.sunasterisk.music_72.screen.factory.ViewModelFactory
import com.sunasterisk.music_72.screen.fragment.playtrack.PlayTrackFragment
import com.sunasterisk.music_72.screen.service.PlayTrackService
import com.sunasterisk.music_72.screen.service.listener.OnControlListener
import com.sunasterisk.music_72.utils.BindingUtils
import com.sunasterisk.music_72.utils.replaceFragmentToActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_control.*
import java.util.*

class ControlFragment : Fragment(), View.OnClickListener,
    CompoundButton.OnCheckedChangeListener,
    OnControlListener {
    private lateinit var binding: FragmentControlBinding
    private lateinit var service: PlayTrackService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_control, container, false)
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
        service.registerControlListener(this)
    }
    private fun initView() {
        (activity as AppCompatActivity).apply {
            containerControl.setOnClickListener(this@ControlFragment)
        }
        buttonNext.setOnClickListener(this)
        buttonPrevious.setOnClickListener(this)
        cbActionPlayTrack.setOnCheckedChangeListener(this)
        if (service.state == State.PLAY) {
            cbActionPlayTrack.isChecked = true
            animationImageUser()
        } else {
            cbActionPlayTrack.isChecked = false
            imageLogoUser.clearAnimation()
        }
        service.getCurrentTrack()?.let {
            BindingUtils.image(imageLogoUser, it.user.avatarUrl)
            textTrackName.text = it.title
            textUserName.text = it.user.username
        }
    }

    private fun initData() {
        // Do nothing
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

    private fun createViewModel() {
        binding.viewModel =
            ViewModelProviders.of(
                this,
                ViewModelFactory{
                    ControlViewModel(
                        TrackRepositoryImplementor(
                            TrackLocalDataSource(), TrackRemoteDataSource(RetrofitClient())
                        )
                    )
                }
            ).get(ControlViewModel::class.java)
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        if (isChecked) {
            service.startTrack()
            animationImageUser()
        } else {
            service.pauseTrack()
            imageLogoUser.clearAnimation()
        }
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.buttonNext -> service.nextTrack()
            R.id.buttonPrevious -> service.previousTrack()
            else -> {
                (activity as AppCompatActivity).apply {
                    navigationBottomHome.visibility = View.GONE
                    service.getCurrentTrack()?.let { track ->
                        service.getType()?.let { type ->
                            replaceFragmentToActivity(
                                supportFragmentManager,
                                PlayTrackFragment.newInstance(track, type),
                                R.id.container
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onChangeTrackListener(track: Track) {
        // Do nothing
    }

    override fun onUpdateTimeListener() {
        service.getDuration()?.let {
            progressControl.max = it
        }
        service.getCurrentDuration()?.let {
            progressControl.progress = it
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
        // Do nothing
    }

    override fun onPause() {
        super.onPause()
        service.registerControlListener(null)
    }

    companion object {
        fun newInstance() = ControlFragment()
    }


}
