package com.tlz.fragmentbase

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.support.annotation.IdRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import com.tlz.fragmentbase.FbConst.STATE_STACK
import java.io.Serializable
import java.util.*
import kotlin.reflect.KClass


/**
 * Created by tomlezen.
 * Data: 2018/5/4.
 * Time: 9:45.
 */
class FbFragmentStack private constructor(
    private val ctx: Context,
    private val fragmentManager: FragmentManager,
    private val containerId: Int
) {

  var enter = R.anim.empty
  var exit = R.anim.empty

  private val stack = LinkedList<Fragment>()
  private val topLevelTags = mutableListOf<String>()
  private var fragmentTransaction: FragmentTransaction? = null

  private val stackChangedCallbacks = mutableListOf<(Int, Fragment) -> Unit>()

  var topFragment: Fragment? = null
    get() = stack.peekLast()
    private set

  var botFragment: Fragment? = null
    get() = stack.peekFirst()
    private set


  private val handler = Handler()

  /** 执行事务. */
  private val execPendingTransactions: Runnable = Runnable {
    fragmentTransaction?.apply {
      commit()
      fragmentManager.executePendingTransactions()

      dispatchOnStackChangedEvent()
    }
    fragmentTransaction = null
  }


  /**
   * 销毁所有界面.
   * 不要在onDestroy生命周期中调用.
   */
  fun destory() {
    ensureTransaction()
    fragmentTransaction?.setCustomAnimations(enter, exit, enter, exit)

    val botFragment = this.botFragment
    stack.filter { it != botFragment }.forEach { remove(it) }
    stack.clear()

    topLevelTags.map { fragmentManager.findFragmentByTag(it) }.forEach { remove(it) }

    fragmentTransaction?.commit()
    fragmentTransaction = null
  }

  /**
   * 保存状态.
   * @param outState Bundle
   */
  fun onSaveInstanceState(outState: Bundle) {
    executePendingTransactions()

    val stackTags = arrayOfNulls<String>(stack.size)
    stack.forEachIndexed { index, fragment -> stackTags[index] = fragment.tag }

    outState.putStringArray(STATE_STACK, stackTags)
  }

  /**
   * 恢复状态.
   * @param state Bundle
   */
  fun onRestoreInstanceState(state: Bundle) {
    state.getStringArray(STATE_STACK)
        ?.map { fragmentManager.findFragmentByTag(it) }
        ?.forEach { stack.add(it) }

    dispatchOnStackChangedEvent()
  }

  /**
   * 切换Fragment，功能相当于replace.
   * @param kclass KClass<out Fragment>
   * @param frgTag String
   * @param args Array<out Pair<String, Any>>
   * @return Boolean
   */
  fun switch(kclass: KClass<out Fragment>, frgTag: String = System.currentTimeMillis().toString(), vararg args: Pair<String, Any>): Boolean {
    this.botFragment?.apply {
      if (tag == this.tag) {
        while (stack.size > 1) {
          remove(stack.pollLast())
        }
        attach(stack.peek(), frgTag)
        return commit()
      }
    }

    val frg = fragmentManager.findFragmentByTag(frgTag)
        ?: Fragment.instantiate(ctx, kclass.java.name, createFrgArgs(*args))
    ensureTransaction()
    fragmentTransaction?.setCustomAnimations(enter, exit, enter, exit)
    clear()
    attach(frg, frgTag)
    stack.add(frg)
    topLevelTags.add(frgTag)
    return commit()
  }

  /**
   * 添加Fragment页面.
   * @param kclass KClass<out Fragment>
   * @param frgTag String
   * @param requestCode Int
   * @param clearAll Boolean 是否清除其它页面.
   * @param args Array<out Pair<String, Any>>
   * @return Boolean
   */
  fun add(kclass: KClass<out Fragment>, frgTag: String = System.currentTimeMillis().toString(), requestCode: Int = 0, clearAll: Boolean = false, vararg args: Pair<String, Any>): Boolean {
    if (clearAll) {
      clear()
    }
    val topFrg = topFragment
    ensureTransaction()
    fragmentTransaction?.setCustomAnimations(enter, exit, enter, exit)
    val frg = fragmentManager.findFragmentByTag(frgTag)
        ?: Fragment.instantiate(ctx, kclass.java.name, createFrgArgs(*args).apply { putInt(FbConst.REQUEST_CODE, requestCode) })
    attach(frg, frgTag)
    stack.add(frg)
    val resultCommit = commit()
    if(resultCommit && topFrg is FbLifeCycle){
      topFrg.onFbStop()
    }
    return resultCommit
  }

  /**
   * 移除顶端Fragment.
   * @return Boolean
   */
  fun back(): Boolean {
    if (stack.size > 1) {
      ensureTransaction()
      fragmentTransaction?.setCustomAnimations(enter, exit, enter, exit)
      remove(stack.pollLast())

      val resultCommit = commit()
      val topFrg = topFragment
      if(resultCommit && topFrg is FbLifeCycle){
        topFrg.onFbResume()
      }
      return resultCommit
    }

    return false
  }

  /**
   * 移除顶端Fragment并返回result值，并触发onFragmentResult方法.
   * @param requestCode Int
   * @param resultCode Int
   * @param data Bundle
   * @return Boolean
   */
  fun back(requestCode: Int, resultCode: Int, data: Bundle?): Boolean {
    val commitResult = back()
    val frg = topFragment
    if (commitResult && frg is FbFragmentResult) {
      frg.onFragmentResult(requestCode, resultCode, data)
    }
    return commitResult
  }

  /**
   * 返回按钮点击事件.
   * @return Boolean
   */
  fun onBackPress(): Boolean {
    val frg = topFragment
    if (stack.size > 1 && frg is FbBackPress && frg.onBackPress()) {
      return true
    }
    return back()
  }

  /**
   * 移除Fragment.
   * @param frg Fragment?
   */
  private fun remove(frg: Fragment?) {
    if (frg != null && frg.isAdded) {
      ensureTransaction()
      fragmentTransaction?.remove(frg)
    }
  }

  @SuppressLint("CommitTransaction")
  private fun ensureTransaction() {
    if (fragmentTransaction == null) {
      fragmentTransaction = fragmentManager.beginTransaction()
    }
    handler.removeCallbacks(execPendingTransactions)
  }

  /**
   * 清空栈.
   */
  private fun clear() {
    val botFragment = this.botFragment
    stack.forEach {
      if (it == botFragment) {
        detach(it)
      } else {
        remove(it)
      }
    }

    stack.clear()
  }

  /**
   * 从Act移除Fragment。
   * @param frg Fragment?
   */
  private fun detach(frg: Fragment?) {
    if (frg != null && !frg.isDetached) {
      ensureTransaction()
      fragmentTransaction?.detach(frg)
    }
  }

  /**
   * 向Act添加Fragment.
   * @param frg Fragment?
   */
  private fun attach(frg: Fragment?, frgTag: String) {
    frg?.apply {
      if (isDetached) {
        ensureTransaction()
        fragmentTransaction?.attach(this)
      } else if (!isAdded) {
        ensureTransaction()
        fragmentTransaction?.add(containerId, this, frgTag)
      }
    }
  }

  /**
   * 提交事务.
   * @return Boolean
   */
  private fun commit(): Boolean {
    if (fragmentTransaction != null && fragmentTransaction?.isEmpty != true) {
      handler.removeCallbacks(execPendingTransactions)
      handler.post(execPendingTransactions)
      return true
    }
    return false
  }

  /**
   * 执行Fragment事务.
   * @return Boolean
   */
  private fun executePendingTransactions(): Boolean {
    if (fragmentTransaction != null && fragmentTransaction?.isEmpty != true) {
      handler.removeCallbacks(execPendingTransactions)
      fragmentTransaction?.commit()
      fragmentTransaction = null
      if (fragmentManager.executePendingTransactions()) {
        dispatchOnStackChangedEvent()
        return true
      }
    }

    return false
  }

  /**
   * 分发栈变化事件.
   */
  private fun dispatchOnStackChangedEvent() {
    if (stack.isNotEmpty()) {
      val size = stack.size
      val topFrg = stack.peekLast()
      stackChangedCallbacks.forEach { it.invoke(size, topFrg) }
    }
  }

  /**
   * 注册栈改变回调.
   * @param onStackChanged Function2<Int, Fragment, Unit>
   */
  fun registerStackChangedCallback(onStackChanged: (Int, Fragment) -> Unit) {
    if (!stackChangedCallbacks.contains(onStackChanged)) {
      stackChangedCallbacks.add(onStackChanged)
    }
  }

  /**
   * 反注册栈改变回调.
   * @param onStackChanged Function2<Int, Fragment, Unit>
   */
  fun unregisterStackChangedCallback(onStackChanged: (Int, Fragment) -> Unit) {
    stackChangedCallbacks.remove(onStackChanged)
  }


  /**
   * 创建Fragment传递参数.
   * @param args Array<out Pair<String, Any>>
   * @return Bundle
   */
  private fun createFrgArgs(vararg args: Pair<String, Any>): Bundle {
    val data = Bundle()
    args.forEach {
      val value = it.second
      when (value) {
        is Int -> data.putInt(it.first, value)
        is Long -> data.putLong(it.first, value)
        is CharSequence -> data.putCharSequence(it.first, value)
        is String -> data.putString(it.first, value)
        is Float -> data.putFloat(it.first, value)
        is Double -> data.putDouble(it.first, value)
        is Char -> data.putChar(it.first, value)
        is Short -> data.putShort(it.first, value)
        is Boolean -> data.putBoolean(it.first, value)
        is Serializable -> data.putSerializable(it.first, value)
        is Parcelable -> data.putParcelable(it.first, value)
        is Array<*> -> when {
          value.isArrayOf<CharSequence>() -> data.putCharSequenceArray(it.first, value as Array<CharSequence>?)
          value.isArrayOf<String>() -> data.putStringArray(it.first, value as Array<out String>?)
          value.isArrayOf<Parcelable>() -> data.putParcelableArray(it.first, value as Array<out Parcelable>?)
          else -> throw IllegalArgumentException("data extra ${it.first} has wrong type ${value.javaClass.name}")
        }
        is IntArray -> data.putIntArray(it.first, value)
        is LongArray -> data.putLongArray(it.first, value)
        is FloatArray -> data.putFloatArray(it.first, value)
        is DoubleArray -> data.putDoubleArray(it.first, value)
        is CharArray -> data.putCharArray(it.first, value)
        is ShortArray -> data.putShortArray(it.first, value)
        is BooleanArray -> data.putBooleanArray(it.first, value)
        else -> throw IllegalArgumentException("data extra ${it.first} has wrong type ${value.javaClass.name}")
      }
      return@forEach
    }
    return data
  }


  companion object {
    /** SuperFragmentManager collection. */
    private val stackMap = mutableMapOf<Any, FbFragmentStack>()

    fun with(activity: FragmentActivity, @IdRes frameLayoutId: Int): FbFragmentStack {
      return with(activity, activity, activity.supportFragmentManager, frameLayoutId)
    }

    fun with(fragment: Fragment, @IdRes frameLayoutId: Int): FbFragmentStack =
        with(fragment.activity!!, fragment, fragment.childFragmentManager, frameLayoutId)

    private fun with(context: Context, tag: Any, fragmentManager: FragmentManager, @IdRes frameLayoutId: Int): FbFragmentStack {
      val manager = getForTag(tag)
          ?: FbFragmentStack(context.applicationContext, fragmentManager, frameLayoutId)
      stackMap[tag] = manager
      return manager
    }

    fun get(fragment: Fragment): FbFragmentStack? =
        getForTag(fragment)

    fun get(activity: FragmentActivity): FbFragmentStack? =
        getForTag(activity)

    private fun getForTag(tag: Any): FbFragmentStack? = stackMap[tag]

    fun remove(fragment: Fragment) {
      removeForTag(fragment)
    }

    fun remove(activity: FragmentActivity) {
      removeForTag(activity)
    }

    private fun removeForTag(tag: Any) {
      stackMap.remove(tag)
    }
  }

}