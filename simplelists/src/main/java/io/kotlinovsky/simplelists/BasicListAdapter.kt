package io.kotlinovsky.simplelists

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import io.kotlinovsky.simplelists.callbacks.ItemDiffCallback
import io.kotlinovsky.simplelists.helpers.getChangePayload
import io.kotlinovsky.simplelists.model.BasicListItem
import io.kotlinovsky.simplelists.model.BasicListViewObject

/**
 * Базовый адаптер списка.
 */
abstract class BasicListAdapter : RecyclerView.Adapter<BasicListHolder<*>>() {

    private lateinit var inflater: LayoutInflater

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    var currentItems = ArrayList<BasicListItem<*>>()

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        inflater = LayoutInflater.from(recyclerView.context)
    }

    override fun onViewDetachedFromWindow(holder: BasicListHolder<*>) {
        holder.cancelAnimations()
    }

    override fun onViewRecycled(holder: BasicListHolder<*>) {
        holder.recycleView()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BasicListHolder<*> =
        createViewHolder(parent.context, inflater, parent, viewType)

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: BasicListHolder<*>, position: Int, payloads: MutableList<Any>) {
        val payload = payloads.getOrNull(0) as? List<Any>
        val castedHolder = holder as BasicListHolder<BasicListViewObject<Any?, *>>

        castedHolder.bind(currentItems[position] as BasicListItem<BasicListViewObject<Any?, *>>, payload)
    }

    /**
     * Добавляет элементы в список.
     *
     * @param position Позиция для вставки.
     * @param items Элементы, которые нужно вставить.
     */
    open fun addItems(position: Int, items: List<BasicListItem<*>>) {
        currentItems.addAll(position, items)
        notifyItemRangeInserted(position, items.size)
    }

    /**
     * Удаляет элементы из списка.
     *
     * @param position Позиция, с которой начнется удаление
     * @param count Количество элементов, которые нужно удалить.
     */
    open fun removeItems(position: Int, count: Int) {
        currentItems.subList(position, position + count).clear()
        notifyItemRangeRemoved(position, count)
    }

    /**
     * Выдает элементы, находящиеся в опр. диапазоне.
     *
     * @param position Позиция, с которой нужно получить элементы
     * @param count Количество элементов, которое нужно получить
     * @param copy Скопировать элементы в отдельный список?
     * @return Список с элементами в диапазоне
     */
    open fun getItems(position: Int, count: Int, copy: Boolean): MutableList<BasicListItem<*>> {
        val part = currentItems.subList(position, position + count)

        return if (copy) {
            ArrayList(part)
        } else {
            part
        }
    }

    /**
     * Обновляет запись на определенной позиции.
     *
     * @param item Обновленная запись
     * @param position Позиция записи.
     */
    open fun updateItem(item: BasicListItem<*>, position: Int) {
        val previousItem = currentItems[position]

        currentItems[position] = item.apply {
            viewAnimationState = previousItem.viewAnimationState
        }

        notifyItemChanged(position, getChangePayload(previousItem, item))
    }

    /**
     * Осуществляет изменение данных с помощью DiffUtil
     *
     * @param newList Список с новыми данными
     * @param detectMoves Обнаруживать перемещения элементов?
     */
    open fun changeItems(newList: List<BasicListItem<*>>?, detectMoves: Boolean) {
        val result = DiffUtil.calculateDiff(ItemDiffCallback(currentItems, newList) { old, new ->
            new.viewAnimationState = old.viewAnimationState
        }, detectMoves)

        currentItems = when {
            newList != null -> ArrayList(newList)
            else -> ArrayList()
        }

        result.dispatchUpdatesTo(this)
    }

    /**
     * Выдает количество элементов в списке.
     *
     * @return Количество элементов в списке.
     */
    open fun getItemsCount(): Int = itemCount

    /**
     * Создает и возвращает ViewHolder с нужной View.
     *
     * @param context Контекст приложения/активности
     * @param inflater LayoutInflater для получения разметки
     * @param parent Родительская View
     * @param viewType Тип View, которую нужно создать.
     */
    abstract fun createViewHolder(context: Context, inflater: LayoutInflater, parent: ViewGroup, viewType: Int): BasicListHolder<*>

    override fun onBindViewHolder(holder: BasicListHolder<*>, position: Int) {}
    override fun onFailedToRecycleView(holder: BasicListHolder<*>): Boolean = true
    override fun getItemViewType(position: Int): Int = currentItems[position].viewType
    override fun getItemCount(): Int = currentItems.size
}