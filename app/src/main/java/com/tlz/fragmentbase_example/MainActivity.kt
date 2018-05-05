package com.tlz.fragmentbase_example

import android.os.Bundle
import android.util.Log
import com.tlz.fragmentbase.FbAppCompatActivity
import com.tlz.fragmentbase.switch

class MainActivity : FbAppCompatActivity() {

  private val tag = MainActivity::class.java.canonicalName

  override val frgContainerId: Int = R.id.fl_frg_container

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    frgStack.enter = R.anim.slide_in_from_right
    frgStack.exit = R.anim.slide_out_from_right
    switch(TestFbFragment::class)
    frgStack.registerStackChangedCallback { i, fragment ->
      Log.d(tag, "fragment count: $i")
    }
  }
}
