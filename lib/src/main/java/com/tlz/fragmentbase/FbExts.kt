package com.tlz.fragmentbase

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.view.animation.Animation
import kotlin.reflect.KClass

/**
 * Created by tomlezen.
 * Data: 2018/5/4.
 * Time: 11:44.
 */
fun FragmentActivity.switch(kclass: KClass<out Fragment>, frgTag: String = System.currentTimeMillis().toString(), vararg args: Pair<String, Any>): Boolean =
   FbFragmentStack.get(this)?.switch(kclass, frgTag, *args) ?: false

fun FragmentActivity.add(kclass: KClass<out Fragment>, frgTag: String = System.currentTimeMillis().toString(), requestCode: Int = 0, clearAll: Boolean = false, vararg args: Pair<String, Any>): Boolean =
    FbFragmentStack.get(this)?.add(kclass, frgTag, requestCode, clearAll, *args) ?: false

fun FragmentActivity.back(): Boolean =
    FbFragmentStack.get(this)?.back() ?: false

fun Fragment.switch(kclass: KClass<out Fragment>, frgTag: String = System.currentTimeMillis().toString(), vararg args: Pair<String, Any>): Boolean =
    FbFragmentStack.get(this.requireActivity())?.switch(kclass, frgTag, *args) ?: false

fun Fragment.add(kclass: KClass<out Fragment>, frgTag: String = System.currentTimeMillis().toString(), requestCode: Int = 0, clearAll: Boolean = false, vararg args: Pair<String, Any>): Boolean =
    FbFragmentStack.get(this.requireActivity())?.add(kclass, frgTag, requestCode, clearAll, *args) ?: false

fun Fragment.back(): Boolean =
    FbFragmentStack.get(this.requireActivity())?.back() ?: false

fun Fragment.back(requestCode: Int, resultCode: Int, data: Bundle?): Boolean =
    FbFragmentStack.get(this.requireActivity())?.back(requestCode, resultCode, data) ?: false

fun Fragment.switchForChild(kclass: KClass<out Fragment>, frgTag: String = System.currentTimeMillis().toString(), vararg args: Pair<String, Any>): Boolean =
    FbFragmentStack.get(this)?.switch(kclass, frgTag, *args) ?: false

fun Fragment.addForChild(kclass: KClass<out Fragment>, frgTag: String = System.currentTimeMillis().toString(), requestCode: Int = 0, clearAll: Boolean = false, vararg args: Pair<String, Any>): Boolean =
    FbFragmentStack.get(this)?.add(kclass, frgTag, requestCode, clearAll, *args) ?: false

fun Fragment.backForChild(): Boolean =
    FbFragmentStack.get(this)?.back() ?: false

fun Fragment.backForChild(requestCode: Int, resultCode: Int, data: Bundle?): Boolean =
    FbFragmentStack.get(this)?.back(requestCode, resultCode, data) ?: false

inline fun FbAnimationSet.endWithAction(crossinline action: () -> Unit): FbAnimationSet{
  setAnimationListener(object: Animation.AnimationListener {
    override fun onAnimationRepeat(p0: Animation?) {

    }

    override fun onAnimationEnd(p0: Animation?) {
      action()
    }

    override fun onAnimationStart(p0: Animation?) {

    }
  })
  return this
}

