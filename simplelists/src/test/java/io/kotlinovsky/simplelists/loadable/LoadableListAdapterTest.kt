package io.kotlinovsky.simplelists.loadable

import android.app.Activity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.clearInvocations
import org.mockito.Mockito.spy
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import io.kotlinovsky.simplelists.dependencies.TestListViewObject
import io.kotlinovsky.simplelists.loadable.dependencies.LoadableTestAdapter
import io.kotlinovsky.simplelists.model.BasicListItem
import org.junit.Assert.*

@RunWith(RobolectricTestRunner::class)
class LoadableListAdapterTest {

    @Test
    fun checkThatLoadersNotContainsInCount() {
        val adapter = getAdapter()

        adapter.isBottomLoadingEnabled = true
        adapter.isTopLoadingEnabled = true
        assertCount(adapter, 0, 2)

        adapter.isBottomLoadingEnabled = false
        adapter.isTopLoadingEnabled = true
        assertCount(adapter, 0, 1)

        adapter.isBottomLoadingEnabled = true
        adapter.isTopLoadingEnabled = false
        assertCount(adapter, 0, 1)
    }

    @Test
    fun testTopLoadingToggling() {
        val adapter = getAdapter()

        adapter.isTopLoadingEnabled = true
        verify(adapter).notifyItemRangeInserted(0, 1)
        assertEquals(LOADER_VIEW_TYPE, adapter.currentItems[0].viewType)

        adapter.isTopLoadingEnabled = false
        verify(adapter).notifyItemRangeRemoved(0, 1)
        assertTrue(adapter.currentItems.isEmpty())

        val first = BasicListItem(0, TestListViewObject("First"))
        val second = BasicListItem(0, TestListViewObject("Second"))
        val third = BasicListItem(0, TestListViewObject("Third"))

        adapter.addItems(0, listOf(first, second, third))
        clearInvocations(adapter)

        adapter.isTopLoadingEnabled = true
        verify(adapter).notifyItemRangeInserted(0, 1)
        assertEquals(LOADER_VIEW_TYPE, adapter.currentItems[0].viewType)
        assertEquals(first, adapter.currentItems[1])
        assertEquals(second, adapter.currentItems[2])
        assertEquals(third, adapter.currentItems[3])
        assertCount(adapter, 3, 4)

        adapter.isTopLoadingEnabled = false
        verify(adapter).notifyItemRangeRemoved(0, 1)
        assertEquals(first, adapter.currentItems[0])
        assertEquals(second, adapter.currentItems[1])
        assertEquals(third, adapter.currentItems[2])
        assertCount(adapter, 3, 3)
    }

    @Test
    fun testBottomLoadingToggling() {
        val adapter = getAdapter()

        adapter.isBottomLoadingEnabled = true
        verify(adapter).notifyItemRangeInserted(0, 1)
        assertEquals(LOADER_VIEW_TYPE, adapter.currentItems[0].viewType)

        adapter.isBottomLoadingEnabled = false
        verify(adapter).notifyItemRangeRemoved(0, 1)
        assertTrue(adapter.currentItems.isEmpty())

        val first = BasicListItem(0, TestListViewObject("First"))
        val second = BasicListItem(0, TestListViewObject("Second"))
        val third = BasicListItem(0, TestListViewObject("Third"))

        adapter.addItems(0, listOf(first, second, third))
        clearInvocations(adapter)

        adapter.isBottomLoadingEnabled = true
        verify(adapter).notifyItemRangeInserted(3, 1)
        assertEquals(first, adapter.currentItems[0])
        assertEquals(second, adapter.currentItems[1])
        assertEquals(third, adapter.currentItems[2])
        assertEquals(LOADER_VIEW_TYPE, adapter.currentItems[3].viewType)
        assertCount(adapter, 3, 4)

        adapter.isBottomLoadingEnabled = false
        verify(adapter).notifyItemRangeRemoved(3, 1)
        assertEquals(first, adapter.currentItems[0])
        assertEquals(second, adapter.currentItems[1])
        assertEquals(third, adapter.currentItems[2])
        assertCount(adapter, 3, 3)
    }

    @Test
    fun testItemsAddingWhenEnabledTopLoading() {
        val adapter = getAdapter()

        adapter.isTopLoadingEnabled = true
        clearInvocations(adapter)

        val first = BasicListItem(0, TestListViewObject("First"))
        val second = BasicListItem(0, TestListViewObject("Second"))
        val third = BasicListItem(0, TestListViewObject("Third"))

        adapter.addItems(0, listOf(first, second, third))
        verify(adapter).notifyItemRangeInserted(1, 3)
        assertEquals(LOADER_VIEW_TYPE, adapter.currentItems[0].viewType)
        assertEquals(first, adapter.currentItems[1])
        assertEquals(second, adapter.currentItems[2])
        assertEquals(third, adapter.currentItems[3])
        assertCount(adapter, 3, 4)

        val fourth = BasicListItem(0, TestListViewObject("Fourth"))

        adapter.addItems(adapter.getItemsCount(), listOf(fourth))
        verify(adapter).notifyItemRangeInserted(4, 1)
        assertEquals(LOADER_VIEW_TYPE, adapter.currentItems[0].viewType)
        assertEquals(first, adapter.currentItems[1])
        assertEquals(second, adapter.currentItems[2])
        assertEquals(third, adapter.currentItems[3])
        assertEquals(fourth, adapter.currentItems[4])
        assertCount(adapter, 4, 5)
    }

    @Test
    fun testItemsAddingWhenEnabledBottomLoading() {
        val adapter = getAdapter()

        adapter.isBottomLoadingEnabled = true
        clearInvocations(adapter)

        val first = BasicListItem(0, TestListViewObject("First"))
        val second = BasicListItem(0, TestListViewObject("Second"))

        adapter.addItems(0, listOf(first, second))
        verify(adapter).notifyItemRangeInserted(0, 2)
        assertEquals(first, adapter.currentItems[0])
        assertEquals(second, adapter.currentItems[1])
        assertEquals(LOADER_VIEW_TYPE, adapter.currentItems[2].viewType)
        assertCount(adapter, 2, 3)

        val third = BasicListItem(0, TestListViewObject("Third"))
        val fourth = BasicListItem(0, TestListViewObject("Fourth"))

        adapter.addItems(adapter.getItemsCount(), listOf(third, fourth))
        verify(adapter).notifyItemRangeInserted(2, 2)
        assertEquals(first, adapter.currentItems[0])
        assertEquals(second, adapter.currentItems[1])
        assertEquals(third, adapter.currentItems[2])
        assertEquals(fourth, adapter.currentItems[3])
        assertEquals(LOADER_VIEW_TYPE, adapter.currentItems[4].viewType)
        assertCount(adapter, 4, 5)
    }

    @Test
    fun testAddingWhenEnabledAllLoadings() {
        val adapter = getAdapter()

        adapter.isTopLoadingEnabled = true
        adapter.isBottomLoadingEnabled = true
        clearInvocations(adapter)

        val first = BasicListItem(0, TestListViewObject("First"))
        val second = BasicListItem(0, TestListViewObject("Second"))

        adapter.addItems(0, listOf(first, second))
        verify(adapter).notifyItemRangeInserted(1, 2)
        assertEquals(LOADER_VIEW_TYPE, adapter.currentItems[0].viewType)
        assertEquals(first, adapter.currentItems[1])
        assertEquals(second, adapter.currentItems[2])
        assertEquals(LOADER_VIEW_TYPE, adapter.currentItems[3].viewType)
        assertCount(adapter, 2, 4)

        val third = BasicListItem(0, TestListViewObject("Third"))
        val fourth = BasicListItem(0, TestListViewObject("Fourth"))

        adapter.addItems(adapter.getItemsCount(), listOf(third, fourth))
        verify(adapter).notifyItemRangeInserted(3, 2)
        assertEquals(LOADER_VIEW_TYPE, adapter.currentItems[0].viewType)
        assertEquals(first, adapter.currentItems[1])
        assertEquals(second, adapter.currentItems[2])
        assertEquals(third, adapter.currentItems[3])
        assertEquals(fourth, adapter.currentItems[4])
        assertEquals(LOADER_VIEW_TYPE, adapter.currentItems[5].viewType)
        assertCount(adapter, 4, 6)
    }

    @Test
    fun testItemsRemovingWhenEnabledTopLoading() {
        val adapter = getAdapter()
        val first = BasicListItem(0, TestListViewObject("First"))
        val second = BasicListItem(0, TestListViewObject("Second"))
        val third = BasicListItem(0, TestListViewObject("Third"))

        adapter.addItems(0, listOf(first, second, third))
        adapter.isTopLoadingEnabled = true
        clearInvocations(adapter)

        adapter.removeItems(1, 2)
        verify(adapter).notifyItemRangeRemoved(2, 2)
        assertEquals(LOADER_VIEW_TYPE, adapter.currentItems[0].viewType)
        assertEquals(first, adapter.currentItems[1])
        assertCount(adapter, 1, 2)

        adapter.removeItems(0, 1)
        verify(adapter).notifyItemRangeRemoved(1, 1)
        assertEquals(LOADER_VIEW_TYPE, adapter.currentItems[0].viewType)
        assertCount(adapter, 0, 1)
    }

    @Test
    fun testItemsRemovingWhenEnabledBottomLoading() {
        val adapter = getAdapter()
        val first = BasicListItem(0, TestListViewObject("First"))
        val second = BasicListItem(0, TestListViewObject("Second"))
        val third = BasicListItem(0, TestListViewObject("Third"))
        val fourth = BasicListItem(0, TestListViewObject("Fourth"))

        adapter.isBottomLoadingEnabled = true
        adapter.addItems(0, listOf(first, second, third, fourth))
        clearInvocations(adapter)

        adapter.removeItems(2, 2)
        verify(adapter).notifyItemRangeRemoved(2, 2)
        assertEquals(first, adapter.currentItems[0])
        assertEquals(second, adapter.currentItems[1])
        assertEquals(LOADER_VIEW_TYPE, adapter.currentItems[2].viewType)
        assertCount(adapter, 2, 3)

        adapter.removeItems(0, 2)
        verify(adapter).notifyItemRangeRemoved(0, 2)
        assertEquals(LOADER_VIEW_TYPE, adapter.currentItems[0].viewType)
        assertCount(adapter, 0, 1)
    }

    @Test
    fun testItemsRemovingWhenEnabledAllLoadings() {
        val adapter = getAdapter()
        val first = BasicListItem(0, TestListViewObject("First"))
        val second = BasicListItem(0, TestListViewObject("Second"))
        val third = BasicListItem(0, TestListViewObject("Third"))
        val fourth = BasicListItem(0, TestListViewObject("Fourth"))

        adapter.addItems(0, listOf(first, second, third, fourth))
        adapter.isTopLoadingEnabled = true
        adapter.isBottomLoadingEnabled = true
        clearInvocations(adapter)

        adapter.removeItems(2, 2)
        verify(adapter).notifyItemRangeRemoved(3, 2)
        assertEquals(LOADER_VIEW_TYPE, adapter.currentItems[0].viewType)
        assertEquals(first, adapter.currentItems[1])
        assertEquals(second, adapter.currentItems[2])
        assertEquals(LOADER_VIEW_TYPE, adapter.currentItems[3].viewType)
        assertCount(adapter, 2, 4)

        adapter.removeItems(0, 2)
        verify(adapter).notifyItemRangeRemoved(1, 2)
        assertEquals(LOADER_VIEW_TYPE, adapter.currentItems[0].viewType)
        assertEquals(LOADER_VIEW_TYPE, adapter.currentItems[1].viewType)
        assertCount(adapter, 0, 2)
    }

    @Test
    fun testItemsGettingWhenEnabledTopLoading() {
        val adapter = getAdapter()
        val first = BasicListItem(0, TestListViewObject("First"))
        val second = BasicListItem(0, TestListViewObject("Second"))
        val third = BasicListItem(0, TestListViewObject("Third"))

        adapter.addItems(0, listOf(first, second, third))
        adapter.isTopLoadingEnabled = true
        clearInvocations(adapter)

        val firstItems = adapter.getItems(1, 2, true)
        assertEquals(second, firstItems[0])
        assertEquals(third, firstItems[1])

        val secondItems = adapter.getItems(0, 1, true)
        assertEquals(first, secondItems[0])
    }

    @Test
    fun testItemsGettingWhenEnabledBottomLoading() {
        val adapter = getAdapter()
        val first = BasicListItem(0, TestListViewObject("First"))
        val second = BasicListItem(0, TestListViewObject("Second"))
        val third = BasicListItem(0, TestListViewObject("Third"))
        val fourth = BasicListItem(0, TestListViewObject("Fourth"))

        adapter.isBottomLoadingEnabled = true
        adapter.addItems(0, listOf(first, second, third, fourth))
        clearInvocations(adapter)

        val firstItems = adapter.getItems(2, 2, true)
        assertEquals(third, firstItems[0])
        assertEquals(fourth, firstItems[1])

        val secondItems = adapter.getItems(0, 2, true)
        assertEquals(first, secondItems[0])
        assertEquals(second, secondItems[1])
    }

    @Test
    fun testItemsGettingWhenEnabledAllLoadings() {
        val adapter = getAdapter()
        val first = BasicListItem(0, TestListViewObject("First"))
        val second = BasicListItem(0, TestListViewObject("Second"))
        val third = BasicListItem(0, TestListViewObject("Third"))
        val fourth = BasicListItem(0, TestListViewObject("Fourth"))

        adapter.isBottomLoadingEnabled = true
        adapter.isTopLoadingEnabled = true
        adapter.addItems(0, listOf(first, second, third, fourth))
        clearInvocations(adapter)

        val firstItems = adapter.getItems(2, 2, true)
        assertEquals(third, firstItems[0])
        assertEquals(fourth, firstItems[1])

        val secondItems = adapter.getItems(0, 2, true)
        assertEquals(first, secondItems[0])
        assertEquals(second, secondItems[1])
    }

    @Test
    fun testItemUpdatingWhenEnabledTopLoading() {
        val adapter = getAdapter()
        val first = BasicListItem(0, TestListViewObject("First"))

        adapter.addItems(0, listOf(first))
        adapter.isTopLoadingEnabled = true
        clearInvocations(adapter)

        val firstUpdated = BasicListItem(0, TestListViewObject("First Updated"))
        adapter.updateItem(firstUpdated, 0)
        verify(adapter).notifyItemChanged(1, null)
        assertEquals(LOADER_VIEW_TYPE, adapter.currentItems[0].viewType)
        assertEquals(firstUpdated, adapter.currentItems[1])
    }

    @Test
    fun testItemUpdatingWhenEnabledBottomLoading() {
        val adapter = getAdapter()
        val first = BasicListItem(0, TestListViewObject("First"))

        adapter.addItems(0, listOf(first))
        adapter.isBottomLoadingEnabled = true
        clearInvocations(adapter)

        val firstUpdated = BasicListItem(0, TestListViewObject("First Updated"))
        adapter.updateItem(firstUpdated, 0)
        verify(adapter).notifyItemChanged(0, null)
        assertEquals(firstUpdated, adapter.currentItems[0])
        assertEquals(LOADER_VIEW_TYPE, adapter.currentItems[1].viewType)
    }

    @Test
    fun testItemUpdatingWhenAllLoadingsEnabled() {
        val adapter = getAdapter()
        val first = BasicListItem(0, TestListViewObject("First"))

        adapter.addItems(0, listOf(first))
        adapter.isTopLoadingEnabled = true
        adapter.isBottomLoadingEnabled = true
        clearInvocations(adapter)

        val firstUpdated = BasicListItem(0, TestListViewObject("First Updated"))
        adapter.updateItem(firstUpdated, 0)
        verify(adapter).notifyItemChanged(1, null)
        assertEquals(LOADER_VIEW_TYPE, adapter.currentItems[0].viewType)
        assertEquals(LOADER_VIEW_TYPE, adapter.currentItems[2].viewType)
        assertEquals(firstUpdated, adapter.currentItems[1])
    }

    @Test
    fun testItemsChangingWhenEnabledTopLoading() {
        val adapter = getAdapter()

        adapter.isTopLoadingEnabled = true
        clearInvocations(adapter)

        val first = BasicListItem(0, TestListViewObject("First"))
        val second = BasicListItem(0, TestListViewObject("Second"))
        val third = BasicListItem(0, TestListViewObject("Third"))

        adapter.changeItems(listOf(first, second, third), false)
        verify(adapter).notifyItemRangeInserted(1, 3)
        assertEquals(LOADER_VIEW_TYPE, adapter.currentItems[0].viewType)
        assertEquals(first, adapter.currentItems[1])
        assertEquals(second, adapter.currentItems[2])
        assertEquals(third, adapter.currentItems[3])
        assertCount(adapter, 3, 4)
    }

    @Test
    fun testItemsChangingWhenEnabledBottomLoading() {
        val adapter = getAdapter()

        adapter.isBottomLoadingEnabled = true
        clearInvocations(adapter)

        val first = BasicListItem(0, TestListViewObject("First"))
        val second = BasicListItem(0, TestListViewObject("Second"))
        val third = BasicListItem(0, TestListViewObject("Third"))

        adapter.changeItems(listOf(first, second, third), false)
        verify(adapter).notifyItemRangeInserted(0, 3)
        assertEquals(first, adapter.currentItems[0])
        assertEquals(second, adapter.currentItems[1])
        assertEquals(third, adapter.currentItems[2])
        assertEquals(LOADER_VIEW_TYPE, adapter.currentItems[3].viewType)
        assertCount(adapter, 3, 4)
    }

    @Test
    fun testItemsChangingWhenEnabledAllLoadings() {
        val adapter = getAdapter()

        adapter.isTopLoadingEnabled = true
        adapter.isBottomLoadingEnabled = true
        clearInvocations(adapter)

        val first = BasicListItem(0, TestListViewObject("First"))
        val second = BasicListItem(0, TestListViewObject("Second"))
        val third = BasicListItem(0, TestListViewObject("Third"))

        adapter.changeItems(listOf(first, second, third), false)
        verify(adapter).notifyItemRangeInserted(1, 3)
        assertEquals(LOADER_VIEW_TYPE, adapter.currentItems[0].viewType)
        assertEquals(LOADER_VIEW_TYPE, adapter.currentItems[4].viewType)
        assertEquals(first, adapter.currentItems[1])
        assertEquals(second, adapter.currentItems[2])
        assertEquals(third, adapter.currentItems[3])
        assertCount(adapter, 3, 5)
    }

    @Test
    fun testItemsChangingWhenLoadingsNotEnabled() {
        val adapter = getAdapter()
        val first = BasicListItem(0, TestListViewObject("First"))
        val second = BasicListItem(0, TestListViewObject("Second"))
        val third = BasicListItem(0, TestListViewObject("Third"))

        adapter.changeItems(listOf(first, second, third), false)
        verify(adapter).notifyItemRangeInserted(0, 3)
        assertEquals(first, adapter.currentItems[0])
        assertEquals(second, adapter.currentItems[1])
        assertEquals(third, adapter.currentItems[2])
        assertCount(adapter, 3, 3)
    }

    @Test
    fun testViewTypesLogging() {
        val adapter = getAdapter()
        val firstOfFirst = BasicListItem(0, TestListViewObject("First 1"))
        val secondOfFirst = BasicListItem(0, TestListViewObject("First 2"))
        val firstOfSecond = BasicListItem(1, TestListViewObject("Second 1"))
        val secondOfSecond = BasicListItem(1, TestListViewObject("Second 2"))
        val firstOfThird = BasicListItem(2, TestListViewObject("Third 1"))
        val secondOfThird = BasicListItem(2, TestListViewObject("Third 2"))

        adapter.addItems(0, listOf(firstOfFirst, secondOfFirst))
        adapter.addItems(0, listOf(firstOfSecond, secondOfSecond))
        adapter.addItems(0, listOf(firstOfThird, secondOfThird))

        assertEquals(2, adapter.countByTypes[0])
        assertEquals(2, adapter.countByTypes[1])
        assertEquals(2, adapter.countByTypes[2])

        adapter.removeItems(0, 2)
        assertFalse(adapter.countByTypes.containsKey(2))
        assertEquals(2, adapter.countByTypes[0])
        assertEquals(2, adapter.countByTypes[1])

        adapter.removeItems(0, 2)
        assertFalse(adapter.countByTypes.containsKey(2))
        assertFalse(adapter.countByTypes.containsKey(1))
        assertEquals(2, adapter.countByTypes[0])

        adapter.removeItems(0, 2)
        assertFalse(adapter.countByTypes.containsKey(2))
        assertFalse(adapter.countByTypes.containsKey(1))
        assertFalse(adapter.countByTypes.containsKey(0))

        val list = listOf(
            firstOfFirst,
            secondOfFirst,
            firstOfSecond
        )

        adapter.changeItems(list, false)
        assertFalse(adapter.countByTypes.containsKey(2))
        assertEquals(2, adapter.countByTypes[0])
        assertEquals(1, adapter.countByTypes[1])
    }

    private fun assertCount(list: LoadableTestAdapter, count: Int, countWithoutExcluding: Int) {
        assertEquals(count, list.getItemsCount())
        assertEquals(countWithoutExcluding, list.currentItems.size)
    }

    private fun getAdapter(): LoadableTestAdapter {
        val controller = Robolectric.buildActivity(Activity::class.java)
        val activity = controller.get()
        val list = RecyclerView(activity)
        val adapter = spy(LoadableTestAdapter())

        list.adapter = adapter
        list.layoutManager = LinearLayoutManager(activity)

        activity.setContentView(list)
        controller.create()
        controller.visible()
        clearInvocations(list.adapter)

        return adapter
    }
}