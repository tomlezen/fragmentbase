package com.tlz.fragmentbase

import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.annotation.CallSuper
import android.support.annotation.DrawableRes
import android.support.v7.content.res.AppCompatResources
import android.support.v7.widget.Toolbar
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout

/**
 * 自动添加了Toolbar的Fragment.
 * Created by tomlezen.
 * Data: 2018/5/4.
 * Time: 17:02.
 */
abstract class FbToolbarFragment: FbFragment() {

  protected var toolbarEnable = false
  protected var toolbar: Toolbar? = null

  /**
   * toolbar 主题.
   */
  protected var themeId: Int = 0
    get() {
      return if (activity == null || field != 0) {
        field
      } else {
        activity?.let {
          it.packageManager.getActivityInfo(it.componentName, PackageManager.MATCH_DEFAULT_ONLY).themeResource
        } ?: 0
      }
    }
  /**
   * toolbar 标题.
   */
  protected var title: String? = null
    set(value) {
      field = value
      toolbar?.title = value
    }
  /**
   * toolbar 子标题.
   */
  protected var subtitle: String? = null
    set(value) {
      field = value
      toolbar?.subtitle = value
    }
  /**
   * toolbar 顶部导航图标.
   */
  protected var navigationIcon: Drawable? = null
    set(value) {
      field = value
      toolbar?.navigationIcon = navigationIcon
    }
  protected var fitsSystemWindows = true
  private var colorPrimary = Color.WHITE
  private var colorPrimaryDark = Color.WHITE

  private val windowTranslucentStatus by lazy {
    val ta = context!!.obtainStyledAttributes(intArrayOf(android.R.attr.windowTranslucentStatus))
    val value = ta.getBoolean(ta.getIndex(0), false)
    ta.recycle()
     value
  }
  protected val statusBarHeight by lazy {
    resources.getDimensionPixelSize(Resources.getSystem().getIdentifier("status_bar_height", "dimen", "android"))
  }
  protected val toolbarHeight by lazy {
    activity?.let {
      val ta = context!!.obtainStyledAttributes(intArrayOf(android.R.attr.actionBarSize))
      val value = ta.getDimensionPixelSize(ta.getIndex(0), 0)
      ta.recycle()
      value
    } ?: 0
  }
  protected var displayHomeAsUpEnabled = false
    set(value) {
      if (navigationIcon == null) {
        setNavigationIconId(R.drawable.ic_fb_back)
      }
      field = value
    }

  /** 状态栏占位view. */
  private var statusBarPlaceholder: View? = null

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
    if (!toolbarEnable && (!fitsSystemWindows || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP || !windowTranslucentStatus)) {
      return onCreateContentView(inflater, container)
    } else {
      val childContainer = inflater.inflate(R.layout.fb_toolbar_layout, container, false) as LinearLayout
      childContainer.orientation = LinearLayout.VERTICAL
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && windowTranslucentStatus && fitsSystemWindows) {
        statusBarPlaceholder = View(context)
        statusBarPlaceholder?.id = R.id.fb_statusBar
        childContainer.addView(statusBarPlaceholder, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, statusBarHeight))
      }
      if (toolbarEnable) {
        toolbar = Toolbar(context)
        toolbar?.id = R.id.fb_toolbar
        childContainer.addView(toolbar,
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, toolbarHeight))
      }
      initToolbar()
      childContainer.addView(onCreateContentView(inflater, childContainer))
      return childContainer
    }
  }

  @CallSuper
  override fun onLazyInit() {
    if (toolbarEnable) {
      invalidateOptionsMenu()
    }
  }

  /**
   * 初始化toolbar.
   */
  @CallSuper
  protected fun initToolbar(){
    val theme = resources.newTheme().apply { applyStyle(themeId, false) }
    var ta = theme.obtainStyledAttributes(intArrayOf(
        android.support.v7.appcompat.R.attr.colorPrimary,
        android.support.v7.appcompat.R.attr.colorPrimaryDark,
        android.support.v7.appcompat.R.attr.toolbarStyle))
    colorPrimary = ta.getColor(ta.getIndex(0), colorPrimary)
    colorPrimaryDark = ta.getColor(ta.getIndex(1), colorPrimaryDark)
    theme.applyStyle(ta.peekValue(ta.getIndex(2)).resourceId, false)
    ta.recycle()
    ta = theme.obtainStyledAttributes(intArrayOf(
        android.support.v7.appcompat.R.attr.colorPrimary,
        android.support.v7.appcompat.R.attr.colorPrimaryDark,
        android.support.v7.appcompat.R.attr.titleTextColor,
        android.support.v7.appcompat.R.attr.subtitleTextColor,
        android.support.v7.appcompat.R.attr.popupTheme))
    colorPrimary = ta.getColor(ta.getIndex(0), colorPrimary)
    colorPrimaryDark = ta.getColor(ta.getIndex(1), colorPrimaryDark)
    toolbar?.apply {
      setTitleTextColor(ta.getColor(ta.getIndex(2), Color.WHITE))
      val index = ta.getIndex(3)
      val value = TypedValue()
      ta.getValue(index, value)
      if (value.type != 0x1) {
        setSubtitleTextColor(ta.getColor(index, Color.WHITE))
      } else {
        setSubtitleTextColor(Color.WHITE)
      }
      popupTheme = ta.getResourceId(ta.getIndex(4), themeId)
      setBackgroundColor(colorPrimary)

      title = this@FbToolbarFragment.title
      subtitle = this@FbToolbarFragment.subtitle
      navigationIcon = this@FbToolbarFragment.navigationIcon
      setNavigationOnClickListener { onNavigationClicked() }
      setOnMenuItemClickListener { onOptionsItemSelected(it) }
    }
    ta.recycle()

    if (statusBarPlaceholder == null) {
      setWindowStatusBarColor()
    } else {
      statusBarPlaceholder?.setBackgroundColor(colorPrimaryDark)
    }
  }

  override fun setUserVisibleHint(isVisibleToUser: Boolean) {
    super.setUserVisibleHint(isVisibleToUser)
    if (isVisibleToUser && statusBarPlaceholder == null) {
      setWindowStatusBarColor()
    }
  }

  override fun onDestroy() {
    toolbar?.setNavigationOnClickListener(null)
    toolbar?.setOnMenuItemClickListener(null)
    super.onDestroy()
  }

  /**
   * 设置状态栏颜色.
   */
  protected fun setWindowStatusBarColor(){
    try {
      activity?.apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
          window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
          window.statusBarColor = colorPrimaryDark
        }
      }
    } catch (e: Exception) {
    }
  }


  /**
   * 设置toolbar导航图标.
   * @param resId Int
   */
  protected fun setNavigationIconId(@DrawableRes resId: Int){
    context?.apply {
      navigationIcon = AppCompatResources.getDrawable(this, resId)
    }
  }

  /**
   * toolbar导航按钮点击事件.
   */
  protected open fun onNavigationClicked(){
    onBackPress()
  }

  /**
   * 加载Menu.
   */
  protected fun invalidateOptionsMenu() {
    toolbar?.menu?.clear()
    activity?.apply {
      onCreateOptionsMenu(toolbar!!.menu, menuInflater)
    }
  }

  protected abstract fun onCreateContentView(inflater: LayoutInflater, container: ViewGroup): View

}