package com.tlz.fragmentbase

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v4.widget.ViewDragHelper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout

/**
 * Created by tomlezen.
 * Data: 2018/5/4.
 * Time: 15:01.
 */
class FbFrameLayout(context: Context, private val swipeCallback: FbSwipeCallback) : FrameLayout(context, null) {

  var scrollFinishThreshold = DEFAULT_SCROLL_THRESHOLD
    set(value) {
      if (value >= 1.0f || value <= 0) {
        throw IllegalArgumentException("Threshold value should be between 0 and 1.0")
      }
      field = value
    }

  private val helper: ViewDragHelper

  private var scrollPercent: Float = 0f
  private var scrimOpacity: Float = 0f

  private val shadowLeft: Drawable by lazy {
    ContextCompat.getDrawable(context, R.mipmap.shadow_left)!!
  }
  private val shadowRight: Drawable by lazy {
    ContextCompat.getDrawable(context, R.mipmap.shadow_right)!!
  }
  private val shadowTop: Drawable by lazy {
    ContextCompat.getDrawable(context, R.mipmap.shadow_top)!!
  }
  private val shadowBottom: Drawable by lazy {
    ContextCompat.getDrawable(context, R.mipmap.shadow_bottom)!!
  }
  private val drawnShadowRect = Rect()

  var curDragEdge = FbSwipeMode.NONE
  var dragMode = FbSwipeMode.NONE
  var dragEnable = false

  init {
    helper = ViewDragHelper.create(this, ViewDragCallback())
    helper.setEdgeTrackingEnabled(FbSwipeMode.All)
    super.setBackgroundColor(Color.TRANSPARENT)
  }

  override fun setBackground(background: Drawable?) {}

  override fun setBackgroundColor(color: Int) {}

  override fun setBackgroundResource(@DrawableRes resId: Int) {}

  override fun drawChild(canvas: Canvas, child: View, drawingTime: Long): Boolean {
    val drawChild = super.drawChild(canvas, child, drawingTime)
    if ((getChildAt(
            childCount - 1) == child) && scrimOpacity > 0 && helper.viewDragState != ViewDragHelper.STATE_IDLE) {
      drawShadow(canvas, child)
      drawScrim(canvas, child)
    }
    return drawChild
  }

  private fun drawShadow(canvas: Canvas, child: View) {
    val childRect = drawnShadowRect
    child.getHitRect(childRect)

    if (curDragEdge and dragMode != 0) {
      var shadowDrawable: Drawable? = null
      when (curDragEdge) {
        FbSwipeMode.LEFT -> {
          shadowDrawable = shadowLeft.apply {
            setBounds(childRect.left - shadowLeft.intrinsicWidth, childRect.top, childRect.left,
                childRect.bottom)
          }
        }
        FbSwipeMode.RIGHT -> {
          shadowDrawable = shadowRight.apply {
            setBounds(childRect.right, childRect.top, childRect.right + shadowLeft.intrinsicWidth,
                childRect.bottom)
          }
        }
        FbSwipeMode.TOP -> {
          shadowDrawable = shadowTop.apply {
            setBounds(childRect.left, childRect.top - shadowTop.intrinsicHeight, childRect.right,
                childRect.top)
          }
        }
        FbSwipeMode.BOTTOM -> {
          shadowDrawable = shadowBottom.apply {
            setBounds(childRect.left, childRect.bottom, childRect.right,
                childRect.bottom + shadowBottom.intrinsicHeight)
          }
        }
      }
      shadowDrawable?.alpha = (scrimOpacity * FULL_ALPHA).toInt()
      shadowDrawable?.draw(canvas)
    }
  }

  private fun drawScrim(canvas: Canvas, child: View) {
    val baseAlpha = (DEFAULT_SCRIM_COLOR and 0xff000000.toInt()).ushr(24)
    val alpha = (baseAlpha * scrimOpacity).toInt()
    val color = alpha shl 24

    if (curDragEdge and dragMode != 0) {
      when (curDragEdge) {
        FbSwipeMode.LEFT -> {
          canvas.clipRect(0, 0, child.left, height)
        }
        FbSwipeMode.RIGHT -> {
          canvas.clipRect(child.right, 0, right, height)
        }
        FbSwipeMode.TOP -> {
          canvas.clipRect(0, 0, right, child.top)
        }
        FbSwipeMode.BOTTOM -> {
          canvas.clipRect(0, child.bottom, right, bottom)
        }
      }
    }
    canvas.drawColor(color)
  }

  override fun computeScroll() {
    scrimOpacity = 1f - scrollPercent
    if (scrimOpacity >= 0f && helper.continueSettling(true)) {
      ViewCompat.postInvalidateOnAnimation(this)
    }
  }

  internal inner class ViewDragCallback : ViewDragHelper.Callback() {

    override fun tryCaptureView(child: View, pointerId: Int): Boolean =
        helper.isEdgeTouched(dragMode, pointerId)

    override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int =
        when (curDragEdge) {
          FbSwipeMode.LEFT -> Math.min(child.width, Math.max(left, 0))
          FbSwipeMode.RIGHT -> Math.min(0, Math.max(left, -child.width))
          else -> 0
        }

    override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int =
        when (curDragEdge) {
          FbSwipeMode.TOP -> Math.min(child.height, Math.max(top, 0))
          FbSwipeMode.BOTTOM -> Math.min(0, Math.max(top, -child.height))
          else -> 0
        }

    override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
      super.onViewPositionChanged(changedView, left, top, dx, dy)
      scrollPercent = when (curDragEdge) {
        FbSwipeMode.LEFT -> Math.abs(left.toFloat() / (changedView.width + shadowLeft.intrinsicWidth))
        FbSwipeMode.RIGHT -> Math.abs(left.toFloat() / (changedView.width + shadowRight.intrinsicWidth))
        FbSwipeMode.TOP -> Math.abs(top.toFloat() / (changedView.height + shadowTop.intrinsicHeight))
        FbSwipeMode.BOTTOM -> Math.abs(top.toFloat() / (changedView.height + shadowBottom.intrinsicHeight))
        else -> 0f
      }
      swipeCallback.onSwipePercent(scrollPercent)
      invalidate()
      if (scrollPercent > 1f) {
        swipeCallback.onSwipeBack()
      }
    }

    override fun getViewHorizontalDragRange(child: View): Int = 1

    override fun getViewVerticalDragRange(child: View): Int = 1

    override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
      val childWidth = releasedChild.width
      val childHeight = releasedChild.height

      var left = 0
      var top = 0
      when (curDragEdge) {
        FbSwipeMode.LEFT -> {
          left = if (xvel >= 0f && scrollPercent > scrollFinishThreshold) childWidth + shadowLeft.intrinsicWidth + OVER_SCROLL_DISTANCE else 0
        }
        FbSwipeMode.RIGHT -> {
          left = -if (xvel <= 0f && scrollPercent > scrollFinishThreshold) childWidth + shadowRight.intrinsicWidth + OVER_SCROLL_DISTANCE else 0
        }
        FbSwipeMode.TOP -> {
          top = if (yvel >= 0f && scrollPercent > scrollFinishThreshold) childHeight + shadowTop.intrinsicHeight + OVER_SCROLL_DISTANCE else 0
        }
        FbSwipeMode.BOTTOM -> {
          top = -if (yvel <= 0f && scrollPercent > scrollFinishThreshold) childHeight + shadowBottom.intrinsicHeight + OVER_SCROLL_DISTANCE else 0
        }
      }

      helper.settleCapturedViewAt(left, top)
      invalidate()
    }

    override fun onEdgeTouched(edgeFlags: Int, pointerId: Int) {
      super.onEdgeTouched(edgeFlags, pointerId)
      curDragEdge = edgeFlags
    }
  }

  override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
    if (dragEnable && dragMode != FbSwipeMode.NONE) {
      return helper.shouldInterceptTouchEvent(ev)
    }
    return super.onInterceptTouchEvent(ev)
  }

  @SuppressLint("ClickableViewAccessibility")
  override fun onTouchEvent(event: MotionEvent): Boolean {
    if (dragEnable && dragMode != FbSwipeMode.NONE) {
      helper.processTouchEvent(event)
      return true
    }
    return false
  }

  interface FbSwipeCallback{

    /**
     * 滑动的百分比.
     * @param percent Float
     */
    fun onSwipePercent(percent: Float)

    /**
     * 滑动返回.
     */
    fun onSwipeBack()
  }

  companion object {
    private const val DEFAULT_SCRIM_COLOR = 0x99000000.toInt()
    private const val FULL_ALPHA = 255
    private const val DEFAULT_SCROLL_THRESHOLD = 0.4f
    private const val OVER_SCROLL_DISTANCE = 10
  }

}