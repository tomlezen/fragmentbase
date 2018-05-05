package com.tlz.fragmentbase

import android.content.Context
import android.util.AttributeSet
import android.view.animation.Animation
import android.view.animation.AnimationSet

/**
 * Created by tomlezen.
 * Data: 2018/5/4.
 * Time: 16:40.
 */
class FbAnimationSet : AnimationSet {

  constructor(context: Context, attrs: AttributeSet): super(context, attrs)
  constructor(shareInterpolator: Boolean): super(shareInterpolator)

  private var animationListener: AnimationListener? = null

  override fun setAnimationListener(listener: AnimationListener?) {
    animationListener = listener
    super.setAnimationListener(listener)
  }

  fun add(animation: Animation?): FbAnimationSet{
    addAnimation(animation)
    return this
  }

  fun notificationAnimationEnd(){
    animationListener?.onAnimationEnd(this)
  }
}