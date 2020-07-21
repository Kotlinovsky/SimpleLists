package io.kotlinovsky.simplelists.model

/**
 * Элемент списка.
 *
 * @param viewType Тип отображаемой View.
 * @param viewObject Модель для поставки во View
 * @param viewAnimationState Состояние анимации, которая была остановлена.
 */
open class BasicListItem<VO : BasicListViewObject<*, *>>(
    val viewType: Int = 0,
    val viewObject: VO? = null,
    var viewAnimationState: Any? = null
)