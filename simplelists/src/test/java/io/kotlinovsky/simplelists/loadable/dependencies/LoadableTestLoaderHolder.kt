package io.kotlinovsky.simplelists.loadable.dependencies

import android.widget.ProgressBar
import io.kotlinovsky.simplelists.BasicListHolder
import io.kotlinovsky.simplelists.model.BasicListItem

/**
 * Тестовый Holder для загрузчика.
 *
 * @param view View загрузчика.
 */
class LoadableTestLoaderHolder(
    private val view: ProgressBar
) : BasicListHolder<Nothing>(view) {

    override fun bind(item: BasicListItem<Nothing>, changePayload: List<Any>?) {
        view.isIndeterminate = true
    }

    override fun cancelAnimations() {
        view.isIndeterminate = false
    }
}