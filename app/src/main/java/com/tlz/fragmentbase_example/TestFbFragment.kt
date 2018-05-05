package com.tlz.fragmentbase_example

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import com.labo.kaji.fragmentanimations.CubeAnimation
import com.labo.kaji.fragmentanimations.MoveAnimation
import com.tlz.fragmentbase.FbSwipeMode
import com.tlz.fragmentbase.FbToolbarFragment
import com.tlz.fragmentbase.add
import kotlinx.android.synthetic.main.frg_test1.*
import java.util.*

/**
 * Created by tomlezen.
 * Data: 2018/5/4.
 * Time: 13:55.
 */
class TestFbFragment: FbToolbarFragment() {

  private val TAG = TestFbFragment::class.java.canonicalName

//  override fun onCreateFbAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
//    return if (enter) {
//      MoveAnimation.create(MoveAnimation.RIGHT, enter, 500)
//    } else {
//      CubeAnimation.create(CubeAnimation.LEFT, enter, 500)
//    }
//  }

  override fun onCreateViewBefore() {
    toolbarEnable = true
    displayHomeAsUpEnabled = true
    themeId = R.style.AppTheme_Toolbar
    title = "TestFragment${Random().nextInt(100)}"
    fitsSystemWindows = true
  }

  override fun onCreateContentView(inflater: LayoutInflater, container: ViewGroup): View {
    return inflater.inflate(R.layout.frg_test1, container, false)
  }

  override fun onInit(savedInstanceState: Bundle?) {
    btn_add_frg.setOnClickListener { add(TestFbFragment::class, requestCode = 10001) }
  }

  override fun onLazyInit() {
    swipeBackMode = when (Random().nextInt(6)) {
      1 -> FbSwipeMode.LEFT
      2 -> FbSwipeMode.RIGHT
      3 -> FbSwipeMode.TOP
      4 -> FbSwipeMode.BOTTOM
      5 -> FbSwipeMode.All
      else -> FbSwipeMode.NONE
    }
  }

  override fun onAnimationEnd(enter: Boolean) {
    Log.d(TAG, "onAnimationEnd: enter=$enter")
  }

  override fun onFragmentResult(requestCode: Int, resultCode: Int, data: Bundle?) {
    super.onFragmentResult(requestCode, resultCode, data)
    Log.d(TAG, "onFragmentResult: requestCode=$requestCode, resultCode=$resultCode, data=$data")
  }

  override fun onFbResume() {
    super.onFbResume()
    Log.d(TAG,"onFbResume")
  }

  override fun onFbStop() {
    super.onFbStop()
    Log.d(TAG,"onFbStop")
  }

}