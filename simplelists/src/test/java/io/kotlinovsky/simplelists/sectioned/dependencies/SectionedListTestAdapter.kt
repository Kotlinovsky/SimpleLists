package io.kotlinovsky.simplelists.sectioned.dependencies

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import io.kotlinovsky.simplelists.BasicListHolder
import io.kotlinovsky.simplelists.dependencies.TestListHolder
import io.kotlinovsky.simplelists.loadable.dependencies.LoadableTestLoaderHolder
import io.kotlinovsky.simplelists.sectioned.SectionedListAdapter

/**
 * Тестовый адаптер для списка с секциями.
 */
class SectionedListTestAdapter : SectionedListAdapter() {

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