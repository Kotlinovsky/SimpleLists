package io.kotlinovsky.simplelists.sectioned

import android.app.Activity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nhaarman.mockitokotlin2.clearInvocations
import com.nhaarman.mockitokotlin2.verify
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import io.kotlinovsky.simplelists.dependencies.TestListViewObject
import io.kotlinovsky.simplelists.loadable.LOADER_VIEW_TYPE
import io.kotlinovsky.simplelists.model.BasicListItem
import io.kotlinovsky.simplelists.sectioned.dependencies.SectionedListTestAdapter

@RunWith(RobolectricTestRunner::class)
class SectionedListTest {

    @Test
    fun testSectionRemoving() {
        val adapter = getAdapter()
        val firstOfFirstSection = BasicListItem(0, TestListViewObject("0 0"))
        val secondOfFirstSection = BasicListItem(0, TestListViewObject("0 1"))
        val firstOfSecondSection = BasicListItem(0, TestListViewObject("1 0"))
        val secondOfSecondSection = BasicListItem(0, TestListViewObject("1 1"))

        adapter.addSection(0)
        adapter.addSection(1)
        adapter.addItemsToSection(0, 0, listOf(firstOfFirstSection, secondOfFirstSection))
        adapter.addItemsToSection(1, 0, listOf(firstOfSecondSection, secondOfSecondSection))
        clearInvocations(adapter)

        adapter.removeSection(1)
        verify(adapter).notifyItemRangeRemoved(2, 2)
        assertEquals(firstOfFirstSection, adapter.currentItems[0])
        assertEquals(secondOfFirstSection, adapter.currentItems[1])
        assertSection(adapter, 0, 0, 2, 2)
        assertCount(adapter, 2)

        adapter.removeSection(0)
        verify(adapter).notifyItemRangeRemoved(0, 2)
        assertCount(adapter, 0)
    }

    @Test
    fun checkThatCallbackCalledWhenRemovedSectionWithEnabledLoading() {
        val adapter = getAdapter()
        var idFromCallback = -1
        var count = 0

        adapter.forceLoadingDisabledCallback = {
            idFromCallback = it
            count++
        }

        adapter.addSection(0)
        adapter.toggleLoading(0, true)
        adapter.removeSection(0)

        assertEquals(0, idFromCallback)
        assertEquals(1, count)
    }

    @Test
    fun testItemsToSectionAdding() {
        val adapter = getAdapter()
        val firstOfFirstSection = BasicListItem(0, TestListViewObject("0 0"))
        val secondOfFirstSection = BasicListItem(0, TestListViewObject("0 1"))
        val firstOfSecondSection = BasicListItem(0, TestListViewObject("1 0"))
        val secondOfSecondSection = BasicListItem(0, TestListViewObject("1 1"))
        val firstOfThirdSection = BasicListItem(0, TestListViewObject("2 0"))
        val secondOfThirdSection = BasicListItem(0, TestListViewObject("2 1"))

        adapter.addSection(0)
        adapter.addSection(1)
        adapter.addSection(2)
        clearInvocations(adapter)

        adapter.addItemsToSection(1, 0, listOf(firstOfSecondSection, secondOfSecondSection))
        verify(adapter).notifyItemRangeInserted(0, 2)
        clearInvocations(adapter)
        assertEquals(firstOfSecondSection, adapter.currentItems[0])
        assertEquals(secondOfSecondSection, adapter.currentItems[1])
        assertSection(adapter, 1, 0, 2, 2)
        assertCount(adapter, 2)

        adapter.addItemsToSection(0, 0, listOf(firstOfFirstSection, secondOfFirstSection))
        assertEquals(firstOfFirstSection, adapter.currentItems[0])
        assertEquals(secondOfFirstSection, adapter.currentItems[1])
        assertEquals(firstOfSecondSection, adapter.currentItems[2])
        assertEquals(secondOfSecondSection, adapter.currentItems[3])
        verify(adapter).notifyItemRangeInserted(0, 2)
        assertSection(adapter, 0, 0, 2, 2)
        assertSection(adapter, 1, 2, 2, 2)
        assertCount(adapter, 4)

        adapter.addItemsToSection(2, 0, listOf(firstOfThirdSection, secondOfThirdSection))
        verify(adapter).notifyItemRangeInserted(4, 2)
        assertEquals(firstOfFirstSection, adapter.currentItems[0])
        assertEquals(secondOfFirstSection, adapter.currentItems[1])
        assertEquals(firstOfSecondSection, adapter.currentItems[2])
        assertEquals(secondOfSecondSection, adapter.currentItems[3])
        assertEquals(firstOfThirdSection, adapter.currentItems[4])
        assertEquals(secondOfThirdSection, adapter.currentItems[5])
        assertSection(adapter, 0, 0, 2, 2)
        assertSection(adapter, 1, 2, 2, 2)
        assertSection(adapter, 2, 4, 2, 2)
        assertCount(adapter, 6)
    }

    @Test
    fun testItemsFromSectionRemoving() {
        val adapter = getAdapter()
        val firstOfFirstSection = BasicListItem(0, TestListViewObject("0 0"))
        val secondOfFirstSection = BasicListItem(0, TestListViewObject("0 1"))
        val thirdOfFirstSection = BasicListItem(0, TestListViewObject("0 2"))
        val firstOfSecondSection = BasicListItem(0, TestListViewObject("1 0"))
        val secondOfSecondSection = BasicListItem(0, TestListViewObject("1 1"))
        val thirdOfSecondSection = BasicListItem(0, TestListViewObject("1 2"))
        val firstOfThirdSection = BasicListItem(0, TestListViewObject("2 0"))
        val secondOfThirdSection = BasicListItem(0, TestListViewObject("2 1"))
        val thirdOfThirdSection = BasicListItem(0, TestListViewObject("2 2"))

        adapter.addSection(0)
        adapter.addSection(1)
        adapter.addSection(2)
        adapter.addItemsToSection(0, 0, listOf(firstOfFirstSection, secondOfFirstSection, thirdOfFirstSection))
        adapter.addItemsToSection(1, 0, listOf(firstOfSecondSection, secondOfSecondSection, thirdOfSecondSection))
        adapter.addItemsToSection(2, 0, listOf(firstOfThirdSection, secondOfThirdSection, thirdOfThirdSection))
        clearInvocations(adapter)

        // Removing from end

        adapter.removeItemsFromSection(2, 2, 1)
        verify(adapter).notifyItemRangeRemoved(8, 1)
        clearInvocations(adapter)
        assertEquals(firstOfFirstSection, adapter.currentItems[0])
        assertEquals(secondOfFirstSection, adapter.currentItems[1])
        assertEquals(thirdOfFirstSection, adapter.currentItems[2])
        assertEquals(firstOfSecondSection, adapter.currentItems[3])
        assertEquals(secondOfSecondSection, adapter.currentItems[4])
        assertEquals(thirdOfSecondSection, adapter.currentItems[5])
        assertEquals(firstOfThirdSection, adapter.currentItems[6])
        assertEquals(secondOfThirdSection, adapter.currentItems[7])
        assertSection(adapter, 0, 0, 3, 3)
        assertSection(adapter, 1, 3, 3, 3)
        assertSection(adapter, 2, 6, 2, 2)
        assertCount(adapter, 8)

        adapter.removeItemsFromSection(2, 0, 2)
        verify(adapter).notifyItemRangeRemoved(6, 2)
        clearInvocations(adapter)
        assertEquals(firstOfFirstSection, adapter.currentItems[0])
        assertEquals(secondOfFirstSection, adapter.currentItems[1])
        assertEquals(thirdOfFirstSection, adapter.currentItems[2])
        assertEquals(firstOfSecondSection, adapter.currentItems[3])
        assertEquals(secondOfSecondSection, adapter.currentItems[4])
        assertEquals(thirdOfSecondSection, adapter.currentItems[5])
        assertSection(adapter, 0, 0, 3, 3)
        assertSection(adapter, 1, 3, 3, 3)
        assertSection(adapter, 2, 6, 0, 0)
        assertCount(adapter, 6)

        // Removing from start

        adapter.removeItemsFromSection(0, 2, 1)
        verify(adapter).notifyItemRangeRemoved(2, 1)
        clearInvocations(adapter)
        assertEquals(firstOfFirstSection, adapter.currentItems[0])
        assertEquals(secondOfFirstSection, adapter.currentItems[1])
        assertEquals(firstOfSecondSection, adapter.currentItems[2])
        assertEquals(secondOfSecondSection, adapter.currentItems[3])
        assertEquals(thirdOfSecondSection, adapter.currentItems[4])
        assertSection(adapter, 0, 0, 2, 2)
        assertSection(adapter, 1, 2, 3, 3)
        assertSection(adapter, 2, 5, 0, 0)
        assertCount(adapter, 5)

        adapter.removeItemsFromSection(0, 0, 2)
        verify(adapter).notifyItemRangeRemoved(0, 2)
        clearInvocations(adapter)
        assertEquals(firstOfSecondSection, adapter.currentItems[0])
        assertEquals(secondOfSecondSection, adapter.currentItems[1])
        assertEquals(thirdOfSecondSection, adapter.currentItems[2])
        assertSection(adapter, 0, 0, 0, 0)
        assertSection(adapter, 1, 0, 3, 3)
        assertSection(adapter, 2, 3, 0, 0)
        assertCount(adapter, 3)

        // From center

        adapter.removeItemsFromSection(1, 2, 1)
        verify(adapter).notifyItemRangeRemoved(2, 1)
        clearInvocations(adapter)
        assertEquals(firstOfSecondSection, adapter.currentItems[0])
        assertEquals(secondOfSecondSection, adapter.currentItems[1])
        assertSection(adapter, 0, 0, 0, 0)
        assertSection(adapter, 1, 0, 2, 2)
        assertSection(adapter, 2, 2, 0, 0)
        assertCount(adapter, 2)

        adapter.removeItemsFromSection(1, 0, 2)
        verify(adapter).notifyItemRangeRemoved(0, 2)
        clearInvocations(adapter)
        assertSection(adapter, 0, 0, 0, 0)
        assertSection(adapter, 1, 0, 0, 0)
        assertSection(adapter, 2, 0, 0, 0)
        assertCount(adapter, 0)
    }

    @Test
    fun testSectionItemsGetting() {
        val adapter = getAdapter()
        val firstOfFirstSection = BasicListItem(0, TestListViewObject("0 0"))
        val secondOfFirstSection = BasicListItem(0, TestListViewObject("0 1"))
        val thirdOfFirstSection = BasicListItem(0, TestListViewObject("0 2"))

        adapter.addSection(0)
        adapter.addItemsToSection(0, 0, listOf(firstOfFirstSection, secondOfFirstSection, thirdOfFirstSection))
        clearInvocations(adapter)

        val firstItems = adapter.getItemsFromSection(0, 2, 1, false)
        assertEquals(thirdOfFirstSection, firstItems[0])
        assertEquals(1, firstItems.size)

        val secondItems = adapter.getItemsFromSection(0, 0, 2, false)
        assertEquals(firstOfFirstSection, secondItems[0])
        assertEquals(secondOfFirstSection, secondItems[1])
        assertEquals(2, secondItems.size)
    }

    @Test
    fun testSectionItemUpdating() {
        val adapter = getAdapter()
        val firstOfFirstSection = BasicListItem(0, TestListViewObject("0 0")).apply { viewAnimationState = "Animation" }
        val updatedFirstOfFirstSection = BasicListItem(0, TestListViewObject("0 0 Updated"))

        adapter.addSection(0)
        adapter.addItemsToSection(0, 0, listOf(firstOfFirstSection))
        clearInvocations(adapter)

        adapter.updateSectionItem(0, 0, updatedFirstOfFirstSection)
        assertEquals(updatedFirstOfFirstSection, adapter.currentItems[0])
        assertEquals("Animation", adapter.currentItems[0].viewAnimationState)
    }

    @Test
    fun testSectionItemsChanging() {
        val adapter = getAdapter()
        val first = BasicListItem(0, TestListViewObject("First")).apply { viewAnimationState = "1" }
        val second = BasicListItem(0, TestListViewObject("Second")).apply { viewAnimationState = "2" }
        val third = BasicListItem(0, TestListViewObject("Third")).apply { viewAnimationState = "3" }

        adapter.addSection(0)
        adapter.addItemsToSection(0, 0, listOf(first, second, third))
        clearInvocations(adapter)

        val firstUpdated = BasicListItem(0, TestListViewObject("First Updated", requestedId = "First"))
        val secondUpdated = BasicListItem(0, TestListViewObject("Second Updated", requestedId = "Second"))

        adapter.changeSectionItems(0, listOf(firstUpdated, secondUpdated), false)
        verify(adapter).notifyItemRangeChanged(0, 2, null)
        assertEquals(firstUpdated, adapter.currentItems[0])
        assertEquals(secondUpdated, adapter.currentItems[1])
        assertEquals("1", adapter.currentItems[0].viewAnimationState)
        assertEquals("2", adapter.currentItems[1].viewAnimationState)
        assertEquals(2, adapter.getSectionItemsCount(0))
        assertCount(adapter, 2)
    }

    @Test
    fun testSectionItemsChangingWhenEnabledLoading() {
        val adapter = getAdapter()
        val first = BasicListItem(0, TestListViewObject("First")).apply { viewAnimationState = "1" }
        val second = BasicListItem(0, TestListViewObject("Second")).apply { viewAnimationState = "2" }
        val third = BasicListItem(0, TestListViewObject("Third")).apply { viewAnimationState = "3" }

        adapter.addSection(0)
        adapter.addItemsToSection(0, 0, listOf(first, second, third))
        adapter.toggleLoading(0, true)
        clearInvocations(adapter)

        val firstUpdated = BasicListItem(0, TestListViewObject("First Updated", requestedId = "First"))
        val secondUpdated = BasicListItem(0, TestListViewObject("Second Updated", requestedId = "Second"))

        adapter.changeSectionItems(0, listOf(firstUpdated, secondUpdated), false)
        verify(adapter).notifyItemRangeChanged(0, 2, null)
        assertEquals(firstUpdated, adapter.currentItems[0])
        assertEquals(secondUpdated, adapter.currentItems[1])
        assertEquals("1", adapter.currentItems[0].viewAnimationState)
        assertEquals("2", adapter.currentItems[1].viewAnimationState)
        assertEquals(LOADER_VIEW_TYPE, adapter.currentItems[2].viewType)
        assertEquals(2, adapter.getSectionItemsCount(0))
        assertCount(adapter, 3)
    }

    @Test
    fun testSectionLoading() {
        val adapter = getAdapter()
        val firstOfFirstSection = BasicListItem(0, TestListViewObject("0 0"))
        val firstOfSecondSection = BasicListItem(0, TestListViewObject("1 0"))

        adapter.addSection(0)
        adapter.addSection(1)
        adapter.addItemsToSection(0, 0, listOf(firstOfFirstSection))
        adapter.addItemsToSection(1, 0, listOf(firstOfSecondSection))
        clearInvocations(adapter)

        adapter.toggleLoading(0, true)
        verify(adapter).notifyItemRangeInserted(1, 1)
        assertEquals(firstOfFirstSection, adapter.currentItems[0])
        assertEquals(LOADER_VIEW_TYPE, adapter.currentItems[1].viewType)
        assertEquals(firstOfSecondSection, adapter.currentItems[2])
        assertTrue(adapter.sections[0]!!.isLoading)
        assertFalse(adapter.sections[1]!!.isLoading)
        assertCount(adapter, 3)

        adapter.toggleLoading(1, true)
        verify(adapter).notifyItemRangeInserted(3, 1)
        assertEquals(firstOfFirstSection, adapter.currentItems[0])
        assertEquals(LOADER_VIEW_TYPE, adapter.currentItems[1].viewType)
        assertEquals(firstOfSecondSection, adapter.currentItems[2])
        assertEquals(LOADER_VIEW_TYPE, adapter.currentItems[3].viewType)
        assertTrue(adapter.sections[0]!!.isLoading)
        assertTrue(adapter.sections[1]!!.isLoading)
        assertCount(adapter, 4)

        adapter.toggleLoading(0, false)
        verify(adapter).notifyItemRangeRemoved(1, 1)
        assertEquals(firstOfFirstSection, adapter.currentItems[0])
        assertEquals(firstOfSecondSection, adapter.currentItems[1])
        assertEquals(LOADER_VIEW_TYPE, adapter.currentItems[2].viewType)
        assertFalse(adapter.sections[0]!!.isLoading)
        assertTrue(adapter.sections[1]!!.isLoading)
        assertCount(adapter, 3)

        adapter.toggleLoading(1, false)
        verify(adapter).notifyItemRangeRemoved(2, 1)
        assertEquals(firstOfFirstSection, adapter.currentItems[0])
        assertEquals(firstOfSecondSection, adapter.currentItems[1])
        assertFalse(adapter.sections[0]!!.isLoading)
        assertFalse(adapter.sections[1]!!.isLoading)
        assertCount(adapter, 2)
    }

    @Test
    fun testSectionItemsCountCalculation() {
        val adapter = getAdapter()
        val first = BasicListItem(0, TestListViewObject("0 0"))

        adapter.addSection(0)
        adapter.addItemsToSection(0, 0, listOf(first))
        assertEquals(1, adapter.getSectionItemsCount(0))

        adapter.toggleLoading(0, true)
        assertEquals(1, adapter.getSectionItemsCount(0))

        adapter.toggleLoading(0, false)
        assertEquals(1, adapter.getSectionItemsCount(0))
    }

    @Test
    fun testSectionInitialLoadingDetecting() {
        val adapter = getAdapter()
        val first = BasicListItem(0, TestListViewObject("0 0"))

        adapter.addSection(0)
        adapter.toggleLoading(0, true)

        adapter.addItemsToSection(0, 0, listOf(first))
        assertFalse(adapter.isSectionInInitialLoading(0))

        adapter.removeItemsFromSection(0, 0, 1)
        assertTrue(adapter.isSectionInInitialLoading(0))
    }

    private fun assertSection(adapter: SectionedListTestAdapter, order: Int, startPosition: Int, itemsCount: Int, itemsCountResult: Int) {
        assertEquals(startPosition, adapter.sections[order]!!.startPosition)
        assertEquals(itemsCount, adapter.sections[order]!!.itemsCount)
        assertEquals(itemsCountResult, adapter.getSectionItemsCount(order))
    }

    private fun assertCount(adapter: SectionedListTestAdapter, count: Int) {
        assertEquals(count, adapter.getItemsCount())
        assertEquals(count, adapter.currentItems.size)
    }

    private fun getAdapter(): SectionedListTestAdapter {
        val controller = Robolectric.buildActivity(Activity::class.java)
        val activity = controller.get()
        val list = RecyclerView(activity)
        val adapter = Mockito.spy(SectionedListTestAdapter())

        list.adapter = adapter
        list.layoutManager = LinearLayoutManager(activity)

        activity.setContentView(list)
        controller.create()
        controller.visible()
        Mockito.clearInvocations(list.adapter)

        return adapter
    }
}