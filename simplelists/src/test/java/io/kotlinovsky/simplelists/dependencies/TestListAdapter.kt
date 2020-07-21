package io.kotlinovsky.simplelists.dependencies

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import io.kotlinovsky.simplelists.BasicListAdapter
import io.kotlinovsky.simplelists.BasicListHolder

/**
 * Тестовый адаптер
 */
class TestListAdapter : BasicListAdapter() {

    override fun createViewHolder(context: Context, inflater: LayoutInflater, parent: ViewGroup, viewType: Int): BasicListHolder<*> {
        return TestListHolder(TextView(context))
    }
}