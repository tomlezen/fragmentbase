package com.tlz.fragmentbase

import android.support.v4.widget.ViewDragHelper

/**
 * Created by tomlezen.
 * Data: 2018/5/4.
 * Time: 15:11.
 */
object FbSwipeMode {
  const val LEFT = ViewDragHelper.EDGE_LEFT
  const val RIGHT = ViewDragHelper.EDGE_RIGHT
  const val TOP = ViewDragHelper.EDGE_TOP
  const val BOTTOM = ViewDragHelper.EDGE_BOTTOM
  const val All = ViewDragHelper.EDGE_ALL
  const val NONE = -1
}