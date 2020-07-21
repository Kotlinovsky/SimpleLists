package io.kotlinovsky.simplelists

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import io.kotlinovsky.simplelists.model.BasicListItem
import io.kotlinovsky.simplelists.model.BasicListViewObject

/**
 * Базовый ViewHolder.
 * Реализует поставку данных во View, находящуюся во ViewHolder'е.
 *
 * @param view View, которая будет помещена во ViewHolder.
 */
abstract class BasicListHolder<VO : BasicListViewObject<*, *>>(
    view: View
) : RecyclerView.ViewHolder(view) {

    /**
     * Поставляет данные в ViewHolder.
     * Также должен восстанавливать анимации если есть данные для этого.
     *
     * @param item Элемент списка.
     * @param changePayload Флаги изменений.
     */
    open fun bind(item: BasicListItem<VO>, changePayload: List<Any>?) {
    }

    /**
     * Вызывается при утилизации View.
     */
    open fun recycleView() {
    }

    /**
     * Останавливает анимации, которые происходят во View.
     */
    open fun cancelAnimations() {
    }
}