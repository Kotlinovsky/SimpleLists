package io.kotlinovsky.simplelists

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.core.view.children
import androidx.core.view.doOnDetach
import androidx.recyclerview.widget.RecyclerView
import io.kotlinovsky.simplelists.loadable.LOADER_VIEW_TYPE
import io.kotlinovsky.simplelists.model.BasicListItem
import io.kotlinovsky.simplelists.model.BasicListViewObject
import io.kotlinovsky.simplelists.sticky.StickyViewBinder

/**
 * Контейнер для списков
 * Отображает загрузчик поверх списка.
 * Также реализует липкие заголовки
 *
 * Примечание к липким заголовкам:
 * Не обновляйте значения View если они не изменились,
 * т.к. это приводит к циклу вызова методов invalidate(),
 * в следствие чего возникает повышенная нагрузка на ЦП устройства.
 */
open class BasicListContainer : FrameLayout, ViewTreeObserver.OnDrawListener {

    internal var isScrolling = false
    internal var stickyView: View? = null
    internal var isLoadingEnabled = false
        set(value) {
            if (loaderView == null) {
                loaderView = getAdapter().createViewHolder(
                    context,
                    layoutInflater,
                    this,
                    LOADER_VIEW_TYPE
                ).itemView
                loaderView!!.layoutParams = LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER
                )

                addView(loaderView!!)
            }

            if (value) {
                loaderView!!.visibility = View.VISIBLE
                listView!!.visibility = View.GONE
            } else {
                stickyView?.visibility = View.GONE
                loaderView!!.visibility = View.GONE
                listView!!.visibility = View.VISIBLE
            }

            field = value
        }

    private var stickyViewType = 0
    private var layoutInflater = LayoutInflater.from(context)
    private var stickyViewHolder: BasicListHolder<BasicListViewObject<*, Any>>? = null
    private var stickyViewBinder: StickyViewBinder<View>? = null
    private var listView: RecyclerView? = null
    private var loaderView: View? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onDraw() {
        if (!isScrolling && stickyView != null && (listView!!.itemAnimator == null || listView!!.itemAnimator!!.isRunning)) {
            updateStickyView()
        }
    }

    override fun onViewAdded(child: View) {
        super.onViewAdded(child)

        if (listView == null && child is RecyclerView) {
            listView = child
            listView!!.viewTreeObserver.addOnDrawListener(this)
            listView!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (stickyView != null) {
                        updateStickyView()
                    }
                }

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    isScrolling = newState != RecyclerView.SCROLL_STATE_IDLE
                }
            })

            doOnDetach { listView!!.viewTreeObserver.removeOnDrawListener(this) }
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        if (!isScrolling && stickyView != null && (listView!!.itemAnimator == null || listView!!.itemAnimator!!.isRunning)) {
            updateStickyView()
        }
    }

    /**
     * Включает прилипание View
     *
     * @param type Тип липкой View
     * @param binder Класс, который будет переводить данные из VO в липкую View
     */
    @Suppress("UNCHECKED_CAST")
    fun enableStickyViews(type: Int, binder: StickyViewBinder<*>) {
        if (stickyView != null) {
            removeView(stickyView)
        }

        stickyViewHolder = getAdapter().createViewHolder(
            context, layoutInflater, this, type
        ) as BasicListHolder<BasicListViewObject<*, Any>>

        stickyViewType = type
        stickyViewBinder = binder as StickyViewBinder<View>
        stickyView = stickyViewHolder!!.itemView

        addView(stickyView!!, 0)
    }

    @Suppress("UNCHECKED_CAST")
    internal fun updateStickyView() {
        val adapter = getAdapter()

        if (adapter.getItemsCount() == 0) {
            changeVisibility(stickyView!!, View.GONE)
            return
        }

        if (stickyView != null && stickyViewBinder != null) {
            val minChild = listView!!.getChildAt(0)
            val minChildPosition = listView!!.getChildAdapterPosition(minChild)

            if (minChildPosition == RecyclerView.NO_POSITION) {
                return
            }

            val minChildItem = adapter.currentItems[minChildPosition]

            if (minChildItem.viewType == stickyViewType) {
                val castedItem = minChildItem as BasicListItem<BasicListViewObject<*, Any>>

                if (stickyView!!.top > minChild.top) {
                    stickyViewHolder!!.bind(castedItem, null)
                    changeVisibility(minChild!!, View.GONE)
                    changeVisibility(stickyView!!, View.VISIBLE)

                    listView!!.children.forEach {
                        if (it != minChild) {
                            changeVisibility(it, View.VISIBLE)
                        }
                    }
                } else if (stickyView!!.top == minChild.top || (stickyView!!.bottom < minChild.bottom && minChildPosition == 0)) {
                    changeVisibility(stickyView!!, View.GONE)
                    listView!!.children.forEach { changeVisibility(it, View.VISIBLE) }
                }

                changeTranslationY(stickyView!!, 0F)
            } else if (minChildItem.viewObject != null && stickyViewBinder!!.bindData(
                    stickyView!!,
                    minChildItem.viewObject
                )
            ) {
                changeVisibility(stickyView!!, View.VISIBLE)
                listView!!.children.forEach { changeVisibility(it, View.VISIBLE) }

                val firstStickyView = listView!!.children.find {
                    val position = listView!!.getChildAdapterPosition(it)

                    if (position == RecyclerView.NO_POSITION) {
                        return
                    }

                    val item = adapter.currentItems[position]
                    item.viewType == stickyViewType
                }

                val needTranslation =
                    if (firstStickyView != null && firstStickyView.bottom in stickyView!!.measuredHeight + 1 until stickyView!!.measuredHeight * 2) {
                        -2F * stickyView!!.measuredHeight + firstStickyView.bottom
                    } else {
                        0F
                    }

                changeTranslationY(stickyView!!, needTranslation)
            } else {
                changeVisibility(stickyView!!, View.GONE)
                changeTranslationY(stickyView!!, 0F)
            }
        }
    }

    private fun getAdapter() = (listView!!.adapter as BasicListAdapter)

    private fun changeVisibility(view: View, visibility: Int) {
        if (view.visibility != visibility) {
            view.visibility = visibility
        }
    }

    private fun changeTranslationY(view: View, translation: Float) {
        if (view.translationY != translation) {
            view.translationY = translation
        }
    }
}