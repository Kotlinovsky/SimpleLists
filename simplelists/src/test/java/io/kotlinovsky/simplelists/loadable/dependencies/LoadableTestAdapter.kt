package io.kotlinovsky.simplelists.loadable.dependencies

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import io.kotlinovsky.simplelists.BasicListHolder
import io.kotlinovsky.simplelists.dependencies.TestListHolder
import io.kotlinovsky.simplelists.loadable.LoadableListAdapter

/**
 * Тестовый адаптер для списка с поддержкой загрузки.
 */
class LoadableTestAdapter : LoadableListAdapter() {

    override fun createItemViewHolder(
        context: Context,
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): BasicListHolder<*> {
        return TestListHolder(TextView(context))
    }

    override fun createLoaderViewHolder(context: Context, inflater: LayoutInflater, parent: ViewGroup): BasicListHolder<*> {
        return LoadableTestLoaderHolder(ProgressBar(context))
    }
}