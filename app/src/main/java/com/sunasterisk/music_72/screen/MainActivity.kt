package com.sunasterisk.music_72.screen

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.sunasterisk.music_72.R
import com.sunasterisk.music_72.screen.fragment.audio.AudioFragment
import com.sunasterisk.music_72.screen.fragment.home.HomeFragment
import com.sunasterisk.music_72.screen.fragment.playtrack.control.ControlFragment
import com.sunasterisk.music_72.screen.fragment.search.SearchFragment
import com.sunasterisk.music_72.screen.service.PlayTrackService
import com.sunasterisk.music_72.utils.Action
import com.sunasterisk.music_72.utils.addFragmentToActivity
import com.sunasterisk.music_72.utils.listener.OnUpdateControlListener
import com.sunasterisk.music_72.utils.replaceFragmentToActivity
import com.sunasterisk.music_72.utils.replaceFragmentToActivityNoBack
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(),
    BottomNavigationView.OnNavigationItemSelectedListener, OnUpdateControlListener {
    var service: PlayTrackService? = null
    private var bound = false

    override fun onStart() {
        super.onStart()
        isStoragePermissionGranted()
        bindService(PlayTrackService.getIntent(this), connection, Context.BIND_AUTO_CREATE)
    }

    private fun isStoragePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
            ) {
                true
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1
                )
                false
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        service?.registerUpdateControlListener(this)
    }

    override fun onPause() {
        super.onPause()
        service?.registerUpdateControlListener(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (bound) {
            unbindService(connection)
            bound = false
        }
    }

    private fun initView() {
        intent.apply {
            if (intent.action == Action.ACTION_PLAY_TRACK) {
                // Do nothing
            } else {
                addFragmentToActivity(supportFragmentManager, HomeFragment.newInstance(), R.id.container)
                navigationBottomHome.setOnNavigationItemSelectedListener(this@MainActivity)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            Log.v(MainActivity::class.java.simpleName,"Permission: "+permissions[0]+ "was "+grantResults[0]);
            //resume tasks needing this permission
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.navigationBottomHome -> {
                replaceFragmentToActivity(
                    supportFragmentManager,
                    HomeFragment.newInstance(),
                    R.id.container
                )
            }
            R.id.navigationBottomLibrary -> {
                replaceFragmentToActivity(
                    supportFragmentManager,
                    AudioFragment.newInstance(),
                    R.id.container
                )
            }
            R.id.navigationBottomSearch -> {
                replaceFragmentToActivity(
                    supportFragmentManager,
                    SearchFragment.newInstance(),
                    R.id.container
                )
            }
        }
        return false
    }

    private val connection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            bound = false
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as PlayTrackService.PlayTrackBinder
            this@MainActivity.service = binder.getService()
            this@MainActivity.service?.registerUpdateControlListener(this@MainActivity)
            initView()
            bound = true
        }
    }

    override fun onUpdateControl() {
        replaceFragmentToActivityNoBack(
            supportFragmentManager,
            ControlFragment.newInstance(),
            R.id.containerControl
        )
    }
}
