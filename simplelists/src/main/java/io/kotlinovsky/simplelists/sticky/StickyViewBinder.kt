package io.kotlinovsky.simplelists.sticky

import android.view.View
import io.kotlinovsky.simplelists.model.BasicListViewObject

/**
 * Класс-переводчик данных в липкую View
 */
interface StickyViewBinder<T : View> {

    /**
     * Переводит данные из ViewObject'а в StickyView
     *
     * @param view View, в которую нужно поставить данные
     * @param vo ViewObject, из которого нужно брать данные
     * @return True если данные были переведены, False - если данные не могут быть использованы
     */
    fun bindData(view: T, vo: BasicListViewObject<*, *>?): Boolean
}