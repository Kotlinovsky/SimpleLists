package io.kotlinovsky.simplelists.loadable

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting
import androidx.recyclerview.widget.DiffUtil
import io.kotlinovsky.simplelists.BasicListAdapter
import io.kotlinovsky.simplelists.BasicListHolder
import io.kotlinovsky.simplelists.callbacks.ItemDiffCallback
import io.kotlinovsky.simplelists.callbacks.OffsetItemUpdateCallback
import io.kotlinovsky.simplelists.model.BasicListItem

const val LOADER_VIEW_TYPE = -1

/**
 * Адаптер для списка с поддержкой загрузки.
 */
abstract class LoadableListAdapter : BasicListAdapter() {

    var isTopLoadingEnabled = false
        set(value) {
            if (value != field) {
                if (value) {
                    super.addItems(0, listOf(BasicListItem(LOADER_VIEW_TYPE, null)))
                } else {
                    super.removeItems(0, 1)
                }
            }

            field = value
        }

    var isBottomLoadingEnabled = false
        set(value) {
            if (value != field) {
                if (value) {
                    super.addItems(
                        super.getItemsCount(),
                        listOf(BasicListItem(LOADER_VIEW_TYPE, null))
                    )
                } else {
                    super.removeItems(super.getItemsCount() - 1, 1)
                }
            }

            field = value
        }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    val countByTypes = HashMap<Int, Int>()

    override fun addItems(position: Int, items: List<BasicListItem<*>>) {
        var insertPosition = position

        if (isTopLoadingEnabled) {
            insertPosition++
        }

        super.addItems(insertPosition, items)
        recalculateTypes(items)
    }

    private fun recalculateTypes(items: List<BasicListItem<*>>) {
        items.forEach {
            // Precalculating for fast paging
            countByTypes[it.viewType] = (countByTypes[it.viewType] ?: 0) + 1
        }
    }

    override fun removeItems(position: Int, count: Int) {
        var removePosition = position

        if (isTopLoadingEnabled) {
            removePosition++
        }

        currentItems.subList(removePosition, removePosition + count).forEach {
            val current = countByTypes[it.viewType]!!

            if (current > 1) {
                countByTypes[it.viewType] = current - 1
            } else {
                countByTypes.remove(it.viewType)
            }
        }

        super.removeItems(removePosition, count)
    }

    override fun getItems(position: Int, count: Int, copy: Boolean): MutableList<BasicListItem<*>> {
        var getPosition = position

        if (isTopLoadingEnabled) {
            getPosition++
        }

        return super.getItems(getPosition, count, copy)
    }

    override fun updateItem(item: BasicListItem<*>, position: Int) {
        var updatePosition = position

        if (isTopLoadingEnabled) {
            updatePosition++
        }

        super.updateItem(item, updatePosition)
    }

    override fun changeItems(newList: List<BasicListItem<*>>?, detectMoves: Boolean) {
        var toIndex = super.getItemsCount()
        var fromIndex = 0

        if (isTopLoadingEnabled) {
            fromIndex++
        }

        if (isBottomLoadingEnabled) {
            toIndex--
        }

        val oldList = currentItems.subList(fromIndex, toIndex)
        val result = DiffUtil.calculateDiff(ItemDiffCallback(ArrayList(oldList), newList) { old, new ->
            new.viewAnimationState = old.viewAnimationState
        }, detectMoves)

        oldList.clear()
        countByTypes.clear()

        if (newList != null) {
            recalculateTypes(newList)
            oldList.addAll(newList)
        }

        result.dispatchUpdatesTo(OffsetItemUpdateCallback(this, fromIndex))
    }

    override fun getItemsCount(): Int {
        var count = super.getItemsCount()

        if (isTopLoadingEnabled) {
            count--
        }

        if (isBottomLoadingEnabled) {
            count--
        }

        return count
    }

    override fun createViewHolder(
        context: Context,
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): BasicListHolder<*> {
        return if (viewType == LOADER_VIEW_TYPE) {
            createLoaderViewHolder(context, inflater, parent)
        } else {
            createItemViewHolder(context, inflater, parent, viewType)
        }
    }

    /**
     * Возвращает Holder необособленного элемента списка.
     *
     * @param context Контекст приложения/активности
     * @param inflater LayoutInflater для получения разметки
     * @param parent Родительская View
     * @param viewType Тип View, которую нужно создать.
     */
    abstract fun createItemViewHolder(
        context: Context,
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): BasicListHolder<*>

    /**
     * Возвращает Holder с View-загрузчиком.
     *
     * @param context Контекст приложения/активности
     * @param inflater LayoutInflater для получения разметки
     * @param parent Родительская View
     */
    abstract fun createLoaderViewHolder(
        context: Context,
        inflater: LayoutInflater,
        parent: ViewGroup
    ): BasicListHolder<*>
}