package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.DiaryEntry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("GlucoDiary", appName)
  }

  @Test
  fun `verify diary entry total insulin calculation`() {
    val entry = DiaryEntry(
      date = "07/08/26",
      fastingSugar = "142mg",
      breakfastInsulinUnits = "7U",
      lunchInsulinUnits = "8U",
      dinnerInsulinUnits = "8U",
      nightInsulinUnits = "14U"
    )
    assertEquals(37.0, entry.totalInsulinUnits, 0.01)
    assertEquals(142, entry.getNumericSugar(entry.fastingSugar))
  }
}
