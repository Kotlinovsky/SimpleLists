package io.kotlinovsky.simplelists

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.recyclerview.widget.RecyclerView
import io.kotlinovsky.simplelists.sectioned.SectionedListAdapter

class BasicListState(
    val superState: Parcelable,
    val states: Bundle
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readParcelable<Parcelable>(RecyclerView::class.java.classLoader)!!,
        parcel.readBundle(SectionedListAdapter::class.java.classLoader)!!
    )

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(superState, flags)
        dest.writeBundle(states)
    }

    companion object CREATOR : Parcelable.Creator<BasicListState> {
        override fun createFromParcel(parcel: Parcel): BasicListState = BasicListState(parcel)
        override fun newArray(size: Int): Array<BasicListState?> = arrayOfNulls(size)
    }

    override fun describeContents(): Int = 0
}