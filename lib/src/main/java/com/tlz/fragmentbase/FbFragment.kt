package com.tlz.fragmentbase

import android.os.Bundle
import android.support.annotation.CallSuper
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils

/**
 * Created by tomlezen.
 * Data: 2018/5/4.
 * Time: 11:41.
 */
abstract class FbFragment : Fragment(), FbFragmentResult, FbBackPress, FbLifeCycle, FbFrameLayout. FbSwipeCallback {

  private var rootView: FbFrameLayout? = null
  private var contentView: View? = null

  private var isCreated = false
  private var isViewCreated = false
  private var isLazyInit = false
  private var isSwipeBack = false

  protected var swipeBackMode
    set(value) { rootView?.dragMode = value }
    get() = rootView?.dragMode ?: FbSwipeMode.NONE

  /** 请求码. */
  private var requestCode: Int = 0
  private var resultCode: Int = FbConst.RESULT_OK
  private var resultData: Bundle? = null

  override fun setArguments(args: Bundle?) {
    super.setArguments(args)
    args?.apply {
      if (args.containsKey(FbConst.REQUEST_CODE)) {
        requestCode = getInt(FbConst.REQUEST_CODE)
      }
    }
  }

  final override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
    return if (!isSwipeBack) {
      val animation = onCreateFbAnimation(transit, enter, nextAnim)
      animation?.let {
        FbAnimationSet(true).add(it).endWithAction { onAnimationEnd(enter) }
      } ?: animation
    } else {
      return super.onCreateAnimation(transit, enter, nextAnim)
    }
  }

  protected open fun onCreateFbAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
    if (isSwipeBack) {
      return AnimationUtils.loadAnimation(activity, R.anim.empty)
    } else {
      if (nextAnim != 0) {
        try {
          return AnimationUtils.loadAnimation(activity, nextAnim)
        } catch (e: Exception) {
        }
      }
    }
    return super.onCreateAnimation(transit, enter, nextAnim)
  }

  final override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    if (rootView == null && container != null) {
      onCreateViewBefore()
      rootView = FbFrameLayout(activity!!, this)
      contentView = onCreateView(inflater, container)
    }
    return if (contentView != null) {
      isCreated = true
      val parentView = contentView?.parent as? ViewGroup
      parentView?.removeView(contentView)
      rootView?.addView(contentView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
      rootView
    } else {
      super.onCreateView(inflater, container, savedInstanceState)
    }
  }

  @CallSuper
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    if (!isViewCreated) {
      onInit(savedInstanceState)
    }
    isViewCreated = true
  }

  @CallSuper
  override fun setUserVisibleHint(isVisibleToUser: Boolean) {
    super.setUserVisibleHint(isVisibleToUser)
    if (isVisibleToUser && isCreated) {
      prepareLazyInit()
    }
  }

  @CallSuper
  override fun onResume() {
    super.onResume()
    if (userVisibleHint && !isLazyInit) {
      if (isCreated) {
        prepareLazyInit()
      }
    }
  }

  private fun prepareLazyInit() {
    onLazyInit()
    isCreated = false
    isLazyInit = true
  }

  override fun onDestroy() {
    FbFragmentStack.remove(this)
    super.onDestroy()
  }

  override fun onFragmentResult(requestCode: Int, resultCode: Int, data: Bundle?) {
    // do nothing
  }

  override fun onBackPress(): Boolean {
    if (FbFragmentStack.get(this)?.onBackPress() != true && requestCode != 0) {
      back(requestCode, resultCode, resultData)
      return true
    }
    return false
  }

  override fun onFbResume() {
    // do nothing
  }

  override fun onFbStop() {
    // do nothing
  }

  @CallSuper
  override fun onSwipeBack() {
    isSwipeBack = back(requestCode, resultCode, resultData)
  }

  override fun onSwipePercent(percent: Float) {
    // do nothing
  }

  /**
   * 设置返回结果.
   * @param resultCode Int
   * @param resultData Bundle?
   */
  protected fun setResult(resultCode: Int, resultData: Bundle?) {
    this.resultCode = resultCode
    this.resultData = resultData
  }

  /**
   * onCreateView中创建View之前触发.
   */
  protected open fun onCreateViewBefore(){}

  /**
   * Fragment转场动画结束触发.
   * @param enter Boolean
   */
  protected open fun onAnimationEnd(enter: Boolean){}
  abstract fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View
  abstract fun onInit(savedInstanceState: Bundle?)
  abstract fun onLazyInit()

}