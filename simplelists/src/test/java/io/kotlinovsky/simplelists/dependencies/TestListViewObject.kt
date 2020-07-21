package io.kotlinovsky.simplelists.dependencies

import io.kotlinovsky.simplelists.model.BasicListViewObject

/**
 * Тестовый ViewObject
 *
 * @param title Текст
 * @param changePayload Изменения
 * @param requestedId Необходимый ID
 */
data class TestListViewObject(
    val title: String? = null,
    val changePayload: List<Any>? = null,
    val requestedId: String? = null
) : BasicListViewObject<String, TestListViewObject> {

    override fun getId(): String {
        return requestedId ?: title!!
    }

    override fun getChangePayload(vo: TestListViewObject): List<Any>? {
        return changePayload
    }
}