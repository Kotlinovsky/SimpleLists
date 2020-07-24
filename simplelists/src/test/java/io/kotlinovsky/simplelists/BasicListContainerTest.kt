package io.kotlinovsky.simplelists

import android.app.Activity
import android.view.View
import androidx.core.view.children
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import io.kotlinovsky.simplelists.loadable.dependencies.LoadableTestAdapter

@RunWith(RobolectricTestRunner::class)
class BasicListContainerTest {

    @Test
    fun testLoadingToggling() {
        val controller = Robolectric.buildActivity(Activity::class.java)
        val activity = controller.get()
        val list = RecyclerView(activity)
        val basicListContainer = BasicListContainer(activity)

        list.adapter = LoadableTestAdapter()
        list.layoutManager = LinearLayoutManager(activity)
        basicListContainer.addView(list)
        activity.setContentView(basicListContainer)
        controller.setup()

        basicListContainer.isLoadingEnabled = true
        val loaderView = basicListContainer.children.find { it != list }!!

        assertEquals(2, basicListContainer.childCount)
        assertEquals(View.GONE, list.visibility)
        assertEquals(View.VISIBLE, loaderView.visibility)

        basicListContainer.isLoadingEnabled = false
        assertEquals(2, basicListContainer.childCount)
        assertEquals(View.VISIBLE, list.visibility)
        assertEquals(View.GONE, loaderView.visibility)
    }
}