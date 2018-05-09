package com.tlz.fragmentbase_example

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun test() {
    val findValue = 20
    val rangeArray = arrayOf(2, 4, 10, 3, 45, 8, 4, 5, 11, 18, 8, 17, 12)
    // 找到最大值
    var maxValue = rangeArray[0]
    for (v in rangeArray) {
      if (v > maxValue) {
        maxValue = v
      }
    }
    val recordArray = IntArray(maxValue + 1)
    for (v in rangeArray) {
      recordArray[v] = recordArray[v] + 1
    }
    // 开始查找
    for (v in rangeArray) {
      val dist = findValue - v
      if (dist > 0 && ((dist == v && recordArray[dist] > 1) || (dist != v && recordArray[dist] > 0))) {
        println("已找到：$v, $dist")
      }
    }
  }
}
