package com.sunasterisk.music_72.utils

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.sunasterisk.music_72.R

fun AppCompatActivity.addFragmentToActivity(
    fragmentManager: FragmentManager,
    fragment: Fragment,
    idRes: Int
) {
    fragmentManager.beginTransaction()
        .setCustomAnimations(
            android.R.animator.fade_in, android.R.animator.fade_out,
            android.R.animator.fade_in, android.R.animator.fade_out
        )
        .add(idRes, fragment, fragment::class.java.simpleName)
        .addToBackStack(fragment::class.java.simpleName)
        .commit()
}

fun AppCompatActivity.replaceFragmentToActivity(
    fragmentManager: FragmentManager,
    fragment: Fragment,
    idRes: Int
) {
   fragmentManager.beginTransaction()
        .setCustomAnimations(
            R.anim.anim_fade_in_y, android.R.animator.fade_out,
            R.anim.anim_fade_in_y, R.anim.anim_fade_out_y
        )
        .replace(idRes, fragment)
        .addToBackStack(fragment::class.java.simpleName)
        .commit()
}

fun AppCompatActivity.replaceFragmentToActivityNoBack(
    fragmentManager: FragmentManager,
    fragment: Fragment,
    idRes: Int
) {
    fragmentManager.beginTransaction()
        .replace(idRes, fragment)
        .commit()
}

fun AppCompatActivity.removeFragmentToActivity(
    fragmentManager: FragmentManager,
    fragment: Fragment
) {
    fragmentManager.beginTransaction()
        .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
        .remove(fragment)
        .commit()
}

fun AppCompatActivity.isFragmentVisible(tag: String) =
    supportFragmentManager.findFragmentByTag(tag)?.isVisible

fun AppCompatActivity.setupToolbar(toolbar: Toolbar) {
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayShowTitleEnabled(false)
}
