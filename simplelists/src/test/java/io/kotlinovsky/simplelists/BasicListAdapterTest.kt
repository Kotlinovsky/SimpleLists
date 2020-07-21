package io.kotlinovsky.simplelists

import android.app.Activity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import io.kotlinovsky.simplelists.dependencies.TestListAdapter
import io.kotlinovsky.simplelists.dependencies.TestListViewObject
import io.kotlinovsky.simplelists.model.BasicListItem

val CHANGE_PAYLOADS = HashMap<BasicListItem<*>, List<Any>?>()

@RunWith(RobolectricTestRunner::class)
class BasicListAdapterTest {

    @After
    fun tearDown() {
        CHANGE_PAYLOADS.clear()
    }

    @Test
    fun testItemsAdding() {
        val adapter = getAdapter()
        val first = BasicListItem(0, TestListViewObject("First"))
        val second = BasicListItem(0, TestListViewObject("Second"))
        val third = BasicListItem(0, TestListViewObject("Third"))

        adapter.addItems(0, listOf(third))
        verify(adapter).notifyItemRangeInserted(0, 1)
        assertEquals(third, adapter.currentItems[0])
        assertCount(adapter, 1)

        adapter.addItems(0, listOf(first, second))
        verify(adapter).notifyItemRangeInserted(0, 2)
        assertEquals(first, adapter.currentItems[0])
        assertEquals(second, adapter.currentItems[1])
        assertEquals(third, adapter.currentItems[2])
        assertCount(adapter, 3)
    }

    @Test
    fun testItemsRemoving() {
        val adapter = getAdapter()
        val first = BasicListItem(0, TestListViewObject("First"))
        val second = BasicListItem(0, TestListViewObject("Second"))
        val third = BasicListItem(0, TestListViewObject("Third"))

        adapter.addItems(0, listOf(first, second, third))
        clearInvocations(adapter)

        adapter.removeItems(1, 2)
        verify(adapter).notifyItemRangeRemoved(1, 2)
        assertEquals(first, adapter.currentItems[0])
        assertCount(adapter, 1)

        adapter.removeItems(0, 1)
        verify(adapter).notifyItemRangeRemoved(0, 1)
        assertCount(adapter, 0)
    }

    @Test
    fun testItemsGettingWithoutCopying() {
        val list = getAdapter()
        val first = BasicListItem(0, TestListViewObject("First"))
        val second = BasicListItem(0, TestListViewObject("Second"))
        val third = BasicListItem(0, TestListViewObject("Third"))

        list.addItems(0, listOf(first, second, third))

        val firstItems = list.getItems(1, 2, false)
        assertArrayEquals(listOf(second, third).toTypedArray(), firstItems.toTypedArray())
        assertNotEquals(firstItems, list.currentItems)

        val secondItems = list.getItems(0, 1, false)
        assertArrayEquals(listOf(first).toTypedArray(), secondItems.toTypedArray())
        assertNotEquals(secondItems, list.currentItems)
    }

    @Test
    fun testItemsGettingWithCopying() {
        val list = getAdapter()
        val first = BasicListItem(0, TestListViewObject("First"))
        val second = BasicListItem(0, TestListViewObject("Second"))
        val third = BasicListItem(0, TestListViewObject("Third"))

        list.addItems(0, listOf(first, second, third))

        val firstItems = list.getItems(1, 2, true)
        assertArrayEquals(listOf(second, third).toTypedArray(), firstItems.toTypedArray())
        assertNotEquals(firstItems, list.currentItems)
        assertCount(list, 3)

        val secondItems = list.getItems(0, 1, true)
        assertArrayEquals(listOf(first).toTypedArray(), secondItems.toTypedArray())
        assertNotEquals(secondItems, list.currentItems)
        assertCount(list, 3)
    }

    @Test
    fun testUpdating() {
        val adapter = getAdapter()
        val first = BasicListItem(0, TestListViewObject("First"))

        adapter.addItems(0, listOf(first))
        clearInvocations(adapter)

        val firstUpdated = BasicListItem(0, TestListViewObject("First updated"))
        first.viewAnimationState = "Hello, Animation!"

        adapter.updateItem(firstUpdated, 0)
        verify(adapter)!!.notifyItemChanged(0, null)
        assertEquals(firstUpdated, adapter.currentItems[0])
        assertEquals("Hello, Animation!", firstUpdated.viewAnimationState)
    }

    @Test
    fun testUpdatingWithChangePayload() {
        val list = getAdapter()
        val first = BasicListItem(0, TestListViewObject("First"))
        val changePayload = listOf("Hello, tester!")
        val firstUpdated = BasicListItem(0, TestListViewObject("First updated", changePayload))

        list.addItems(0, listOf(first))
        list.updateItem(firstUpdated, 0)

        assertNotNull(CHANGE_PAYLOADS[firstUpdated])
        assertTrue(CHANGE_PAYLOADS[firstUpdated]!!.containsAll(changePayload))
    }

    @Test
    fun testItemsChanging() {
        val adapter = getAdapter()
        val first = BasicListItem(0, TestListViewObject("First")).apply { viewAnimationState = "First animation" }
        val second = BasicListItem(0, TestListViewObject("Second")).apply { viewAnimationState = "Second animation" }
        val third = BasicListItem(0, TestListViewObject("Third")).apply { viewAnimationState = "Third animation" }

        adapter.addItems(0, listOf(first, second, third))
        clearInvocations(adapter)

        val newFirst = BasicListItem(0, TestListViewObject("Third"))
        val newSecond = BasicListItem(0, TestListViewObject("Second"))
        val newThird = BasicListItem(0, TestListViewObject("First"))

        adapter.changeItems(listOf(newFirst, newSecond, newThird), false)
        verify(adapter).notifyItemRangeInserted(3, 2)
        verify(adapter).notifyItemRangeRemoved(0, 2)
        assertEquals("Third animation", newFirst.viewAnimationState)
        assertEquals("Second animation", newSecond.viewAnimationState)
        assertEquals("First animation", newThird.viewAnimationState)
        assertEquals(newFirst, adapter.currentItems[0])
        assertEquals(newSecond, adapter.currentItems[1])
        assertEquals(newThird, adapter.currentItems[2])
    }

    @Test
    fun testItemsChangingWithMovingDetection() {
        val adapter = getAdapter()
        val first = BasicListItem(0, TestListViewObject("First")).apply { viewAnimationState = "First animation" }
        val second = BasicListItem(0, TestListViewObject("Second")).apply { viewAnimationState = "Second animation" }
        val third = BasicListItem(0, TestListViewObject("Third")).apply { viewAnimationState = "Third animation" }

        adapter.addItems(0, listOf(first, second, third))
        clearInvocations(adapter)

        val newFirst = BasicListItem(0, TestListViewObject("Third"))
        val newSecond = BasicListItem(0, TestListViewObject("Second"))
        val newThird = BasicListItem(0, TestListViewObject("First"))

        adapter.changeItems(listOf(newFirst, newSecond, newThird), true)
        verify(adapter).notifyItemMoved(1, 2)
        verify(adapter).notifyItemMoved(0, 2)
        assertEquals("Third animation", newFirst.viewAnimationState)
        assertEquals("Second animation", newSecond.viewAnimationState)
        assertEquals("First animation", newThird.viewAnimationState)
        assertEquals(newFirst, adapter.currentItems[0])
        assertEquals(newSecond, adapter.currentItems[1])
        assertEquals(newThird, adapter.currentItems[2])
    }

    @Test
    fun testItemsChangingWhenReceivedNull() {
        val adapter = getAdapter()
        val first = BasicListItem(0, TestListViewObject("First"))
        val second = BasicListItem(0, TestListViewObject("Second"))
        val third = BasicListItem(0, TestListViewObject("Third"))

        adapter.addItems(0, listOf(first, second, third))
        clearInvocations(adapter)

        adapter.changeItems(null, true)
        verify(adapter).notifyItemRangeRemoved(0, 3)
        assertCount(adapter, 0)
    }

    @Test
    fun testItemsChangingWithPayload() {
        val adapter = getAdapter()
        val first = BasicListItem(0, TestListViewObject("First"))
        val second = BasicListItem(0, TestListViewObject("Second"))
        val third = BasicListItem(0, TestListViewObject("Third"))

        adapter.addItems(0, listOf(first, second, third))
        clearInvocations(adapter)

        val newSecond = BasicListItem(0, TestListViewObject("Second Updated", listOf("Second Change"), "Second"))
        val newThird = BasicListItem(0, TestListViewObject("Third Updated", listOf("Third Change"), "Third"))

        adapter.changeItems(listOf(first, newSecond, newThird), true)
        assertNull(CHANGE_PAYLOADS[first])
        assertNotNull(CHANGE_PAYLOADS[newSecond])
        assertNotNull(CHANGE_PAYLOADS[newThird])
        assertTrue(CHANGE_PAYLOADS[newSecond]!!.contains("Second Change"))
        assertTrue(CHANGE_PAYLOADS[newThird]!!.contains("Third Change"))
    }

    private fun assertCount(list: BasicListAdapter, count: Int) {
        assertEquals(count, list.getItemsCount())
        assertEquals(count, list.currentItems.size)
    }

    private fun getAdapter(): TestListAdapter {
        val controller = Robolectric.buildActivity(Activity::class.java)
        val activity = controller.get()
        val list = RecyclerView(activity)
        val adapter = spy(TestListAdapter())

        list.adapter = adapter
        list.layoutManager = LinearLayoutManager(activity)

        activity.setContentView(list)
        controller.create()
        controller.visible()
        clearInvocations(list.adapter)

        return adapter
    }
}