package com.tlz.fragmentbase

/**
 * Created by tomlezen.
 * Data: 2018/5/4.
 * Time: 14:37.
 */
interface FbLifeCycle {

  /**
   * 当Fragment不知在最顶层时触发调用.
   */
  fun onFbStop()

  /**
   * 当Fragment在最顶层时触发调用.
   */
  fun onFbResume()

}