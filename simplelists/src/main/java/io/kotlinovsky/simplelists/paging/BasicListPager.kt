package io.kotlinovsky.simplelists.paging

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.kotlinovsky.simplelists.BasicListContainer
import io.kotlinovsky.simplelists.loadable.LoadableListAdapter
import io.kotlinovsky.simplelists.model.BasicListItem

/**
 * Пейджер списка.
 *
 * @param recyclerView Связанный RecyclerView
 * @param adapter Связанный адаптер
 * @param state Состояние пейджера
 * @param loadPageSize Размер загружаемой страницы
 * @param loadThreshold Порог для начала предзагрузки.
 * @param handlingTypes Обрабатываемые типы View
 * @param onBottomPaging Кэллбэк, вызываемый при необходимости подгрузки данных снизу.
 */
class BasicListPager(
    private val recyclerView: RecyclerView,
    private val adapter: LoadableListAdapter,
    private val state: BasicListPagerState,
    private val loadPageSize: Int,
    private val loadThreshold: Int,
    private val handlingTypes: HashSet<Int>? = null,
    private val onBottomPaging: (Int, Int) -> (Unit)
) : RecyclerView.OnScrollListener() {

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        if ((recyclerView.parent as? BasicListContainer)?.isLoadingEnabled == false) {
            if (state.isBottomPagingAllowed && !adapter.isBottomLoadingEnabled) {
                tryLoadBottomPage()
            }
        }
    }

    private fun tryLoadBottomPage() {
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        val lastPosition = layoutManager.findLastVisibleItemPosition()

        if (lastPosition > 0 && adapter.getItemsCount() - lastPosition <= loadThreshold) {
            val offset = if (handlingTypes != null) {
                adapter.currentItems.count { handlingTypes.contains(it.viewType) }
            } else {
                adapter.itemCount
            }

            adapter.isBottomLoadingEnabled = true
            onBottomPaging(loadPageSize, offset)
        }
    }

    /**
     * Завершает пейджинг.
     * Выключает загрузку.
     *
     * @param items Обьекты, которые были загружены.
     */
    fun completeBottomLoading(items: List<BasicListItem<*>>?) {
        if (items == null || items.size < loadPageSize) {
            state.isBottomPagingAllowed = false
        }

        if (items != null) {
            adapter.addItems(adapter.getItemsCount(), items)
        }

        adapter.isBottomLoadingEnabled = false
    }
}