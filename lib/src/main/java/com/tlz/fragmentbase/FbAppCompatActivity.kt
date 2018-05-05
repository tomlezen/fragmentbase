package com.tlz.fragmentbase

import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity

/**
 * Created by tomlezen.
 * Data: 2018/5/4.
 * Time: 11:34.
 */
abstract class FbAppCompatActivity : AppCompatActivity() {

  abstract val frgContainerId: Int

  val frgStack: FbFragmentStack by lazy { FbFragmentStack.with(this, frgContainerId) }

  override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
    super.onSaveInstanceState(outState, outPersistentState)
    outState?.apply { frgStack.onSaveInstanceState(this) }
  }

  override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
    super.onRestoreInstanceState(savedInstanceState)
    savedInstanceState?.apply { frgStack.onRestoreInstanceState(this) }
  }

  override fun onBackPressed() {
    if (!frgStack.onBackPress()) {
      super.onBackPressed()
    }
  }

  override fun onDestroy() {
    FbFragmentStack.remove(this)
    super.onDestroy()
  }

}