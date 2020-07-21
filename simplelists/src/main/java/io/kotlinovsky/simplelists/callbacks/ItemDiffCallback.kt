package io.kotlinovsky.simplelists.callbacks

import androidx.recyclerview.widget.DiffUtil
import io.kotlinovsky.simplelists.helpers.getChangePayload
import io.kotlinovsky.simplelists.model.BasicListItem

/**
 * Кэллбэк для определения изменения элементов
 *
 * @param old Старый список
 * @param new Новый список
 * @param onItemsSame Кэллбэк, сообщающий о совпадении элементов.
 */
class ItemDiffCallback(
    private val old: List<BasicListItem<*>>,
    private val new: List<BasicListItem<*>>?,
    private val onItemsSame: (BasicListItem<*>, BasicListItem<*>) -> (Unit)
) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val newItem = new!![newItemPosition]
        val oldItem = old[oldItemPosition]
        val isSame = old[oldItemPosition].viewObject?.getId() == new[newItemPosition].viewObject?.getId()

        if (isSame) {
            onItemsSame(oldItem, newItem)
        }

        return isSame
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        old[oldItemPosition].viewObject == new!![newItemPosition].viewObject

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? =
        getChangePayload(old[oldItemPosition], new!![newItemPosition])

    override fun getOldListSize(): Int = old.size
    override fun getNewListSize(): Int = new?.size ?: 0
}