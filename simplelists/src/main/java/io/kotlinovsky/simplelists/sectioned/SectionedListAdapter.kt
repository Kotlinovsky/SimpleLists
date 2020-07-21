package io.kotlinovsky.simplelists.sectioned

import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.VisibleForTesting
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import io.kotlinovsky.simplelists.BasicListHolder
import io.kotlinovsky.simplelists.callbacks.ItemDiffCallback
import io.kotlinovsky.simplelists.callbacks.OffsetItemUpdateCallback
import io.kotlinovsky.simplelists.helpers.NestedScrollableHost
import io.kotlinovsky.simplelists.loadable.LOADER_VIEW_TYPE
import io.kotlinovsky.simplelists.loadable.LoadableListAdapter
import io.kotlinovsky.simplelists.model.BasicListItem
import io.kotlinovsky.simplelists.sectioned.model.ListSection
import java.util.*
import kotlin.collections.HashMap

/**
 * Адаптер для списка с поддержкой разделения по секциям.
 */
abstract class SectionedListAdapter : LoadableListAdapter() {

    private val boundViewHolders = Stack<BasicListHolder<*>>()
    private var states = Bundle()

    @VisibleForTesting
    val sections = HashMap<Int, ListSection>()
    var forceLoadingDisabledCallback: ((Int) -> (Unit))? = null

    override fun onBindViewHolder(holder: BasicListHolder<*>, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)

        if (payloads.isEmpty()) {
            getListViewFromHolder(holder)?.let {
                val keys = states.keySet()
                var state: Parcelable? = null
                val iterator = keys.iterator()

                while (iterator.hasNext()) {
                    val key = iterator.next()
                    val section = sections[getSectionByStateKey(key)]

                    if (section == null) {
                        iterator.remove()
                        continue
                    } else if (section.startPosition <= holder.absoluteAdapterPosition) {
                        state = states.getParcelable(key)
                    }
                }

                if (state != null) {
                    it.layoutManager!!.onRestoreInstanceState(state)
                }
            }
        }

        boundViewHolders.remove(holder)
        boundViewHolders.push(holder)
    }

    override fun onViewRecycled(holder: BasicListHolder<*>) {
        super.onViewRecycled(holder)

        if (holder.absoluteAdapterPosition != RecyclerView.NO_POSITION) {
            trySaveListHolder(holder)
        }

        boundViewHolders.remove(holder)
    }

    /**
     * Сохраняет состояние адаптера и его компонентов.
     * Обрабатывает состояние только RecyclerView.
     *
     * @return Bundle с состояниями.
     */
    internal fun saveInstanceState(): Bundle {
        boundViewHolders.forEach {
            trySaveListHolder(it)
        }

        return states
    }

    /**
     * Восстанавливает состояние адаптера и его компонентов
     * Обрабатывает состояние только RecyclerView.
     *
     * @param states Bundle, с которого будет произведено восстановление
     */
    internal fun restoreInstanceState(states: Bundle) {
        this.states = states
    }

    /**
     * Добавляет секцию в список.
     *
     * @param order Порядковый номер секции.
     */
    fun addSection(order: Int) {
        sections[order] = ListSection(false, 0, 0)
    }

    /**
     * Удаляет секцию из списка.
     *
     * @param order Порядковый номер секции.
     */
    open fun removeSection(order: Int) {
        val section = sections[order]!!

        if (section.isLoading) {
            forceLoadingDisabledCallback?.invoke(order)
        }

        removeItems(section.startPosition, section.itemsCount)
        states.remove(order.toString())
        sections.remove(order)
    }

    /**
     * Добавляет элементы в секцию
     *
     * @param order Порядковый номер секции
     * @param position Позиция для вставки относительно секции
     * @param items Элементы для вставки
     */
    open fun addItemsToSection(order: Int, position: Int, items: List<BasicListItem<*>>) {
        var sectionStartPosition = 0

        sections.forEach { (sectionOrder, sectionEntry) ->
            if (sectionOrder < order) {
                sectionStartPosition += sectionEntry.itemsCount
            } else {
                sectionEntry.startPosition += items.size
            }
        }

        val section = sections[order]!!.apply {
            startPosition = sectionStartPosition
            itemsCount += items.size
        }

        addItems(section.startPosition + position, items)
    }

    /**
     * Удаляет элементы из секции
     *
     * @param order Порядковый номер секции
     * @param position Позиция для удаления относительно секции
     * @param count Количество элементов для удаления
     */
    open fun removeItemsFromSection(order: Int, position: Int, count: Int) {
        sections.forEach { (sectionOrder, sectionEntry) ->
            if (sectionOrder > order) {
                sectionEntry.startPosition -= count
            }
        }

        val section = sections[order]!!.apply {
            itemsCount -= count
        }

        removeItems(section.startPosition + position, count)
    }

    /**
     * Выдает элементы из секции
     *
     * @param order Порядковый номер секции
     * @param position Позиция относительно секции, с которой будут выдаваться элементы
     * @param count Количество элементов для выдачи
     * @param copy Копировать элементы?
     */
    open fun getItemsFromSection(order: Int, position: Int, count: Int, copy: Boolean): MutableList<BasicListItem<*>> {
        return getItems(sections[order]!!.startPosition + position, count, copy)
    }

    /**
     * Обновляет элемент в секции
     *
     * @param order Порядковый номер секции
     * @param position Позиция относительно секции
     * @param item Обновленная запись.
     */
    open fun updateSectionItem(order: Int, position: Int, item: BasicListItem<*>) {
        updateItem(item, sections[order]!!.startPosition + position)
    }

    /**
     * Осуществляет изменение данных с помощью DiffUtil
     *
     * @param order Порядковый номер секции
     * @param newList Список с новыми данными
     * @param detectMoves Обнаруживать перемещения элементов?
     */
    open fun changeSectionItems(order: Int, newList: List<BasicListItem<*>>?, detectMoves: Boolean) {
        val section = sections[order]!!
        val fromIndex = section.startPosition
        var toIndex = fromIndex + section.itemsCount

        if (section.isLoading) {
            toIndex--
        }

        val oldList = currentItems.subList(fromIndex, toIndex)
        val result = DiffUtil.calculateDiff(ItemDiffCallback(oldList, newList) { old, new ->
            new.viewAnimationState = old.viewAnimationState
        }, detectMoves)

        oldList.clear()

        if (newList != null) {
            oldList.addAll(newList)
        }

        section.itemsCount = newList?.size ?: 0

        if (section.isLoading) {
            section.itemsCount++
        }

        result.dispatchUpdatesTo(OffsetItemUpdateCallback(this, fromIndex))
    }

    /**
     * Включает/выключает загрузку в секции.
     *
     * @param order Порядковый номер секции
     * @param toggle Включить загрузку?
     */
    open fun toggleLoading(order: Int, toggle: Boolean) {
        val section = sections[order]!!.apply {
            isLoading = toggle
        }

        if (toggle) {
            addItemsToSection(order, section.itemsCount, listOf(BasicListItem(LOADER_VIEW_TYPE, null)))
        } else {
            removeItemsFromSection(order, section.itemsCount - 1, 1)
        }
    }

    /**
     * Возвращает количество элементов в секции.
     * Загрузчик не входит в число элементов секции.
     *
     * @param order Порядковый номер секции.
     */
    open fun getSectionItemsCount(order: Int): Int {
        val section = sections[order]!!
        var count = section.itemsCount

        if (section.isLoading) {
            count--
        }

        return count
    }

    /**
     * Секция находится в начальной загрузке?
     * При чистой загрузке не рекомендуется добавлять элементы в список.
     *
     * @param order Порядковый номер секции.
     */
    open fun isSectionInInitialLoading(order: Int): Boolean {
        val section = sections[order]!!
        return section.isLoading && section.itemsCount == 1
    }

    private fun trySaveListHolder(holder: BasicListHolder<*>) {
        val listView = getListViewFromHolder(holder) ?: return

        sections.entries.find { it.value.startPosition <= holder.absoluteAdapterPosition }?.let {
            states.putParcelable(getSectionStateKey(it.key), listView.layoutManager!!.onSaveInstanceState()!!)
        }
    }

    private fun getListViewFromHolder(holder: BasicListHolder<*>): RecyclerView? = if (holder.itemView is NestedScrollableHost) {
        holder.itemView.getChildAt(0)
    } else {
        holder.itemView
    } as? RecyclerView

    private fun getSectionByStateKey(key: String): Int = key.toInt()
    private fun getSectionStateKey(section: Int): String = "$section"
}