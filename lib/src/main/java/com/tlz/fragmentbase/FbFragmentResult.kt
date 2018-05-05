package com.tlz.fragmentbase

import android.os.Bundle

/**
 * Created by tomlezen.
 * Data: 2018/5/4.
 * Time: 13:23.
 */
interface FbFragmentResult {

  fun onFragmentResult(requestCode: Int, resultCode: Int, data: Bundle?)

}