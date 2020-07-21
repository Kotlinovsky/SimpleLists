package io.kotlinovsky.simplelists

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import io.kotlinovsky.simplelists.sectioned.SectionedListAdapter

class BasicList : RecyclerView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state is BasicListState && adapter is SectionedListAdapter) {
            (adapter as SectionedListAdapter).restoreInstanceState(state.states)
            super.onRestoreInstanceState(state.superState)
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    override fun onSaveInstanceState(): Parcelable? = if (adapter is SectionedListAdapter) {
        BasicListState(super.onSaveInstanceState()!!, (adapter as SectionedListAdapter).saveInstanceState())
    } else {
        super.onSaveInstanceState()
    }
}